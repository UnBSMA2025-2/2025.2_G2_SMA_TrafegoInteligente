package com.smarttraffic;

import com.smarttraffic.api.ApiServer;

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
    private static AgentContainer mainContainerRef;
    private static boolean systemActive = false;

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = rt.createMainContainer(profile);
            mainContainerRef = mainContainer;
            systemActive = true;

            System.out.println("Iniciando semáforos e pardais do Grid...");

            for (Map.Entry<String, Coordenada> entry : Grid.listarTodas().entrySet()) {
                String nome = entry.getKey();
                Coordenada coord = entry.getValue();

                try {
                    if (nome.startsWith("SEMAFORO_")) {
                        AgentController semaforo = mainContainer.createNewAgent(
                                nome,
                                "com.smarttraffic.agents.TrafficLightAgent",
                                new Object[]{coord}
                        );
                        semaforo.start();
                        Thread.sleep(100);
                    }

                    if (nome.startsWith("PARDAL_")) {
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
            // INICIA O SERVIDOR REST
            // =====================================================
            new Thread(() -> ApiServer.start(new MainContainer())).start();

            System.out.println("🌐 Servidor REST iniciado em http://localhost:8080");
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
                        shutdown();
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
                            System.out.println("Uso: remove <nome_carro> ou remove <numero>");
                            System.out.println("Exemplo: remove Car1, para remover o carro Car1, ou remove 2, para remover os 2 primeiros carros.");
                            continue;
                        }

                        String carName = parts[1];
                        try {
                            int num = Integer.parseInt(carName);
                            List<String> toRemove = new ArrayList<>(activeCars.keySet()).subList(0, Math.min(num, activeCars.size()));
                            for (String name : toRemove) {
                                AgentController car = activeCars.remove(name);
                                if (car != null) {
                                    car.kill();
                                }
                            }
                            if (toRemove.isEmpty()) {
                                System.out.println("Nenhum carro para remover.");
                            } else {
                            System.out.println(toRemove.size() + " carro(s) removido(s).");
                            }

                        } catch (NumberFormatException e) {
                            AgentController car = activeCars.remove(carName);
                            if (car != null) {
                                car.kill();
                                System.out.println("Carro " + carName + " removido.");
                            } else {
                                System.out.println("Carro não encontrado: " + carName);
                            }
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

    // =====================================================
    // =============== MÉTODOS USADOS PELA API =============
    // =====================================================

    /** Lista os pontos de spawn disponíveis no grid */
    public List<Map<String, Object>> listSpawns() {
        List<Map<String, Object>> spawns = new ArrayList<>();

        Grid.listarTodas().forEach((key, value) -> {
            if (key.startsWith("SPAWN_")) {
                Map<String, Object> spawn = new HashMap<>();
                spawn.put("name", key);
                spawn.put("x", value.getX());
                spawn.put("y", value.getY());
                spawns.add(spawn);
            }
        });

        return spawns;
    }

    /** Lista os carros ativos */
    public List<String> listCars() {
        return new ArrayList<>(activeCars.keySet());
    }

    /** Adiciona N carros em um spawn específico ou aleatório */
    public String addCars(int n, String spawnSelecionado) {
        try {
            List<Map.Entry<String, Coordenada>> spawns = Grid.listarTodas().entrySet().stream()
                    .filter(e -> e.getKey().startsWith("SPAWN_"))
                    .collect(Collectors.toList());

            if (spawns.isEmpty()) {
                return "Nenhum ponto de SPAWN disponível.";
            }

            List<Map.Entry<String, Coordenada>> spawnsDisponiveis = spawns;
            if (spawnSelecionado != null) {
                spawnsDisponiveis = spawns.stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(spawnSelecionado))
                        .collect(Collectors.toList());

                if (spawnsDisponiveis.isEmpty()) {
                    return "Spawn não encontrado: " + spawnSelecionado;
                }
            }

            Random random = new Random();
            for (int i = 1; i <= n; i++) {
                String carName = "Car" + (activeCars.size() + 1);
                var spawn = spawnsDisponiveis.get(random.nextInt(spawnsDisponiveis.size()));
                Coordenada coord = spawn.getValue();

                AgentController car = mainContainerRef.createNewAgent(
                        carName,
                        "com.smarttraffic.agents.CarAgent",
                        new Object[]{coord}
                );
                car.start();
                activeCars.put(carName, car);
            }

            return n + " carro(s) adicionados no spawn " +
                    (spawnSelecionado != null ? spawnSelecionado : "aleatório") + ".";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao adicionar carros: " + e.getMessage();
        }
    }

    /** Remove carro pelo nome */
    public String removeCar(String carName) {
        try {
            AgentController car = activeCars.remove(carName);
            if (car != null) {
                car.kill();
                return "Carro " + carName + " removido.";
            } else {
                return "Carro não encontrado: " + carName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao remover carro: " + e.getMessage();
        }
    }

    /** Retorna informações gerais do sistema (para frontend) */
    public Map<String, Object> getSystemStatus() {
        // Cria o mapa explicitamente para evitar inferência genérica do Java 21
        Map<String, Object> status = new HashMap<>();

        // Adiciona status do sistema
        status.put("active", systemActive);
        status.put("cars", activeCars.size());

        // Conta quantos semáforos existem no grid
        long semaforos = Grid.listarTodas().keySet().stream()
                .filter(k -> k.startsWith("SEMAFORO_"))
                .count();

        // Conta quantos pardais existem no grid
        long pardais = Grid.listarTodas().keySet().stream()
                .filter(k -> k.startsWith("PARDAL_"))
                .count();

        // Adiciona ao mapa
        status.put("trafficLights", semaforos);
        status.put("pardais", pardais);

        // Retorna o mapa completo
        return status;
    }

    public static synchronized void shutdown() {
        if (!systemActive) {
            System.out.println("⚠️ Sistema já está encerrado ou em processo de desligamento.");
            return;
        }

        try {
            systemActive = false;
            System.out.println("Encerrando todos os agentes...");
            for (AgentController car : activeCars.values()) {
                car.kill();
            }
            activeCars.clear();
            System.out.println("✅ Sistema JADE encerrado.");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Erro ao encerrar o sistema: " + e.getMessage());
        }
    }
}
