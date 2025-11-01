package com.smarttraffic.api;

import static spark.Spark.*;

import com.smarttraffic.MainContainer;

public class TrafficControllerAPI {

    public static void start(MainContainer main) {
        // Porta do servidor REST
        port(8080);

        // Configura CORS (para permitir acesso do React futuramente)
        options("/*", (req, res) -> "");
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });

        // Rota para listar carros
        get("/list", (req, res) -> {
            res.type("application/json");
            return main.listCars();
        });

        // Rota para adicionar N carros
        post("/add/:n", (req, res) -> {
            int n = Integer.parseInt(req.params(":n"));
            return main.addCars(n);
        });

        // Rota para remover carro específico
        delete("/remove/:name", (req, res) -> {
            String name = req.params(":name");
            return main.removeCar(name);
        });

        System.out.println("Servidor REST rodando em http://localhost:8080");
    }
}
