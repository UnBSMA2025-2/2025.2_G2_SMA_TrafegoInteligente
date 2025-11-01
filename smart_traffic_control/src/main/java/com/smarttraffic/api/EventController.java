package com.smarttraffic.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EventController {

    private final SimpMessagingTemplate messagingTemplate;
    
    // --- Cliente HTTP para falar com o JADE (MainContainer) ---
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String JADE_HTTP_SERVER_URL = "http://localhost:8081/command/createCar";

    
    public EventController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        System.out.println("EventController iniciado. Pronto para enviar comandos ao JADE via HTTP (localhost:8081).");
    }

    /**
     * Recebe eventos HTTP dos agentes JADE (ex: TrafficLight, Car)
     * e retransmite via WebSocket para o React.
     */
    @PostMapping("/events")
    public void receiveEvent(@RequestBody Object event) {
        // System.out.println("Evento (JADE->Spring): " + event);
        messagingTemplate.convertAndSend("/topic/agents", event);
    }

    /**
     * Recebe comandos HTTP do React (ex: createCar, toggleLightSim)
     */
    @PostMapping("/command/{action}")
    public void receiveCommand(@PathVariable("action") String action, @RequestBody(required = false) Map<String, Object> body) {
        System.out.println("Comando (React->Spring): " + action);

        switch (action) {
            case "createCar":
                // --- NOVA LÓGICA ---
                // Envia um comando HTTP para o servidor SparkJava do MainContainer (JADE)
                sendHttpToJade();
                break;
            
            case "toggleLightSim":
                // Este é o comando de simulação (manual override)
                System.out.println("Comando de simulação 'toggleLightSim' recebido.");
                sendSimulationToggle();
                break;
        }
    }

    /**
     * Envia um comando HTTP POST para o MainContainer (JADE) na porta 8081.
     */
    private void sendHttpToJade() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JADE_HTTP_SERVER_URL))
                    .POST(HttpRequest.BodyPublishers.noBody()) // Não precisamos enviar corpo
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        System.out.println("[Spring->JADE] Comando 'createCar' enviado. Resposta: " + responseBody);
                    })
                    .exceptionally(e -> {
                        System.err.println("[Spring->JADE] Falha ao enviar comando para o JADE: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("[Spring->JADE] Erro ao construir requisição HTTP: " + e.getMessage());
        }
    }


    /**
     * Envia um evento de toggle FALSO (simulação) para o front-end
     * Apenas para fins de teste do botão.
     */
    private void sendSimulationToggle() {
        String tl1State = Math.random() > 0.5 ? "GREEN" : "RED";
        String tl2State = tl1State.equals("GREEN") ? "RED" : "GREEN";

        // Criar objetos Map para serialização JSON correta pelo Spring
        Map<String, Object> payload1 = Map.of(
            "type", "TRAFFIC_LIGHT_UPDATE",
            "payload", Map.of("id", "TL1", "state", tl1State)
        );
        Map<String, Object> payload2 = Map.of(
            "type", "TRAFFIC_LIGHT_UPDATE",
            "payload", Map.of("id", "TL2", "state", tl2State)
        );
        
        messagingTemplate.convertAndSend("/topic/agents", payload1);
        messagingTemplate.convertAndSend("/topic/agents", payload2);
    }
}

