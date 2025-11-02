package com.smarttraffic.agents;

import com.smarttraffic.model.Coordenada;
import com.smarttraffic.model.Coordenada.Direcao;
import com.smarttraffic.model.Grid;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;

public class CarAgent extends Agent {
    private Coordenada coordenada;
    private static final double PASSO = 1.0;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        } else {
            coordenada = new Coordenada(getLocalName(), 0.0, 0.0, Direcao.LESTE);
        }

        System.out.println(getLocalName() + " iniciado em " + coordenada);

        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                executarMovimento();
            }
        });
    }

    private void executarMovimento() {
        boolean temSemaforo = false;

        for (Map.Entry<String, Coordenada> entry : Grid.listarTodas().entrySet()) {
            String id = entry.getKey();
            Coordenada c = entry.getValue();

            // verifica apenas objetos que são semáforos
            if (!id.startsWith("SEMAFORO_")) continue;

            // se o semáforo está na mesma posição e direção do carro
            if (Double.compare(c.getX(), coordenada.getX()) == 0 &&
                Double.compare(c.getY(), coordenada.getY()) == 0 &&
                c.getDirecao() == coordenada.getDirecao()) {

                temSemaforo = true;
                String estado = consultarSemaforo(id);

                if (estado == null) {
                    System.out.println(getLocalName() + " não conseguiu obter o estado de " + id + ", vai aguardar.");
                    return;
                }

                if ("RED".equalsIgnoreCase(estado)) {
                    System.out.println(getLocalName() + " está em " + id + " (" + coordenada +
                            ") e o sinal está VERMELHO. Aguardando...");
                    return;
                }

                if ("GREEN".equalsIgnoreCase(estado)) {
                    System.out.println(getLocalName() + " está em " + id + " e o sinal está VERDE, vai se mover agora.");
                    mover();
                    return;
                }
            }
        }

        // se não há semáforo na posição/direção atual, segue normalmente
        if (!temSemaforo) {
            mover();
        }
    }

    private void mover() {
        Direcao direcao = coordenada.getDirecao();
        double x = coordenada.getX();
        double y = coordenada.getY();

        switch (direcao) {
            case NORTE -> y += PASSO;
            case SUL -> y -= PASSO;
            case LESTE -> x += PASSO;
            case OESTE -> x -= PASSO;
            default -> {
                System.out.println(getLocalName() + " direção desconhecida: " + direcao);
                return;
            }
        }

        coordenada = new Coordenada(getLocalName(), x, y, direcao);
        System.out.println(getLocalName() + " se moveu para " + coordenada);
    }

    private String consultarSemaforo(String semaforoId) {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(new AID(semaforoId, AID.ISLOCALNAME));
        request.setContent("STATUS");
        request.setConversationId("consulta-semaforo-" + semaforoId);
        request.setReplyWith("req" + System.currentTimeMillis());
        send(request);

        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("consulta-semaforo-" + semaforoId),
                MessageTemplate.MatchInReplyTo(request.getReplyWith())
        );

        ACLMessage reply = blockingReceive(mt, 1500);
        if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
            return reply.getContent();
        }
        return null;
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " finalizado.");
    }
}
