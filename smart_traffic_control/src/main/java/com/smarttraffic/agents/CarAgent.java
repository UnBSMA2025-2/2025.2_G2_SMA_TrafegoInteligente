package com.smarttraffic.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class CarAgent extends Agent {
    private final Random random = new Random();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new TickerBehaviour(this, 4000) {
            @Override
            protected void onTick() {
                if (random.nextDouble() < 0.3) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("Pardal", AID.ISLOCALNAME));
                    msg.setContent("Car entering street");
                    send(msg);
                    System.out.println(getLocalName() + " informou entrada ao pardal.");
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " finalizado.");
    }
}
