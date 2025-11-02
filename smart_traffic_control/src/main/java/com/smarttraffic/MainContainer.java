package com.smarttraffic;

import com.smarttraffic.model.Grid;
import com.smarttraffic.model.Coordenada;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.*;
import java.util.stream.Collectors;

public class MainContainer {

    private static final Map<String, AgentController> activeCars = new HashMap<>();

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");
            AgentContainer mainContainer = rt.createMainContainer(profile);

            // =====================================================
            // CRIA TODOS OS SEMÁFOROS E PARDALs DO GRID
            // =====================================================
            System.out.println("Iniciando semáforos e pardais do Grid...");

            for (Map.Entry<String, Coordenada> entry : Grid.listarTodas().entrySet()) {
                String nome = entry.getKey();

                try {
                    if (nome.startsWith("SEMAFORO_")) {
                        Coordenada coord = entry.getValue();
                        AgentController semaforo = mainContainer.createNewAgent(
                                nome,
                                "com.smarttraffic.agents.TrafficLightAgent",
                                new Object[]{coord}
                        );
                        semaforo.start();
                        Thread.sleep(100);
                    }

                    if (nome.startsWith("PARDAL_")) {
                        Coordenada coord = entry.getValue();
                        AgentController pardal = mainContainer.createNewAgent(
                                nome,
                                "com.smarttraffic.agents.PardalAgent",
                                new Object[]{coord}
                        );
                        pardal.start();
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    System.err.printf("Erro ao criar agente %s: %s%n", nome, e.getMessage());
                }
            }

            System.out.println("Todos os semáforos e pardais foram iniciados com base no Grid.");
            System.out.println("=====================================================\n");

            // =====================================================
            // INTERAÇÃO PELO CONSOLE
            // =====================================================
            System.out.println("Sistema iniciado.");
            System.out.println("Comandos: add N [spawn] | remove X | list | listspawn | exit");

            try (Scanner scanner = new Scanner(System.in)) {
                String input;
                Random random = new Random();

                // Filtra os pontos de SPAWN do Grid
                List<Map.Entry<String, Coordenada>> spawns = Grid.listarTodas().entrySet().stream()
                        .filter(e -> e.getKey().startsWith("SPAWN_"))
                        .collect(Collectors.toList());

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

                    } else if (input.equalsIgnoreCase("listspawn")) {
                        if (spawns.isEmpty()) {
                            System.out.println("Nenhum ponto de SPAWN definido no Grid.");
                        } else {
                            System.out.println("Spawns disponíveis:");
                            for (Map.Entry<String, Coordenada> spawn : spawns) {
                                Coordenada c = spawn.getValue();
                                System.out.printf("- %s (x = %.2f, y = %.2f)%n", spawn.getKey(), c.getX(), c.getY());
                            }
                        }

                    } else if (input.startsWith("add")) {
                        String[] parts = input.split(" ");
                        if (parts.length < 2) {
                            System.out.println("Uso: add <quantidade> [spawn]");
                            System.out.println("Exemplo: add 3 SPAWN_0_0");
                            continue;
                        }

                        int n = Integer.parseInt(parts[1]);
                        String spawnSelecionado = parts.length >= 3 ? parts[2] : null;

                        if (spawns.isEmpty()) {
                            System.out.println("Nenhum ponto de SPAWN definido no Grid.");
                            continue;
                        }

                        List<Map.Entry<String, Coordenada>> spawnsDisponiveis = spawns;

                        // Se o usuário informou um spawn específico
                        if (spawnSelecionado != null) {
                            spawnsDisponiveis = spawns.stream()
                                    .filter(e -> e.getKey().equalsIgnoreCase(spawnSelecionado))
                                    .collect(Collectors.toList());

                            if (spawnsDisponiveis.isEmpty()) {
                                System.out.println("Spawn não encontrado: " + spawnSelecionado);
                                System.out.println("Use o comando 'listspawn' para ver os disponíveis.");
                                continue;
                            }
                        }

                        for (int i = 1; i <= n; i++) {
                            String carName = "Car" + (activeCars.size() + 1);

                            // Escolhe spawn aleatório se nenhum foi informado
                            var spawnEscolhido = spawnsDisponiveis.get(random.nextInt(spawnsDisponiveis.size()));
                            Coordenada spawnCoord = spawnEscolhido.getValue();

                            try {
                                AgentController car = mainContainer.createNewAgent(
                                        carName,
                                        "com.smarttraffic.agents.CarAgent",
                                        new Object[]{spawnCoord}
                                );
                                car.start();
                                activeCars.put(carName, car);

                            } catch (Exception e) {
                                System.err.printf("Erro ao criar carro %s: %s%n", carName, e.getMessage());
                            }
                        }

                        System.out.println(n + " carro(s) adicionados no spawn " +
                                (spawnSelecionado != null ? spawnSelecionado : "aleatório") + ".\n");

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
                        System.out.println("Comandos disponíveis: add, remove, list, listspawn, exit");
                    }
                }
            }

            // =====================================================
            // FINALIZAÇÃO DOS AGENTES
            // =====================================================
            for (AgentController car : activeCars.values()) {
                car.kill();
            }

            System.out.println("Encerrando sistema JADE...");
            System.exit(0);

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
