package com.smarttraffic.agents;

import com.smarttraffic.model.TrafficConfig;
import com.smarttraffic.model.Coordenada;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agente de semáforo responsável por alternar entre os estados
 * verde e vermelho, responder a requisições de status e ajustar
 * o tempo de espera em função de mensagens de prioridade.
 */
public class TrafficLightAgent extends Agent {

    private int redTime = TrafficConfig.BASE_RED_TIME;
    private int greenTime = TrafficConfig.BASE_GREEN_TIME;
    private volatile boolean isGreen = false;
    private Coordenada coordenada;

    @Override
    protected void setup() {
        // Recupera a coordenada passada na criação do agente
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        }

        System.out.println(getLocalName() + " iniciado em " +
                (coordenada != null ? coordenada : "coordenada não informada"));

        // Comportamento para responder a mensagens de carros
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {

                    // Carro pedindo o status atual do semáforo
                    if ("STATUS".equalsIgnoreCase(msg.getContent())) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(isGreen ? "GREEN" : "RED");
                        send(reply);
                    }
                    // Mensagem de prioridade — reduz tempo de vermelho
                    else {
                        redTime = Math.max(2000, redTime - TrafficConfig.PRIORITY_REDUCTION);
                        System.out.println(getLocalName() +
                                " recebeu prioridade, novo tempo de vermelho: " + redTime + "ms");
                    }

                } else {
                    block();
                }
            }
        });

        // Comportamento para alternar automaticamente o semáforo
        addBehaviour(new TickerBehaviour(this, greenTime + redTime) {
            @Override
            protected void onTick() {
                isGreen = !isGreen;
                System.out.println(getLocalName() + " está " + (isGreen ? "VERDE" : "VERMELHO"));

                // Reseta tempos para valores padrão após cada ciclo
                redTime = TrafficConfig.BASE_RED_TIME;
                greenTime = TrafficConfig.BASE_GREEN_TIME;
            }
        });
    }

    // Métodos utilitários
    public boolean isGreen() {
        return isGreen;
    }

    public int getGreenTime() {
        return greenTime;
    }

    public int getRedTime() {
        return redTime;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }
}
