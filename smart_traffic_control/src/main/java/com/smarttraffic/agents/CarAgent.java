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
    private static final double PASSO = 0.1;
    private boolean primeiroMovimento = true;
    private String pardalAtual = null;
    private final Random random = new Random();

    // mapa das posicoes de todos os carros
    public static final Map<String, Coordenada> POSICOES_CARROS = new HashMap<>();

    // mapa pras regras de movimento
    private static final Map<String, Map<Direcao, List<Direcao>>> REGRAS = new HashMap<>();
    private static final Map<String, List<Direcao>> REGRAS_SPAWN = new HashMap<>();

    static {
        // Regras pra quem n acabou de spawnar: REGRAS.get("x_y").get(currentDir) = possiveis direcoes
        // (0,0) - meio esquerdo
        Map<Direcao, List<Direcao>> regra00 = new HashMap<>();
        regra00.put(Direcao.NORTE, Arrays.asList(Direcao.NORTE, Direcao.LESTE)); // direcao norte pode ir norte ou leste
        regra00.put(Direcao.OESTE, Arrays.asList(Direcao.NORTE)); // direcao oeste só pode ir norte
        regra00.put(Direcao.LESTE, Arrays.asList(Direcao.NORTE, Direcao.LESTE)); // direcao leste só pode ir norte ou leste (vindo do spawn)
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

        // regras pra quem teve que esperar no spawn
        // (-1,0)
        Map<Direcao, List<Direcao>> spawnn10 = new HashMap<>();
        spawnn10.put(Direcao.LESTE, Arrays.asList(Direcao.LESTE)); // pode ir leste
        REGRAS.put("-1_0", spawnn10);

        // (1,2)
        Map<Direcao, List<Direcao>> spawn12 = new HashMap<>();
        spawn12.put(Direcao.SUL, Arrays.asList(Direcao.SUL)); // pode ir sul
        REGRAS.put("1_2", spawn12);

        // (2,2)
        Map<Direcao, List<Direcao>> spawn22 = new HashMap<>();
        spawn22.put(Direcao.SUL, Arrays.asList(Direcao.SUL)); // pode ir sul
        REGRAS.put("2_2", spawn22);

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
        POSICOES_CARROS.put(getLocalName(), coordenada);

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

    /** 
     * Verifica se as coordenadas (x, y) correspondem a um ponto conhecido do mapa interno (REGRAS ou REGRAS_SPAWN).
     */
    private boolean chegouEmCoordenadaInterna(double x, double y) {
        String key = (int) x + "_" + (int) y;
        return REGRAS.containsKey(key) || REGRAS_SPAWN.containsKey(key);
    }

    /**
     * Verifica se o carro está sobre algum PARDAL do Grid com a mesma direção.
     * Se sim, envia mensagem de entrada (ou manutenção).
     * Se saiu de um pardal anterior, envia mensagem de saída.
     */
    private void verificarPardal() {
        String novoPardal = null;

        // percorre todos os pardais do grid
        for (Map.Entry<String, Coordenada> entry : Grid.listarTodas().entrySet()) {
            String id = entry.getKey();
            Coordenada c = entry.getValue();

            // considera apenas pardais
            if (!id.startsWith("PARDAL_")) continue;

            // deve ter a mesma direção
            if (c.getDirecao() != coordenada.getDirecao()) continue;

            // deve estar na mesma posição exata
            if (Double.compare(c.getX(), coordenada.getX()) == 0 &&
                Double.compare(c.getY(), coordenada.getY()) == 0) {
                novoPardal = id;
                break;
            }
        }

        // se encontrou um novo pardal
        if (novoPardal != null && !novoPardal.equals(pardalAtual)) {
            // se já estava em outro, sai dele
            if (pardalAtual != null) {
                ACLMessage sair = new ACLMessage(ACLMessage.INFORM);
                sair.addReceiver(new AID(pardalAtual, AID.ISLOCALNAME));
                sair.setContent("Car leaving street");
                send(sair);
                System.out.println(getLocalName() + " saiu do pardal " + pardalAtual);
            }

            // entra no novo pardal
            ACLMessage entrar = new ACLMessage(ACLMessage.INFORM);
            entrar.addReceiver(new AID(novoPardal, AID.ISLOCALNAME));
            entrar.setContent("Car entering street");
            send(entrar);
            System.out.println(getLocalName() + " entrou no pardal " + novoPardal);

            pardalAtual = novoPardal;
        }
    }


    private void mover() {
        // Verifica se o carro chegou a um ponto interno do mapa (coordenada mapeada nas REGRAS)
        boolean chegouEmCoordenadaInterna = chegouEmCoordenadaInterna(coordenada.getX(), coordenada.getY());

        // Enquanto não chegou a um ponto mapeado, ele segue em frente na mesma direção
        if (!chegouEmCoordenadaInterna) {
            System.out.println(getLocalName() + " ainda fora das coordenadas mapeadas, seguindo reto até entrar no mapa...");
            moverNaDirecao(coordenada.getDirecao());
            return;
        }

        // Assim que entra em uma coordenada mapeada pela primeira vez, libera as regras normais
        if (primeiroMovimento) {
            primeiroMovimento = false;
            System.out.println(getLocalName() + " entrou oficialmente no mapa interno em " + coordenada);
        }

        // Segue a lógica normal de movimento com base nas regras
        List<Direcao> possiveisDirecoes = direcoesPossiveis(coordenada.getX(), coordenada.getY(),
                coordenada.getDirecao(), primeiroMovimento);

        if (possiveisDirecoes.isEmpty()) {
            System.out.println(getLocalName() + " em " + coordenada + " não tem pra onde ir. Deletando agente.");
            doDelete();
            return;
        }

        Direcao nextDir = possiveisDirecoes.get(random.nextInt(possiveisDirecoes.size()));

        // Condições de saída
        if ((coordenada.getX() == 2 && coordenada.getY() == -1 && nextDir == Direcao.SUL) ||
            (coordenada.getX() == 0 && coordenada.getY() == -1 && nextDir == Direcao.OESTE)) {

            System.out.println(getLocalName() + " chegou em uma saída.");

            // Se estiver atualmente em algum pardal, avisa que saiu
            if (pardalAtual != null) {
                ACLMessage sair = new ACLMessage(ACLMessage.INFORM);
                sair.addReceiver(new AID(pardalAtual, AID.ISLOCALNAME));
                sair.setContent("Car leaving street");
                send(sair);
                System.out.println(getLocalName() + " informou SAÍDA do pardal " + pardalAtual);
                pardalAtual = null;
            }

            // Agora o carro é finalizado
            System.out.println(getLocalName() + " foi removido do mapa (saída).");
            doDelete();
            return;
        }

        moverNaDirecao(nextDir);
    }

    /**
     * Move o carro uma unidade mínima (PASSO) na direção indicada, considerando colisões.
     */
    private void moverNaDirecao(Direcao nextDir) {
        double x = coordenada.getX();
        double y = coordenada.getY();

        double dirPraX = Double.MAX_VALUE;
        double dirPraY = Double.MAX_VALUE;

        switch (nextDir) {
            case NORTE -> dirPraY = (y == Math.ceil(y)) ? 1.0 : Math.ceil(y) - y;
            case SUL -> dirPraY = (y == Math.floor(y)) ? 1.0 : y - Math.floor(y);
            case LESTE -> dirPraX = (x == Math.ceil(x)) ? 1.0 : Math.ceil(x) - x;
            case OESTE -> dirPraX = (x == Math.floor(x)) ? 1.0 : x - Math.floor(x);
        }

        double minDir = Math.min(PASSO, Math.min(dirPraX, dirPraY));
        double movimentoMax = PASSO;

        // Evita colisões
        for (Coordenada outros : POSICOES_CARROS.values()) {
            if (outros == coordenada) continue;
            if (outros.getDirecao() != nextDir) continue;

            boolean carroAFrente = switch (nextDir) {
                case NORTE -> outros.getY() > y;
                case SUL -> outros.getY() < y;
                case LESTE -> outros.getX() > x;
                case OESTE -> outros.getX() < x;
            };

            if (carroAFrente) {
                double distAFrente = switch (nextDir) {
                    case NORTE -> outros.getY() - y;
                    case SUL -> y - outros.getY();
                    case LESTE -> outros.getX() - x;
                    case OESTE -> x - outros.getX();
                };
                double movimentoSeguro = distAFrente - 0.1;
                movimentoMax = Math.min(movimentoMax, movimentoSeguro);
            }
        }

        double movimentoFinal = Math.max(0, Math.min(movimentoMax, minDir));

        switch (nextDir) {
            case NORTE -> y += movimentoFinal;
            case SUL -> y -= movimentoFinal;
            case LESTE -> x += movimentoFinal;
            case OESTE -> x -= movimentoFinal;
        }

        coordenada = new Coordenada(getLocalName(), x, y, nextDir);
        POSICOES_CARROS.put(getLocalName(), coordenada);
        System.out.println(getLocalName() + " moveu para " + coordenada);
        verificarPardal();
    }

    private List<Direcao> direcoesPossiveis(double x, double y, Direcao dir, boolean spawnado) {
        String key = (int) x + "_" + (int) y;
        if (spawnado) {
            return REGRAS_SPAWN.getOrDefault(key, new ArrayList<>());
        } else {
            if(x == Math.floor(x) && y == Math.floor(y)) {
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
        POSICOES_CARROS.remove(getLocalName());
        System.out.println(getLocalName() + " finalizado.");
    }
}
