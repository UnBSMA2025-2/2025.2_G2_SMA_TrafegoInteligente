package com.smarttraffic.model;

public class Coordenada {
    private String id;
    private double x;
    private double y;
    private Direcao direcao;

    public enum Direcao { NORTE, SUL, LESTE, OESTE }

    public Coordenada(String id, double x, double y, Direcao direcao) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direcao = direcao;
    }

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Direcao getDirecao() { return direcao; }

    public void setDirecao(Direcao direcao) { this.direcao = direcao; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        return String.format("[%s] (%.2f, %.2f) -> %s", id, x, y, direcao);
    }
}
