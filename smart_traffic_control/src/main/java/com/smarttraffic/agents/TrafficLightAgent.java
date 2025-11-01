package com.smarttraffic.agents;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class TrafficLightAgent extends Agent {

    // Configuração do Semáforo
    // Os tempos agora são em milissegundos
    private static final long RED_TIME = 8000; // 8 segundos
    private static final long GREEN_TIME = 5000; // 5 segundos
    private boolean isGreen = false;

    // Variáveis da Máquina de Estados (Não-Bloqueante)
    private long stateChangeTime;
    
    // Nome do agente (TL1 ou TL2)
    private String agentName;

    // HTTP Client para enviar eventos para o Spring Boot
    private transient HttpClient httpClient;
    private transient ObjectMapper objectMapper;
    private static final String SPRING_API_URL = "http://localhost:8080/api/events";

    @Override
    protected void setup() {
        this.agentName = getLocalName();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        // Define o estado inicial (TL1 começa VERMELHO, TL2 começa VERDE)
        if (agentName.equals("TL1")) {
            isGreen = false;
        } else {
            isGreen = true;
        }
        // Define quando o primeiro "tick" de mudança deve ocorrer
        this.stateChangeTime = System.currentTimeMillis() + (isGreen ? GREEN_TIME : RED_TIME);
        
        System.out.println(agentName + " iniciado. Estado inicial: " + (isGreen ? "GREEN" : "RED"));
        sendStateUpdate(); // Envia o estado inicial

        // Comportamento 1: Responder consultas de Carros (ACL)
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    
                    if ("STATUS".equalsIgnoreCase(msg.getContent())) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(isGreen ? "GREEN" : "RED");
                        send(reply);
                        // System.out.println(agentName + " respondeu STATUS para " + msg.getSender().getLocalName());
                    
                    } else if ("CREATE_CAR".equalsIgnoreCase(msg.getContent())) {
                        // Lógica para criar carro (recebida do EventController via Gateway)
                        // Esta lógica é complexa e pode ser movida para o MainContainer
                        // Por enquanto, vamos focar em responder ao status.
                    }

                } else {
                    block();
                }
            }
        });

        // --- COMPORTAMENTO 2: O NOVO TICKER (NÃO-BLOQUEANTE) ---
        // Roda a cada 500ms para verificar se deve mudar o estado
        addBehaviour(new TickerBehaviour(this, 500) {
            @Override
            protected void onTick() {
                // Verifica se o tempo de mudar de estado já passou
                if (System.currentTimeMillis() < stateChangeTime) {
                    return; // Ainda não é a hora de mudar, não faz nada
                }

                // A hora chegou. Inverte o estado.
                if (isGreen) {
                    turnRed();
                } else {
                    turnGreen();
                }
            }
        });
    }

    private void turnGreen() {
        isGreen = true;
        // Define o próximo tempo de mudança (daqui a GREEN_TIME milissegundos)
        stateChangeTime = System.currentTimeMillis() + GREEN_TIME;
        System.out.println(agentName + " está VERDE por " + (GREEN_TIME / 1000) + "s");
        sendStateUpdate();
    }

    private void turnRed() {
        isGreen = false;
        // Define o próximo tempo de mudança (daqui a RED_TIME milissegundos)
        stateChangeTime = System.currentTimeMillis() + RED_TIME;
        System.out.println(agentName + " está VERMELHO por " + (RED_TIME / 1000) + "s");
        sendStateUpdate();
    }

    /**
     * Envia o estado atual (JSON) para o Spring Boot (API /api/events)
     */
    private void sendStateUpdate() {
        try {
            // Cria o payload do evento
            Map<String, Object> payload = Map.of(
                "id", this.agentName,
                "state", this.isGreen ? "GREEN" : "RED"
            );
            Map<String, Object> event = Map.of(
                "type", "TRAFFIC_LIGHT_UPDATE",
                "payload", payload
            );
            
            String jsonBody = objectMapper.writeValueAsString(event);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SPRING_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Envia o HTTP Post de forma assíncrona
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .exceptionally(e -> {
                        System.err.println("[" + agentName + "] Falha ao enviar evento para o Spring: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("[" + agentName + "] Erro ao serializar JSON: " + e.getMessage());
        }
    }
}

