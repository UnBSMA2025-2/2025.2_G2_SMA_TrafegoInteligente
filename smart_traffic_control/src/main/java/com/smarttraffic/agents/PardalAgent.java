package com.smarttraffic.agents;

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
                    } else if ("Car leaving street".equalsIgnoreCase(conteudo)) {
                        if (carrosNaRua > 0) carrosNaRua--;
                        mudou = true;
                        System.out.println(getLocalName() + " recebeu aviso de " + carro +
                                " que está SAINDO da rua. Total: " + carrosNaRua);
                    }

                    if (mudou) notificarCoordenador();
                } else {
                    block();
                }
            }
        });
    }

    private void notificarCoordenador() {
        ACLMessage aviso = new ACLMessage(ACLMessage.INFORM);
        aviso.addReceiver(new AID("Coordenador", AID.ISLOCALNAME));
        aviso.setContent("CarCountUpdate:" + sufixoRua + ":" + carrosNaRua);
        send(aviso);

        System.out.println(getLocalName() +
                " informou ao Coordenador que há " + carrosNaRua + " carro(s) na rua " + sufixoRua);
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " finalizado.");
    }
}
