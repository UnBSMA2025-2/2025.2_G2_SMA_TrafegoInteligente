package com.smarttraffic;

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

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");
            AgentContainer mainContainer = rt.createMainContainer(profile);

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
}
