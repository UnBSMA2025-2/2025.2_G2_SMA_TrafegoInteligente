package com.smarttraffic.agents;

import com.smarttraffic.model.TrafficConfig;
import com.smarttraffic.model.Coordenada;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agente de Semáforo ATUALIZADO:
 * - Recebe comandos OPEN com tempo específico
 * - Mantém controle interno do tempo verde
 * - Pode auto-fechar após o tempo expirar (backup)
 */
public class TrafficLightAgent extends Agent {

    private boolean isGreen = false;
    private Coordenada coordenada;
    private int tempoVerdeAtual = TrafficConfig.BASE_GREEN_TIME;
    private long tempoAbertura = 0;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        }

        System.out.println(getLocalName() + " iniciado em " +
                (coordenada != null ? coordenada : "coordenada não informada") +
                ". Estado inicial: VERMELHO.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    String conteudo = msg.getContent();

                    // Carros pedem STATUS
                    if (msg.getPerformative() == ACLMessage.REQUEST &&
                            "STATUS".equalsIgnoreCase(conteudo)) {

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(isGreen ? "GREEN" : "RED");
                        send(reply);
                    }

                    // Coordenador envia comandos
                    else if (msg.getPerformative() == ACLMessage.INFORM) {

                        // COMANDO OPEN COM TEMPO: "OPEN:5000"
                        if (conteudo.startsWith("OPEN:")) {
                            try {
                                String[] partes = conteudo.split(":");
                                if (partes.length >= 2) {
                                    tempoVerdeAtual = Integer.parseInt(partes[1]);
                                }
                                
                                if (!isGreen) {
                                    isGreen = true;
                                    tempoAbertura = System.currentTimeMillis();
                                    System.out.println(getLocalName() + " recebeu OPEN - agora está VERDE por " + tempoVerdeAtual + "ms");
                                    
                                    // Inicia comportamento para verificar expiração do tempo
                                    addBehaviour(new VerificaTempoVerde());
                                }
                            } catch (NumberFormatException e) {
                                System.err.println(getLocalName() + " erro ao parsear tempo: " + conteudo);
                            }
                        } 
                        // COMANDO CLOSE simples
                        else if ("CLOSE".equalsIgnoreCase(conteudo)) {
                            if (isGreen) {
                                isGreen = false;
                                tempoVerdeAtual = TrafficConfig.BASE_GREEN_TIME;
                                System.out.println(getLocalName() + " recebeu CLOSE - agora está VERMELHO.");
                            }
                        }
                    }

                    else {
                        block();
                    }
                } else {
                    block();
                }
            }
        });
    }

    /**
     * Comportamento que verifica se o tempo verde expirou
     * (Backup caso o coordenador falhe em enviar CLOSE)
     */
    private class VerificaTempoVerde extends CyclicBehaviour {
        private boolean completed = false;
        
        @Override
        public void action() {
            if (completed || !isGreen) {
                return;
            }
            
            long agora = System.currentTimeMillis();
            long tempoDecorrido = agora - tempoAbertura;
            
            if (tempoDecorrido >= tempoVerdeAtual) {
                // Tempo expirou - auto-fecha como backup
                isGreen = false;
                completed = true;
                System.out.println(getLocalName() + " TEMPO VERDE EXPIRADO - auto-fechando após " + tempoVerdeAtual + "ms");
            } else {
                // Ainda tem tempo, verifica novamente em 500ms
                block(500);
            }
        }
    }

    public boolean isGreen() {
        return isGreen;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }
}