package com.smarttraffic.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class CarAgent extends Agent {
    private final Random random = new Random();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new TickerBehaviour(this, 4000) {
            @Override
            protected void onTick() {
                // Chance de o carro tentar entrar na rua
                if (random.nextDouble() < 0.3) {
                    informarEntrada();
                    String estadoSemaforo = consultarSemaforo();

                    if (estadoSemaforo == null) {
                        System.out.println(getLocalName() + " não recebeu resposta do semáforo.");
                        return;
                    }

                    if (estadoSemaforo.equalsIgnoreCase("GREEN")) {
                        System.out.println(getLocalName() + " detectou sinal VERDE e está saindo da rua.");
                        informarSaida();
                    } else if (estadoSemaforo.equalsIgnoreCase("RED")) {
                        System.out.println(getLocalName() + " detectou sinal VERMELHO e vai AGUARDAR.");
                    }
                }
            }
        });
    }

    private String consultarSemaforo() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(new AID("TrafficLight", AID.ISLOCALNAME));
        request.setContent("STATUS");
        send(request);
        System.out.println(getLocalName() + " consultou o semáforo.");

        MessageTemplate template = MessageTemplate.MatchSender(new AID("TrafficLight", AID.ISLOCALNAME));
        ACLMessage reply = blockingReceive(template, 2000); // espera até 2 segundos

        if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
            return reply.getContent();
        }
        return null;
    }
    
    private void informarEntrada() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("Pardal", AID.ISLOCALNAME));
        msg.setContent("Car entering street");
        send(msg);
        System.out.println(getLocalName() + " informou ENTRADA na rua ao Pardal.");
    }

    private void informarSaida() {
        ACLMessage leaving = new ACLMessage(ACLMessage.INFORM);
        leaving.addReceiver(new AID("Pardal", AID.ISLOCALNAME));
        leaving.setContent("Car leaving street");
        send(leaving);
        System.out.println(getLocalName() + " informou SAÍDA da rua ao Pardal.");
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " finalizado.");
    }
}
