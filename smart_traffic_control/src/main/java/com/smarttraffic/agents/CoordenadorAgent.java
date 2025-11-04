package com.smarttraffic.agents;

import com.smarttraffic.model.Coordenada;
import com.smarttraffic.model.TrafficConfig;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

/**
 * Agente Coordenador CORRIGIDO:
 * - Envia o tempo de abertura calculado para cada semáforo
 * - Ajusta dinamicamente o tempo baseado no número de carros
 */
public class CoordenadorAgent extends Agent {

    private Coordenada coordenada;
    private final Map<String, Integer> contagemPorRua = new HashMap<>();
    private List<String> ordemSemaforos = new ArrayList<>();
    private int indiceAtual = 0;
    private boolean ativo = true;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        }

        String nome = getLocalName();
        System.out.println(nome + " iniciado em " + coordenada);

        definirOrdemManual(nome);

        if (ordemSemaforos.isEmpty()) {
            System.out.println("[COORDENADOR] Nenhuma ordem definida para " + nome);
            doDelete();
            return;
        }

        System.out.println("[COORDENADOR] Ordem de abertura definida: " + ordemSemaforos);

        // Inicializa contagens
        for (String semaforo : ordemSemaforos) {
            String sufixo = semaforo.replace("SEMAFORO", "");
            contagemPorRua.put(sufixo, 0);
        }

        // Recebe atualizações dos Pardais
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    String conteudo = msg.getContent();
                    if (conteudo.startsWith("CarCountUpdate:")) {
                        processarAtualizacao(conteudo);
                    }
                } else {
                    block();
                }
            }
        });

        // Inicia controle contínuo dos semáforos
        addBehaviour(new ControladorSemaforos());
    }

    private void definirOrdemManual(String nomeCoordenador) {
        switch (nomeCoordenador) {
            case "COORDENADOR_0_0_C" ->
                    ordemSemaforos = Arrays.asList("SEMAFORO_0_0_N", "SEMAFORO_0_0_L", "SEMAFORO_0_0_O");
            case "COORDENADOR_1_0_C" ->
                    ordemSemaforos = Arrays.asList("SEMAFORO_1_0_S", "SEMAFORO_1_0_L", "SEMAFORO_1_0_O");
            case "COORDENADOR_2_0_C" ->
                    ordemSemaforos = Arrays.asList("SEMAFORO_2_0_S", "SEMAFORO_2_0_L");
            default -> ordemSemaforos = new ArrayList<>();
        }
    }

    private void processarAtualizacao(String conteudo) {
        try {
            String[] partes = conteudo.split(":");
            if (partes.length != 3) return;

            String sufixo = partes[1];
            int qtd = Integer.parseInt(partes[2]);
            contagemPorRua.put(sufixo, qtd);

            System.out.println("[COORDENADOR] Atualização de " + sufixo + " -> " + qtd + " carro(s).");
        } catch (Exception e) {
            System.err.println("[COORDENADOR] Erro ao processar mensagem: " + conteudo);
            e.printStackTrace();
        }
    }

    /**
     * Envia mensagem COM TEMPO para um semáforo
     */
    private void enviarMensagemComTempo(String agente, String comando, int tempo) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(agente, AID.ISLOCALNAME));
        msg.setContent(comando + ":" + tempo); // Inclui o tempo na mensagem
        send(msg);
    }

    private void enviarMensagem(String agente, String comando) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(agente, AID.ISLOCALNAME));
        msg.setContent(comando);
        send(msg);
    }

    /**
     * Controlador de semáforos CORRIGIDO - envia tempo calculado
     */
    private class ControladorSemaforos extends OneShotBehaviour {
        @Override
        public void action() {
            new Thread(() -> {
                while (ativo) {
                    try {
                        if (ordemSemaforos.isEmpty()) continue;

                        // Fecha todos os semáforos
                        for (String sem : ordemSemaforos) {
                            enviarMensagem(sem, "CLOSE");
                        }

                        // Delay de transição
                        Thread.sleep(TrafficConfig.TRANSITION_DELAY);

                        // Seleciona próximo semáforo
                        String semaforoAtual = ordemSemaforos.get(indiceAtual);
                        String sufixo = semaforoAtual.replace("SEMAFORO", "");
                        
                        // Obtém número atual de carros e calcula tempo
                        int carros = contagemPorRua.getOrDefault(sufixo, 0);
                        int tempoVerde = TrafficConfig.calcularTempoVerde(carros);

                        // ENVIA COMANDO OPEN COM O TEMPO CALCULADO
                        enviarMensagemComTempo(semaforoAtual, "OPEN", tempoVerde);

                        System.out.println("[COORDENADOR] " + coordenada.getId() +
                                " abriu " + semaforoAtual + " por " + tempoVerde + " ms. (Carros: " + carros + ")");

                        // Aguarda o tempo verde calculado
                        Thread.sleep(tempoVerde);

                        // Avança para próximo semáforo
                        indiceAtual = (indiceAtual + 1) % ordemSemaforos.size();

                    } catch (InterruptedException e) {
                        System.err.println("[COORDENADOR] Ciclo interrompido: " + e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();
        }
    }

    @Override
    protected void takeDown() {
        ativo = false;
        System.out.println(getLocalName() + " finalizado.");
    }
}