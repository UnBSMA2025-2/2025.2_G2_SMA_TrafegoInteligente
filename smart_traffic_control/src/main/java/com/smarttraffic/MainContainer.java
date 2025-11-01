package com.smarttraffic;

// --- Imports JADE ---
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

// --- Imports SparkJava (Servidor HTTP) ---
import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class MainContainer {

    // --- Tornar estático para ser acessível pelo servidor HTTP ---
    private static final Map<String, AgentController> activeCars = new HashMap<>();
    private static final Random random = new Random();
    private static AgentContainer mainContainer; // Tornar o container acessível

    // Pontos de spawn para carros (X, Y, Rotation, Direction)
    // Coordenadas baseadas no mapa fixo de 1000x800
    private static final int[][] spawnPoints = {
            {0, 385, 0, 1},    // Horizontal: Esquerda para Direita
            {970, 425, 180, -1}, // Horizontal: Direita para Esquerda
            {485, 0, 90, 1},     // Vertical: Cima para Baixo
            {525, 770, 270, -1}   // Vertical: Baixo para Cima
    };

    public static void main(String[] args) {
        
        AgentController trafficLight1 = null;
        AgentController trafficLight2 = null;
        AgentController pardal = null;

        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            
            // Inicia o MainContainer na porta 1099
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.GUI, "true"); 
            
            // --- Tornar o mainContainer uma variável de classe ---
            mainContainer = rt.createMainContainer(profile);

            // Cria o primeiro semáforo
            trafficLight1 = mainContainer.createNewAgent(
                    "TL1", "com.smarttraffic.agents.TrafficLightAgent", null
            );
            trafficLight1.start();

            // Cria o segundo semáforo
            trafficLight2 = mainContainer.createNewAgent(
                    "TL2", "com.smarttraffic.agents.TrafficLightAgent", null
            );
            trafficLight2.start();

            pardal = mainContainer.createNewAgent(
                    "Pardal", "com.smarttraffic.agents.PardalAgent", null
            );
            pardal.start();

            System.out.println("Sistema JADE iniciado. MainContainer escutando na porta 1099.");
            System.out.println("Agentes TL1, TL2, e Pardal iniciados.");

            // --- INICIA O SERVIDOR HTTP DO JADE ---
            startHttpServer();

            // --- Loop do Scanner (Console) ---
            System.out.println("Comandos do Console: add N | remove X | list | exit");
            runConsoleScanner();

        } catch (Exception e) {
            System.err.println("Erro fatal ao iniciar o MainContainer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // --- Desligamento ---
            System.out.println("Encerrando servidor HTTP...");
            stop(); // Para o servidor Spark
            
            System.out.println("Encerrando agentes...");
            try {
                for (AgentController car : activeCars.values()) {
                    if (car != null) car.kill();
                }
                if (pardal != null) pardal.kill();
                if (trafficLight1 != null) trafficLight1.kill();
                if (trafficLight2 != null) trafficLight2.kill();
            } catch (StaleProxyException e) {
                System.err.println("Erro ao desligar agentes: " + e.getMessage());
            }
            System.exit(0);
        }
    }

    /**
     * Inicia um servidor HTTP leve (na porta 8081) para receber comandos do Spring Boot.
     */
    private static void startHttpServer() {
        port(8081); // Define a porta para o servidor HTTP do JADE
        
        // Endpoint: /command/createCar
        // Recebe um POST do EventController (Spring)
        post("/command/createCar", (request, response) -> {
            System.out.println("[Servidor HTTP JADE] Comando 'createCar' recebido do Spring!");
            
            // Executa a lógica de criação de carro (a mesma do Scanner)
            String carName = createNewCarAgent();
            
            response.status(200);
            return "Carro criado: " + carName;
        });

        System.out.println("Servidor HTTP do JADE iniciado na porta 8081 (escutando por /command/createCar)");
    }

    /**
     * Lógica do console (Scanner) para comandos manuais.
     */
    private static void runConsoleScanner() {
        try (Scanner scanner = new Scanner(System.in)) {
            String input;
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    System.out.println("Entrada encerrada.");
                    break;
                }
                input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Encerrando sistema...");
                    break;
                } else if (input.startsWith("add")) {
                    int n = 1;
                    try {
                        String[] parts = input.split(" ");
                        if (parts.length > 1) {
                            n = Integer.parseInt(parts[1]);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Uso: add <quantidade>");
                        continue;
                    }
                    for (int i = 0; i < n; i++) {
                        createNewCarAgent();
                    }
                    System.out.println(n + " carros adicionados.");
                
                } else if (input.startsWith("remove")) {
                    String[] parts = input.split(" ");
                    if (parts.length < 2) {
                        System.out.println("Uso: remove <nome_carro>");
                        continue;
                    }
                    String carName = parts[1];
                    AgentController car = activeCars.remove(carName);
                    
                    // --- CORREÇÃO AQUI ---
                    if (car != null) {
                        try {
                            car.kill();
                            System.out.println(carName + " removido.");
                        } catch (StaleProxyException e) {
                            System.err.println("Erro ao remover " + carName + ": " + e.getMessage());
                        }
                    } else {
                        System.out.println("Carro não encontrado: " + carName);
                    }
                } else if (input.equals("list")) {
                    if (activeCars.isEmpty()) {
                        System.out.println("Nenhum carro ativo.");
                    } else {
                        System.out.println("Carros ativos: " + activeCars.keySet());
                    }
                } else {
                    System.out.println("Comando inválido.");
                }
            }
        }
    }

    /**
     * Lógica centralizada para criar um novo CarAgent.
     * Usado tanto pelo Scanner quanto pelo Servidor HTTP.
     */
    private static String createNewCarAgent() {
        String carName = "Car" + (activeCars.size() + System.currentTimeMillis() % 1000);
        int[] spawn = spawnPoints[random.nextInt(spawnPoints.length)];
        Object[] agentArgs = new Object[]{carName, spawn[0], spawn[1], spawn[3]};

        try {
            AgentController car = mainContainer.createNewAgent(
                    carName,
                    "com.smarttraffic.agents.CarAgent",
                    agentArgs
            );
            car.start();
            activeCars.put(carName, car);
            return carName;
        } catch (StaleProxyException e) {
            System.err.println("Erro ao criar agente Carro: " + e.getMessage());
            return null;
        }
    }
}

