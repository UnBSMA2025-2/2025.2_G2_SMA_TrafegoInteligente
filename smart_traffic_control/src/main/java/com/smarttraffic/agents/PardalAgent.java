package com.smarttraffic.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class PardalAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    System.out.println("Recebido aviso de " + msg.getSender().getLocalName());
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
