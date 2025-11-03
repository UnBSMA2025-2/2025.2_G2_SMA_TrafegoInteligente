package com.smarttraffic.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import com.smarttraffic.model.Coordenada;

public class PardalAgent extends Agent {
    private Coordenada coordenada;
    private int carrosNaRua = 0; // contador simples de carros na rua

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        }

        System.out.println(getLocalName() + " iniciado em " + coordenada);

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    String conteudo = msg.getContent();
                    String carro = msg.getSender().getLocalName();

                    if ("Car entering street".equalsIgnoreCase(conteudo)) {
                        carrosNaRua++;
                        System.out.println(getLocalName() + " recebeu aviso de " + carro +
                                " que está ENTRANDO na rua. Total de carros agora: " + carrosNaRua);

                        // opcional: solicitar prioridade ao semáforo
                        ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                        forward.addReceiver(new AID("TrafficLight", AID.ISLOCALNAME));
                        forward.setContent("Request Priority");
                        send(forward);
                    }
                    else if ("Car leaving street".equalsIgnoreCase(conteudo)) {
                        if (carrosNaRua > 0) carrosNaRua--;
                        System.out.println(getLocalName() + " recebeu aviso de " + carro +
                                " que está SAINDO da rua. Total de carros agora: " + carrosNaRua);
                    }
                    else {
                        System.out.println(getLocalName() + " recebeu mensagem desconhecida de " + carro + ": " + conteudo);
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " finalizado. Carros ainda na rua: " + carrosNaRua);
    }
}
