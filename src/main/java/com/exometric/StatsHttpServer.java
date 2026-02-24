package com.exometric;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class StatsHttpServer {

    private static HttpServer server;
    private static int currentPort = -1;

    public static void start() {
        ConfigManager.Config config = ConfigManager.getConfig();
        if (!config.api_enabled) {
            System.out.println("[ExoMetric] API is disabled in config.");
            return;
        }

        if (config.api_port <= 0) {
            System.out.println("[ExoMetric] API port is not set (0). Please configure api_port in config/ExoMetric.json");
            return;
        }

        try {
            currentPort = config.api_port;
            server = HttpServer.create(new InetSocketAddress(currentPort), 0);
            server.createContext("/mc-stats", new MyHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("[ExoMetric] API Server started on port " + currentPort);
        } catch (IOException e) {
            System.err.println("[ExoMetric] Failed to start API Server: " + e.getMessage());
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("[ExoMetric] API Server stopped.");
        }
    }

    public static void reload() {
        ConfigManager.Config config = ConfigManager.getConfig();
        
        // Se a porta mudou ou foi desativado/ativado, reinicia o servidor
        if (server == null || config.api_port != currentPort || !config.api_enabled) {
            stop();
            if (config.api_enabled) {
                start();
            }
        } else {
            System.out.println("[ExoMetric] Config reloaded (token updated).");
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                if (!"GET".equals(t.getRequestMethod())) {
                    t.sendResponseHeaders(405, -1);
                    return;
                }

                String query = t.getRequestURI().getQuery();
                String expectedToken = ConfigManager.getConfig().api_token;
                boolean authorized = false;

                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2 && "token".equals(pair[0])) {
                            if (expectedToken.equals(pair[1])) {
                                authorized = true;
                                break;
                            }
                        }
                    }
                }

                if (!authorized) {
                    byte[] response = "{\"error\": \"Unauthorized\"}".getBytes("UTF-8");
                    t.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    t.sendResponseHeaders(401, response.length);
                    try (OutputStream os = t.getResponseBody()) {
                        os.write(response);
                    }
                    return;
                }

                String path = t.getRequestURI().getPath();
                MetricsData metrics = MetricsCollector.getLatestMetrics();
                
                String json;
                if (path.startsWith("/mc-stats/players")) {
                    json = metrics.toJsonPlayers();
                } else if (path.startsWith("/mc-stats/system")) {
                    json = metrics.toJsonSystem();
                } else {
                    json = metrics.toJson();
                }

                byte[] responseBytes = json.getBytes("UTF-8");
                t.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                t.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(responseBytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                t.sendResponseHeaders(500, -1);
            }
        }
    }
}
