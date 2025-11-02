package com.smarttraffic.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Coordenadas fixas com base no grid (x, y) definido no mapa.
 * Cada cruzamento é identificado pelas coordenadas (x, y).
 * Cada cruzamento contém semáforos e pardais associados.
 */
public final class Grid {

    private static final Map<String, Coordenada> coordenadas = new HashMap<>();

    static {
        // =====================================================
        // CRUZAMENTO (0, 0)
        // =====================================================
        coordenadas.put("SEMAFORO_0_0_N", new Coordenada("SEMAFORO_0_0_N", 0.0, 0.0, Coordenada.Direcao.NORTE));
        coordenadas.put("SEMAFORO_0_0_L", new Coordenada("SEMAFORO_0_0_L", 0.0, 0.0, Coordenada.Direcao.LESTE));
        coordenadas.put("SEMAFORO_0_0_O", new Coordenada("SEMAFORO_0_0_O", 0.0, 0.0, Coordenada.Direcao.OESTE));

        coordenadas.put("PARDAL_0_0_N", new Coordenada("PARDAL_0_0_N", 0.0, 0.0, Coordenada.Direcao.NORTE));
        coordenadas.put("PARDAL_0_0_L", new Coordenada("PARDAL_0_0_L", 0.0, 0.0, Coordenada.Direcao.LESTE));
        coordenadas.put("PARDAL_0_0_O", new Coordenada("PARDAL_0_0_O", 0.0, 0.0, Coordenada.Direcao.OESTE));

        // =====================================================
        // CRUZAMENTO (1, 0)
        // =====================================================
        coordenadas.put("SEMAFORO_1_0_S", new Coordenada("SEMAFORO_1_0_S", 1.0, 0.0, Coordenada.Direcao.SUL));
        coordenadas.put("SEMAFORO_1_0_L", new Coordenada("SEMAFORO_1_0_L", 1.0, 0.0, Coordenada.Direcao.LESTE));
        coordenadas.put("SEMAFORO_1_0_O", new Coordenada("SEMAFORO_1_0_O", 1.0, 0.0, Coordenada.Direcao.OESTE));

        coordenadas.put("PARDAL_1_0_S", new Coordenada("PARDAL_1_0_S", 1.0, 0.0, Coordenada.Direcao.SUL));
        coordenadas.put("PARDAL_1_0_L", new Coordenada("PARDAL_1_0_L", 1.0, 0.0, Coordenada.Direcao.LESTE));
        coordenadas.put("PARDAL_1_0_O", new Coordenada("PARDAL_1_0_O", 1.0, 0.0, Coordenada.Direcao.OESTE));

        // =====================================================
        // CRUZAMENTO (2, 0)
        // =====================================================
        coordenadas.put("SEMAFORO_2_0_S", new Coordenada("SEMAFORO_2_0_S", 2.0, 0.0, Coordenada.Direcao.SUL));
        coordenadas.put("SEMAFORO_2_0_L", new Coordenada("SEMAFORO_2_0_L", 2.0, 0.0, Coordenada.Direcao.LESTE));

        coordenadas.put("PARDAL_2_0_S", new Coordenada("PARDAL_2_0_S", 2.0, 0.0, Coordenada.Direcao.SUL));
        coordenadas.put("PARDAL_2_0_L", new Coordenada("PARDAL_2_0_L", 2.0, 0.0, Coordenada.Direcao.LESTE));

        // =====================================================
        // PLACAS DE PARE
        // =====================================================
        coordenadas.put("PARE_1_1_S", new Coordenada("PARE_1_1_S", 1.0, 1.0, Coordenada.Direcao.SUL));
        coordenadas.put("PARE_2_1_S", new Coordenada("PARE_2_1_S", 2.0, 1.0, Coordenada.Direcao.SUL));
        coordenadas.put("PARE_1_-1_S", new Coordenada("PARE_1_-1_S", 1.0, -1.0, Coordenada.Direcao.SUL));

        // =====================================================
        // SAÍDA
        // =====================================================
        coordenadas.put("SAIDA_0_-1_O", new Coordenada("SAIDA_0_-1_O", 0.0, -1.0, Coordenada.Direcao.OESTE));
        coordenadas.put("SAIDA_2_-1_S", new Coordenada("SAIDA_2_-1_S", 2.0, -1.0, Coordenada.Direcao.SUL));

        // =====================================================
        // ENTRADA (SPAWN)
        // =====================================================
        coordenadas.put("SPAWN_0_0_L", new Coordenada("SPAWN_0_0_L", 0.0, 0.0, Coordenada.Direcao.LESTE));
        coordenadas.put("SPAWN_1_1_S", new Coordenada("SPAWN_1_1_S", 1.0, 1.0, Coordenada.Direcao.SUL));
        coordenadas.put("SPAWN_2_1_S", new Coordenada("SPAWN_2_1_S", 2.0, 1.0, Coordenada.Direcao.SUL));
    }

    /** Retorna uma coordenada pelo nome lógico */
    public static Coordenada get(String id) {
        return coordenadas.get(id);
    }

    /** Retorna todas as coordenadas fixas do mapa */
    public static Map<String, Coordenada> listarTodas() {
        return Collections.unmodifiableMap(coordenadas);
    }
}
