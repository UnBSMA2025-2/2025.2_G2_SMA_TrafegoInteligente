package com.smarttraffic.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import com.smarttraffic.model.Coordenada;

public class PardalAgent extends Agent {
    private Coordenada coordenada;

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
}
