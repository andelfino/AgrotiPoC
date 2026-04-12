package com.agromonitor.controller;

import com.agromonitor.dto.CrearEventoRequest;
import com.agromonitor.model.Evento;
import com.agromonitor.service.EventoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

public class EventoController implements HttpHandler {

    private final EventoService service;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
                @Override
                public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toString());
                    }
                }

                @Override
                public OffsetDateTime read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    return OffsetDateTime.parse(in.nextString());
                }
            })
            .create();

    public EventoController(EventoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else {
                send(exchange, 405, "{\"error\":\"metodo no permitido\"}");
            }
        } catch (IllegalArgumentException ex) {
            String msg = escapeJson(ex.getMessage() == null ? "solicitud invalida" : ex.getMessage());
            send(exchange, 400, "{\"error\":\"" + msg + "\"}");
        } catch (SQLException ex) {
            String msg = escapeJson(ex.getMessage() == null ? "error de base de datos" : ex.getMessage());
            send(exchange, 500, "{\"error\":\"" + msg + "\"}");
        } catch (Exception ex) {
            String msg = escapeJson(ex.getMessage() == null ? "error interno" : ex.getMessage());
            send(exchange, 500, "{\"error\":\"" + msg + "\"}");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        List<Evento> list = service.listar();
        send(exchange, 200, gson.toJson(list));
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        CrearEventoRequest req;
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            req = gson.fromJson(reader, CrearEventoRequest.class);
        }
        if (req == null) {
            throw new IllegalArgumentException("cuerpo JSON invalido");
        }
        Evento guardado = service.crear(req);
        send(exchange, 201, gson.toJson(guardado));
    }

    private static void send(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
