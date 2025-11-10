package com.smarttraffic.agents;

import com.smarttraffic.api.EventSocket;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import com.smarttraffic.model.Coordenada;

public class PardalAgent extends Agent {
    private Coordenada coordenada;
    private int carrosNaRua = 0;
    private String sufixoRua;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        }

        sufixoRua = getLocalName().replace("PARDAL", "");
        System.out.println(getLocalName() + " iniciado em " + coordenada + " (sufixo " + sufixoRua + ")");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    String conteudo = msg.getContent();
                    String carro = msg.getSender().getLocalName();
                    boolean mudou = false;

                    if ("Car entering street".equalsIgnoreCase(conteudo)) {
                        carrosNaRua++;
                        mudou = true;
                        System.out.println(getLocalName() + " recebeu aviso de " + carro +
                                " que está ENTRANDO na rua. Total: " + carrosNaRua);
                        enviarEventoWebSocket("car_enter");
                    } else if ("Car leaving street".equalsIgnoreCase(conteudo)) {
                        if (carrosNaRua > 0) carrosNaRua--;
                        mudou = true;
                        System.out.println(getLocalName() + " recebeu aviso de " + carro +
                                " que está SAINDO da rua. Total: " + carrosNaRua);
                        enviarEventoWebSocket("car_exit");
                    }

                    if (mudou) {
                        notificarCoordenador();
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void notificarCoordenador() {
        // Envia para o coordenador correto baseado no sufixo
        String coordenadorName = "COORDENADOR" + sufixoRua.substring(0, 4) + "_C"; // Ex: _0_0_N -> COORDENADOR_0_0_C
        
        ACLMessage aviso = new ACLMessage(ACLMessage.INFORM);
        aviso.addReceiver(new AID(coordenadorName, AID.ISLOCALNAME));
        aviso.setContent("CarCountUpdate:" + sufixoRua + ":" + carrosNaRua);
        send(aviso);

        System.out.println(getLocalName() + " informou ao " + coordenadorName + 
                " que há " + carrosNaRua + " carro(s) na rua " + sufixoRua);
    }

    private void enviarEventoWebSocket(String tipoEvento) {
        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("agent", getLocalName());
            evento.put("event", tipoEvento);
            evento.put("streetSuffix", sufixoRua);
            evento.put("carCount", carrosNaRua);
            evento.put("timestamp", java.time.LocalDateTime.now().toString());

            String json = new Gson().toJson(evento);
            EventSocket.broadcastMessage(json);

        } catch (Exception e) {
            System.err.println("Erro ao enviar evento WebSocket do pardal: " + e.getMessage());
        }
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " finalizado.");
    }
}