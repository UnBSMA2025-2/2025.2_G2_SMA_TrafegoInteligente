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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CarAgent extends Agent {
    private Coordenada coordenada;
    private static final double PASSO = 1.0;
    private boolean primeiroMovimento = true;
    private final Random random = new Random();

    // mapa pras regras de movimento
    private static final Map<String, Map<Direcao, List<Direcao>>> REGRAS = new HashMap<>();
    private static final Map<String, List<Direcao>> REGRAS_SPAWN = new HashMap<>();

    static {
        // Regras pra quem n acabou de spawnar: REGRAS.get("x_y").get(currentDir) = possiveis direcoes
        // (0,0) - meio esquerdo
        Map<Direcao, List<Direcao>> regra00 = new HashMap<>();
        regra00.put(Direcao.NORTE, Arrays.asList(Direcao.NORTE, Direcao.LESTE)); // direcao norte pode ir norte ou leste
        regra00.put(Direcao.LESTE, Arrays.asList(Direcao.NORTE)); // direcao leste só pode ir norte
        regra00.put(Direcao.OESTE, Arrays.asList(Direcao.NORTE, Direcao.LESTE)); // direcao oeste só pode ir norte ou leste (vindo do spawn)
        REGRAS.put("0_0", regra00);

        // (0,1) - canto superior esquerdo
        Map<Direcao, List<Direcao>> regra01 = new HashMap<>();
        regra01.put(Direcao.NORTE, Arrays.asList(Direcao.LESTE)); // direcao norte pode ir leste
        REGRAS.put("0_1", regra01);

        // (1,1) - superior meio
        Map<Direcao, List<Direcao>> regra11 = new HashMap<>();
        regra11.put(Direcao.LESTE, Arrays.asList(Direcao.SUL, Direcao.LESTE)); // direcao leste pode ir sul ou leste
        regra11.put(Direcao.SUL, Arrays.asList(Direcao.LESTE, Direcao.SUL)); // direcao sul pode ir leste ou sul (vindo do spawn)
        REGRAS.put("1_1", regra11);

        // (2,1) - canto superior direito
        Map<Direcao, List<Direcao>> regra21 = new HashMap<>();
        regra21.put(Direcao.LESTE, Arrays.asList(Direcao.SUL)); // direcao leste só pode ir sul
        regra21.put(Direcao.SUL, Arrays.asList(Direcao.SUL)); // direcao sul só pode ir sul (vindo do spawn)
        REGRAS.put("2_1", regra21);

        // (2,0) - meio direito
        Map<Direcao, List<Direcao>> regra20 = new HashMap<>();
        regra20.put(Direcao.SUL, Arrays.asList(Direcao.OESTE, Direcao.SUL)); // direcao sul pode ir oeste ou sul
        regra20.put(Direcao.LESTE, Arrays.asList(Direcao.SUL)); // direcao leste pode ir sul
        REGRAS.put("2_0", regra20);

        // (2, -1) - canto inferior direito + saida -> Sul
        Map<Direcao, List<Direcao>> regra2n1 = new HashMap<>();
        regra2n1.put(Direcao.SUL, Arrays.asList(Direcao.OESTE, Direcao.SUL)); // direcao sul pode ir oeste ou sul (saida)
        REGRAS.put("2_-1", regra2n1);

        // (1, 0) - meio meio
        Map<Direcao, List<Direcao>> regra10 = new HashMap<>();
        regra10.put(Direcao.SUL, Arrays.asList(Direcao.LESTE, Direcao.OESTE, Direcao.SUL)); // direcao sul pode ir leste, oeste ou sul
        regra10.put(Direcao.OESTE, Arrays.asList(Direcao.SUL, Direcao.OESTE)); // direcao oeste pode ir sul ou oeste
        regra10.put(Direcao.LESTE, Arrays.asList(Direcao.SUL, Direcao.LESTE)); // direcao leste pode ir sul ou leste
        REGRAS.put("1_0", regra10);

        // (1,-1) - inferior meio
        Map<Direcao, List<Direcao>> regra1n1 = new HashMap<>();
        regra1n1.put(Direcao.SUL, Arrays.asList(Direcao.OESTE)); // direcao sul só pode ir oeste
        regra1n1.put(Direcao.OESTE, Arrays.asList(Direcao.OESTE)); // direcao oeste só pode ir oeste
        REGRAS.put("1_-1", regra1n1);

        // (0,-1) - canto inferior esquerdo + saida -> Oeste
        Map<Direcao, List<Direcao>> regra0n1 = new HashMap<>();
        regra0n1.put(Direcao.OESTE, Arrays.asList(Direcao.NORTE, Direcao.OESTE)); // direcao oeste pode ir norte ou oeste (saida)
        REGRAS.put("0_-1", regra0n1);

        // Regras pra quem acabou d spawnar: REGRAS_SPAWN.get("x_y") = possiveis direcoes
        REGRAS_SPAWN.put("-1_0", Arrays.asList(Direcao.LESTE)); // pode ir leste
        REGRAS_SPAWN.put("1_2", Arrays.asList(Direcao.SUL)); // pode ir sul
        REGRAS_SPAWN.put("2_2", Arrays.asList(Direcao.SUL)); // pode ir sul

    }

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Coordenada) {
            coordenada = (Coordenada) args[0];
        } else {
            coordenada = new Coordenada(getLocalName(), 0.0, 0.0, Direcao.LESTE);
        }
        primeiroMovimento = true;

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
        List<Direcao> possiveisDirecoes = direcoesPossiveis(coordenada.getX(), coordenada.getY(),
                coordenada.getDirecao(), primeiroMovimento);
        if (possiveisDirecoes.isEmpty()) {
            System.out.println(getLocalName() + " em " + coordenada + " não tem pra onde ir. Deletando agente.");
            doDelete();
            return;
        }
        Direcao nextDir = possiveisDirecoes.get(random.nextInt(possiveisDirecoes.size()));
        primeiroMovimento = false;

        double x = coordenada.getX();
        double y = coordenada.getY();
        double dirPraX = Double.MAX_VALUE;
        double dirPraY = Double.MAX_VALUE;

        switch(nextDir) {
            case NORTE -> dirPraY = (Math.floor(y) + 1) - y;
            case SUL -> dirPraY = y - (Math.floor(y) - 1);
            case LESTE -> dirPraX = (Math.floor(x) + 1) - x;
            case OESTE -> dirPraX = x - (Math.floor(x) - 1);
        }

        double minDir = Math.min(PASSO, Math.min(dirPraX, dirPraY));

        switch (nextDir) {
            case NORTE -> y += minDir;
            case SUL -> y -= minDir;
            case LESTE -> x += minDir;
            case OESTE -> x -= minDir;
        }

        // Condicoes de saida
        if ((x == 2 && y == -1 && nextDir == Direcao.SUL) || (x == 0 && y == -1 && nextDir == Direcao.OESTE)) {
            System.out.println(getLocalName() + " chegou em uma saída. Deletando agente.");
            doDelete();
            return;
        }

        coordenada = new Coordenada(getLocalName(), x, y, nextDir);
        System.out.println(getLocalName() + " moveu para " + coordenada);
    }

    private List<Direcao> direcoesPossiveis(double x, double y, Direcao dir, boolean spawnado) {
        String key = (int) x + "_" + (int) y;
        if (spawnado) {
            return REGRAS_SPAWN.getOrDefault(key, new ArrayList<>());
        } else {
            if((dir == Direcao.LESTE || dir == Direcao.OESTE) ? x == Math.floor(x) : y == Math.floor(y)) {
            Map<Direcao, List<Direcao>> regrasLocal = REGRAS.get(key);
            return regrasLocal != null ? regrasLocal.getOrDefault(dir, new ArrayList<>()) : new ArrayList<>();
            }
        }
        return Arrays.asList(dir); // se n estiver em intersecao, continua na mesma direcao
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
