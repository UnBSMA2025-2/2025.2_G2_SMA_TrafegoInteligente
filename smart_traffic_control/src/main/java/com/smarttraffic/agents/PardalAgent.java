package com.smarttraffic.agents;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class PardalAgent extends Agent {

    // Cliente HTTP para a "ponte" com o Spring
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
    private static final String SPRING_API_URL = "http://localhost:8080/api/events";

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    // 1. Recebeu um aviso (do CarAgent)
                    String carName = msg.getSender().getLocalName();
                    System.out.println(getLocalName() + " recebeu aviso de " + carName);
                    
                    // 2. Envia o evento PARDAL_TRIGGERED para o Spring
                    sendPardalEventToSpring(carName);
                    
                    // 3. Lógica original (pedir prioridade ao semáforo)
                    ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                    forward.addReceiver(new AID("TrafficLight", AID.ISLOCALNAME));
                    forward.setContent("Request Priority");
                    send(forward);
                } else {
                    block();
                }
            }
        });
    }

    // Novo método para enviar o evento do pardal para o Spring
    private void sendPardalEventToSpring(String carAgentName) {
        
        // O tipo "PARDAL_TRIGGERED" será lido pelo App.tsx
        String jsonBody = String.format(
            "{\"type\": \"PARDAL_TRIGGERED\", \"payload\": {\"id\": \"%s\", \"carId\": \"%s\"}}",
            getLocalName(), carAgentName
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SPRING_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Envia a requisição de forma assíncrona
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> 
                        System.out.println("[" + getLocalName() + "] Evento de 'furou sinal' enviado ao Spring.")
                    )
                    .exceptionally(e -> {
                        System.err.println("[" + getLocalName() + "] Falha ao enviar evento: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("[" + getLocalName() + "] Erro ao construir requisição HTTP: " + e.getMessage());
        }
    }
}

