package com.smarttraffic;

import com.smarttraffic.api.TrafficControllerAPI;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainContainer {

    private static final Map<String, AgentController> activeCars = new HashMap<>();

    private static AgentContainer mainContainerRef;

    public static AgentContainer getMainContainerRef() {
        return mainContainerRef;
    }

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = rt.createMainContainer(profile);
            mainContainerRef = mainContainer;   

            AgentController trafficLight = mainContainer.createNewAgent(
                    "TrafficLight",
                    "com.smarttraffic.agents.TrafficLightAgent",
                    null
            );
            trafficLight.start();

            AgentController pardal = mainContainer.createNewAgent(
                    "Pardal",
                    "com.smarttraffic.agents.PardalAgent",
                    null
            );
            pardal.start();

            System.out.println("Sistema iniciado.");
            System.out.println("Comandos: add N | remove X | list | exit");

            TrafficControllerAPI.start(new MainContainer());
            System.out.println("Servidor REST disponível em http://localhost:8080");    

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
                        String[] parts = input.split(" ");
                        if (parts.length < 2) {
                            System.out.println("Uso: add <quantidade>");
                            continue;
                        }
                        int n = Integer.parseInt(parts[1]);
                        for (int i = 1; i <= n; i++) {
                            String carName = "Car" + (activeCars.size() + 1);
                            AgentController car = mainContainer.createNewAgent(
                                    carName,
                                    "com.smarttraffic.agents.CarAgent",
                                    new Object[]{carName}
                            );
                            car.start();
                            activeCars.put(carName, car);
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
                        if (car != null) {
                            car.kill();
                            System.out.println(carName + " removido.");
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

            for (AgentController car : activeCars.values()) {
                car.kill();
            }
            pardal.kill();
            trafficLight.kill();
            System.exit(0);

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    // ---------------------- API MÉTODOS ----------------------

    // Adiciona N carros e retorna mensagem
    public String addCars(int n) {
        try {
            for (int i = 1; i <= n; i++) {
                String carName = "Car" + (activeCars.size() + 1);
                AgentController car = mainContainerRef.createNewAgent(
                    carName, "com.smarttraffic.agents.CarAgent", new Object[]{carName});    
                car.start();
                activeCars.put(carName, car);
            }
            return n + " carros adicionados.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao adicionar carros: " + e.getMessage();
        }
    }

    // Remove um carro específico
    public String removeCar(String carName) {
        try {
            AgentController car = activeCars.remove(carName);
            if (car != null) {
                car.kill();
                return carName + " removido.";
            } else {
                return "Carro não encontrado: " + carName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao remover carro: " + e.getMessage();
        }
    }

    // Lista carros ativos (como JSON simples)
    public String listCars() {
        return activeCars.keySet().toString();
    }
}
