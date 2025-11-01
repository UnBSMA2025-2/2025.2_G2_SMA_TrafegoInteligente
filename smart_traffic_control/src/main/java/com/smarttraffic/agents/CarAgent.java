package com.smarttraffic.agents;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarAgent extends Agent {

    private String carId;
    private int x, y, direction; // 0 = horizontal, 1 = vertical
    private int speed = 20; // Pixels por tick
    private final Random random = new Random();
    private boolean isStopped = false;

    // --- Constantes do Mapa (devem ser iguais ao MainContainer e React) ---
    private static final int MAP_WIDTH = 1000;
    private static final int MAP_HEIGHT = 800;
    
    // Ponto de parada (antes do cruzamento)
    // Ajustado para ser mais próximo do centro (500)
    private static final int STOP_LINE_H = (MAP_WIDTH / 2) - 100; // Parar em x=400
    private static final int STOP_LINE_V = (MAP_HEIGHT / 2) - 100; // Parar em y=300
    
    // Ponto onde o semáforo não é mais relevante (depois do cruzamento)
    private static final int CROSSING_END_H = (MAP_WIDTH / 2) + 100; // x=600
    private static final int CROSSING_END_V = (MAP_HEIGHT / 2) + 100; // y=500


    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String SPRING_API_URL = "http://localhost:8080/api/events";

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 4) {
            carId = (String) args[0];
            x = (Integer) args[1];
            y = (Integer) args[2];
            direction = (Integer) args[3];
            System.out.println(carId + " iniciado em (" + x + "," + y + ") dir=" + direction);
        } else {
            System.out.println("Erro: " + getLocalName() + " iniciado sem argumentos de posição.");
            doDelete();
            return;
        }

        // "Coração" do agente: Ticker de 100ms (mais rápido para animação suave)
        addBehaviour(new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                // 1. Consultar o estado do mundo (semáforo)
                String trafficLightState = consultarSemaforo();

                // 2. Tomar decisão
                boolean deveParar = false;
                
                // Só verifica o semáforo se estiver na direção correta E antes de passar do cruzamento
                if (direction == 0 && x < CROSSING_END_H) { // Horizontal
                    // Se estou na zona de parada E o sinal está vermelho
                    if (x < STOP_LINE_H && (x + speed) >= STOP_LINE_H && "RED".equals(trafficLightState)) {
                        deveParar = true;
                    }
                } else if (direction == 1 && y < CROSSING_END_V) { // Vertical
                    // Se estou na zona de parada E o sinal está vermelho
                    // NOTA: O semáforo "GREEN" controla a pista vertical
                    if (y < STOP_LINE_V && (y + speed) >= STOP_LINE_V && "RED".equals(trafficLightState)) {
                         // Na verdade, o TL1 (isGreen=false) é VERDE para a pista vertical
                         // Esta lógica está invertida em relação ao TrafficLightAgent, vamos simplificar:
                         // O TrafficLightAgent (isGreen) controla a Pista 1 (Horizontal)
                         // O estado oposto controla a Pista 2 (Vertical)
                         
                         // Se (isGreen=true) -> TL1=GREEN (Horizontal anda), TL2=RED (Vertical para)
                         // Se (isGreen=false) -> TL1=RED (Horizontal para), TL2=GREEN (Vertical anda)

                         // O CarAgent horizontal (dir=0) deve parar se o estado for "RED"
                         // O CarAgent vertical (dir=1) deve parar se o estado for "GREEN"
                    }
                }

                // Ajuste da lógica de parada com base na direção
                if (direction == 0) { // Horizontal (Controlado por TL1)
                    if (x < STOP_LINE_H && (x + speed) >= STOP_LINE_H && "RED".equals(trafficLightState)) {
                        deveParar = true;
                    }
                } else { // Vertical (Controlado por TL2, que é o oposto de TL1)
                    if (y < STOP_LINE_V && (y + speed) >= STOP_LINE_V && "GREEN".equals(trafficLightState)) {
                        deveParar = true;
                    }
                }


                // 3. Agir
                if (deveParar && !isStopped) {
                    // Chance de 10% de furar o sinal
                    if (random.nextDouble() < 0.1) {
                        System.out.println(carId + " vai FURAR o sinal VERMELHO.");
                        informarPardal();
                        mover();
                    } else {
                        System.out.println(carId + " PARANDO no sinal.");
                        isStopped = true;
                        // Não move, apenas envia a posição atual
                        sendPositionUpdate();
                    }
                } else if (!deveParar) {
                    // Se o sinal está verde (para minha pista) OU eu já passei da linha de parada
                    mover();
                    isStopped = false;
                } else {
                    // Está parado e o sinal continua vermelho (para minha pista)
                    System.out.println(carId + " AGUARDANDO no sinal.");
                    sendPositionUpdate(); // Envia posição parada
                }
            }
        });
    }

    private void mover() {
        if (direction == 0) { // Horizontal
            x += speed;
            if (x > MAP_WIDTH) { // Saiu do mapa
                System.out.println(carId + " saiu do mapa.");
                sendPositionUpdate(); // Envia a última posição antes de deletar
                doDelete(); // Agente se auto-destrói
                return;
            }
        } else { // Vertical
            y += speed;
            if (y > MAP_HEIGHT) { // Saiu do mapa
                System.out.println(carId + " saiu do mapa.");
                sendPositionUpdate(); // Envia a última posição antes de deletar
                doDelete(); // Agente se auto-destrói
                return;
            }
        }
        // Envia a nova posição para o Spring
        sendPositionUpdate();
    }

    private String consultarSemaforo() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(new AID("TrafficLight", AID.ISLOCALNAME));
        request.setContent("STATUS");
        send(request);

        // Espera no máximo 50ms (para não travar o Ticker)
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM)
                .MatchSender(new AID("TrafficLight", AID.ISLOCALNAME));
        ACLMessage reply = blockingReceive(template, 50); 

        if (reply != null) {
            return reply.getContent(); // Retorna "GREEN" ou "RED" (estado do TL1)
        }
        
        // Otimista: se o semáforo não responder a tempo, assume verde
        // (Isso evita que os carros parem se o agente TL estiver lento)
        System.out.println(carId + " não recebeu resposta do semáforo (timeout).");
        return "GREEN"; 
    }

    private void informarPardal() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("Pardal", AID.ISLOCALNAME));
        msg.setContent("FURANDO SINAL");
        send(msg);
        System.out.println(carId + " informou o PARDAL (furou o sinal).");
    }

    // --- Ponte HTTP para o Spring ---
    private void sendPositionUpdate() {
        // { "type": "CAR_POSITION_UPDATE", "payload": { "id": "Car1", "x": 100, "y": 150, "direction": 0 } }
        String jsonPayload = String.format(
            "{\"type\": \"CAR_POSITION_UPDATE\", \"payload\": {\"id\": \"%s\", \"x\": %d, \"y\": %d, \"direction\": %d}}",
            carId, x, y, direction
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SPRING_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            
            // Envia de forma assíncrona (não bloqueia o TickerBehaviour)
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(e -> {
                    // Log silencioso em caso de falha (para não poluir o console)
                    // System.out.println("[" + getLocalName() + "] Falha ao enviar posição: " + e.getMessage());
                    return null;
                });

        } catch (Exception e) {
            // System.out.println("[" + getLocalName() + "] Exceção ao construir requisição HTTP: " + e.getMessage());
        }
    }

    @Override
    protected void takeDown() {
         // Informa ao front-end que este carro foi removido
        String jsonPayload = String.format(
            "{\"type\": \"CAR_REMOVED\", \"payload\": {\"id\": \"%s\"}}",
            carId
        );
         try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SPRING_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            // Envia síncrono, pois estamos no takedown
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // falha silenciosa
        }
        System.out.println(carId + " finalizado e removido.");
    }
}

