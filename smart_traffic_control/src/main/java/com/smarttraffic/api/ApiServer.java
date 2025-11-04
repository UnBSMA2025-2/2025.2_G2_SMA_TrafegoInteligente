package com.smarttraffic.api;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.smarttraffic.MainContainer;

import java.util.Map;

public class ApiServer {

    private static final Gson gson = new Gson();

    public static void start(MainContainer main) {
        // Porta do servidor REST
        port(8080);

        // CORS (para acesso do frontend)
        options("/*", (req, res) -> {
            String headers = req.headers("Access-Control-Request-Headers");
            if (headers != null) res.header("Access-Control-Allow-Headers", headers);
            String methods = req.headers("Access-Control-Request-Method");
            if (methods != null) res.header("Access-Control-Allow-Methods", methods);
            return "OK";
        });

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
            res.type("application/json");
        });

        // ============= ROTAS ============= //

        // GET /api/spawns  → lista pontos de spawn
        get("/api/spawns", (req, res) -> gson.toJson(main.listSpawns()));

        // GET /api/cars    → lista carros ativos
        get("/api/cars", (req, res) -> gson.toJson(main.listCars()));

        // POST /api/add/:n → adiciona N carros
        post("/api/cars/add", (req, res) -> {
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            int n = ((Double) body.get("n")).intValue();
            String spawn = (String) body.getOrDefault("spawn", null);
            return gson.toJson(main.addCars(n, spawn));
        });

        // DELETE /api/remove/:name → remove carro pelo nome
        delete("/api/remove/:name", (req, res) -> {
            String name = req.params(":name");
            return gson.toJson(main.removeCar(name));
        });

        // GET /api/status → retorna status geral do sistema
        get("/api/status", (req, res) -> gson.toJson(main.getSystemStatus()));

        // POST /api/shutdown → encerra o sistema JADE
        post("/api/shutdown", (req, res) -> {
            new Thread(() -> {
                try {
                    Thread.sleep(500); // pequeno delay para resposta HTTP ser enviada
                    main.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            return gson.toJson(Map.of("message", "Encerrando sistema JADE..."));
        });

        System.out.println("✅ Servidor REST rodando em http://localhost:8080");
    }
}
