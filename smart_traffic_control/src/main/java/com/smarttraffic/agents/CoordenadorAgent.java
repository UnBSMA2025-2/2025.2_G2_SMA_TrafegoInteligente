package com.smarttraffic.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Agente Coordenador responsável por:
 *  - Receber atualizações dos Pardais (quantidade de carros por rua)
 *  - Armazenar esses dados internamente
 *  - (Futuramente) definir a ordem de abertura dos semáforos
 */
public class CoordenadorAgent extends Agent {

    // Armazena a contagem de carros por rua (chave = sufixo, ex: "_0_0_N")
    private final Map<String, Integer> contagemPorRua = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println("Coordenador iniciado. Aguardando informações dos pardais...");

        addBehaviour(new CyclicBehaviour() {
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
    }

    /**
     * Processa a mensagem de atualização enviada por um Pardal.
     * Formato esperado: CarCountUpdate:_X_Y_DIRECAO:quantidade
     */
    private void processarAtualizacao(String conteudo) {
        try {
            // Exemplo: "CarCountUpdate:_0_0_N:3"
            String[] partes = conteudo.split(":");
            if (partes.length != 3) return;

            String sufixo = partes[1];
            int qtd = Integer.parseInt(partes[2]);

            contagemPorRua.put(sufixo, qtd);

            System.out.println("[COORDENADOR] Atualização recebida de " + sufixo +
                    " -> " + qtd + " carro(s) na rua.");

        } catch (Exception e) {
            System.err.println("[COORDENADOR] Erro ao processar mensagem: " + conteudo);
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Coordenador finalizado.");
    }
}
