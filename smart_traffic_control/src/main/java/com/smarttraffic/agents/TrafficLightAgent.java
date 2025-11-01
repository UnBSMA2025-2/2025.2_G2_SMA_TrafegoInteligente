package com.smarttraffic.agents;

import com.smarttraffic.model.TrafficConfig;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class TrafficLightAgent extends Agent {

    private int redTime = TrafficConfig.BASE_RED_TIME;
    private int greenTime = TrafficConfig.BASE_GREEN_TIME;
    private boolean isGreen = false;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) 
                {
                    if (msg.getPerformative() == ACLMessage.REQUEST) 
                    {
                        if ("STATUS".equalsIgnoreCase(msg.getContent())) {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(isGreen ? "GREEN" : "RED");
                            send(reply);
                        } else {
                            System.out.println("Prioridade solicitada por " + msg.getSender().getLocalName());
                            redTime = Math.max(2000, redTime - TrafficConfig.PRIORITY_REDUCTION);
                        }
                    }
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                turnGreen();
                doWait(greenTime);

                turnRed();
                doWait(redTime);

                redTime = TrafficConfig.BASE_RED_TIME;
                greenTime = TrafficConfig.BASE_GREEN_TIME;
            }
        });
    }

    private void turnGreen() {
        isGreen = true;
        System.out.println(getLocalName() + " está VERDE por " + greenTime + "ms");
    }

    private void turnRed() {
        isGreen = false;
        System.out.println(getLocalName() + " está VERMELHO por " + redTime + "ms");
    }

    public boolean getIsGreen() {
        return isGreen;
    }

    public int getGreenTime() {
        return greenTime;
    }

    public int getRedTime() {
        return redTime;
    }
}
