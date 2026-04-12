package com.agromonitor;

import com.agromonitor.config.DbConfig;
import com.agromonitor.controller.EventoController;
import com.agromonitor.repository.EventoRepository;
import com.agromonitor.service.EventoService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = readPort();
        DbConfig db = new DbConfig();
        EventoRepository repo = new EventoRepository(db);
        EventoService service = new EventoService(repo);
        EventoController controller = new EventoController(service);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/eventos", controller);
        server.setExecutor(null);
        server.start();

        System.out.println("AgroMonitor backend escuchando en http://localhost:" + port + "/eventos");
    }

    private static int readPort() {
        String p = System.getenv("PORT");
        if (p == null || p.isBlank()) {
            return 8080;
        }
        return Integer.parseInt(p.trim());
    }
}
