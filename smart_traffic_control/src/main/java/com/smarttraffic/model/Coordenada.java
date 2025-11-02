package com.smarttraffic.model;

public class Coordenada {
    private final String id;
    private final double x;
    private final double y;
    private final Direcao direcao;

    public enum Direcao {
        NORTE, SUL, LESTE, OESTE
    }

    public Coordenada(String id, double x, double y, Direcao direcao) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direcao = direcao;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Direcao getDirecao() {
        return direcao;
    }

    @Override
    public String toString() {
        return String.format("[%s] (%.2f, %.2f) -> %s", id, x, y, direcao);
    }
}
