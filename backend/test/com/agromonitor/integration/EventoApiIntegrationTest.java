package com.agromonitor.integration;

import com.agromonitor.config.DbConfig;
import com.agromonitor.controller.EventoController;
import com.agromonitor.repository.EventoRepository;
import com.agromonitor.service.EventoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoApiIntegrationTest {

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private static HttpServer server;
    private static String baseUrl;
    private static DbConfig dbConfig;

    @BeforeAll
    static void setUpServer() throws Exception {
        dbConfig = new DbConfig();
        EventoRepository repository = new EventoRepository(dbConfig);
        EventoService service = new EventoService(repository);
        EventoController controller = new EventoController(service);

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/eventos", controller);
        server.setExecutor(null);
        server.start();

        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void tearDownServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void clearTable() throws Exception {
        try (Connection c = DriverManager.getConnection(dbConfig.getJdbcUrl(), dbConfig.getUser(), dbConfig.getPassword());
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM evento");
        }
    }

    @Test
    void altaEventoValidoYLuegoApareceEnGet() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivoId", "SEN-01");
        body.addProperty("tipoEvento", "temperatura");
        body.addProperty("valor", 31.4);

        HttpResponse<String> post = postEventos(body.toString());
        assertEquals(201, post.statusCode());

        JsonObject creado = JsonParser.parseString(post.body()).getAsJsonObject();
        assertEquals("SEN-01", creado.get("dispositivoId").getAsString());
        assertEquals("temperatura", creado.get("tipoEvento").getAsString());
        assertEquals("advertencia", creado.get("severidad").getAsString());

        HttpResponse<String> get = getEventos();
        assertEquals(200, get.statusCode());
        JsonArray arr = JsonParser.parseString(get.body()).getAsJsonArray();
        assertEquals(1, arr.size());
        JsonObject persisted = arr.get(0).getAsJsonObject();
        assertEquals("SEN-01", persisted.get("dispositivoId").getAsString());
    }

    @Test
    void tipoEventoInvalidoDevuelve400YSinPersistir() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivoId", "SEN-02");
        body.addProperty("tipoEvento", "ruido");
        body.addProperty("valor", 10);

        HttpResponse<String> post = postEventos(body.toString());
        assertEquals(400, post.statusCode());
        assertTrue(post.body().contains("tipoEvento"));
        assertEquals(0, contarEventosEnDb());
    }

    @Test
    void valorFaltanteDevuelve400YSinPersistir() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivoId", "SEN-03");
        body.addProperty("tipoEvento", "bateria");

        HttpResponse<String> post = postEventos(body.toString());
        assertEquals(400, post.statusCode());
        assertTrue(post.body().contains("valor"));
        assertEquals(0, contarEventosEnDb());
    }

    @Test
    void valorInvalidoParaConexionDevuelve400() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivoId", "SEN-04");
        body.addProperty("tipoEvento", "conexion");
        body.addProperty("valor", 2);

        HttpResponse<String> post = postEventos(body.toString());
        assertEquals(400, post.statusCode());
        assertTrue(post.body().contains("0 o 1"));
        assertEquals(0, contarEventosEnDb());
    }

    @Test
    void dispositivoIdVacioDevuelve400YSinPersistir() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivoId", " ");
        body.addProperty("tipoEvento", "temperatura");
        body.addProperty("valor", 20.0);

        HttpResponse<String> post = postEventos(body.toString());
        assertEquals(400, post.statusCode());
        assertTrue(post.body().contains("dispositivoId"));
        assertEquals(0, contarEventosEnDb());
    }

    @Test
    void severidadSeCalculaCorrectamente() throws Exception {
        assertEquals("normal", crearYLeerSeveridad("temperatura", 29.9));
        assertEquals("advertencia", crearYLeerSeveridad("temperatura", 31.0));
        assertEquals("normal", crearYLeerSeveridad("bateria", 80.0));
        assertEquals("critico", crearYLeerSeveridad("bateria", 10.0));
        assertEquals("normal", crearYLeerSeveridad("conexion", 1.0));
        assertEquals("critico", crearYLeerSeveridad("conexion", 0.0));
    }

    private String crearYLeerSeveridad(String tipoEvento, double valor) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivoId", "SEN-SEV-" + tipoEvento + "-" + valor);
        body.addProperty("tipoEvento", tipoEvento);
        body.addProperty("valor", valor);
        HttpResponse<String> post = postEventos(body.toString());
        assertEquals(201, post.statusCode());
        JsonObject response = JsonParser.parseString(post.body()).getAsJsonObject();
        return response.get("severidad").getAsString();
    }

    private HttpResponse<String> postEventos(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/eventos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> getEventos() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/eventos"))
                .GET()
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private int contarEventosEnDb() throws Exception {
        String sql = "SELECT COUNT(*) FROM evento";
        try (Connection c = DriverManager.getConnection(dbConfig.getJdbcUrl(), dbConfig.getUser(), dbConfig.getPassword());
             PreparedStatement ps = c.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
