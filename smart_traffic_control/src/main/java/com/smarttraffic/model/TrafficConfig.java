package com.smarttraffic.model;

/**
 * Configurações globais de tempo do sistema de tráfego.
 * 
 * Todos os valores são expressos em milissegundos.
 */
public final class TrafficConfig {

    /** Tempo base mínimo de verde */
    public static int BASE_GREEN_TIME = 5000; // 5s

    /** Tempo base mínimo de vermelho */
    public static int BASE_RED_TIME = 5000; // 5s

    /** Redução aplicada em caso de prioridade (para o futuro) */
    public static int PRIORITY_REDUCTION = 1000; // 1s

    /** Fator de incremento por carro detectado */
    public static int PER_CAR_INCREMENT = 1000; // +1s por carro

    /** Tempo máximo permitido de sinal verde */
    public static int MAX_GREEN_TIME = 15000; // 15s

    /** Tempo mínimo garantido de sinal verde */
    public static int MIN_GREEN_TIME = 3000; // 3s

    /** Delay de segurança entre o fechamento de um semáforo e a abertura do próximo */
    public static int TRANSITION_DELAY = 1000; // 1s (simula o "amarelo")

    /**
     * Calcula o tempo verde ideal com base no número de carros.
     * Aplica limites de segurança mínimo e máximo.
     */
    public static int calcularTempoVerde(int carros) {
        int tempo = BASE_GREEN_TIME + (carros * PER_CAR_INCREMENT);
        if (tempo < MIN_GREEN_TIME) tempo = MIN_GREEN_TIME;
        if (tempo > MAX_GREEN_TIME) tempo = MAX_GREEN_TIME;
        return tempo;
    }

    /**
     * Retorna o tempo total do ciclo (verde + vermelho).
     */
    public static int getCicloCompleto() {
        return BASE_GREEN_TIME + BASE_RED_TIME + TRANSITION_DELAY;
    }
}
