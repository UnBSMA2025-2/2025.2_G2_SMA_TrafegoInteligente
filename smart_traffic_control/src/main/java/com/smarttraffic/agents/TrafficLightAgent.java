package com.smarttraffic.agents;

import com.smarttraffic.model.TrafficConfig;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class TrafficLightAgent extends Agent {

    private int redTime = TrafficConfig.BASE_RED_TIME;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    System.out.println("Prioridade solicitada por " + msg.getSender().getLocalName());
                    redTime = Math.max(2000, redTime - TrafficConfig.PRIORITY_REDUCTION);
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                System.out.println("Verde por " + TrafficConfig.BASE_GREEN_TIME + "ms");
                doWait(TrafficConfig.BASE_GREEN_TIME);

                System.out.println("Vermelho por " + redTime + "ms");
                doWait(redTime);

                redTime = TrafficConfig.BASE_RED_TIME;
            }
        });
    }
}
