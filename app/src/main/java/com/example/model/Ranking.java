package com.example.model;

public class Ranking {
    private String nome;
    private long xp;
    private int posicaoReal; // nova propriedade

    public Ranking(String nome, long xp) {
        this.nome = nome;
        this.xp = xp;
    }

    public String getNome() {
        return nome;
    }

    public long getXp() {
        return xp;
    }

    public int getPosicaoReal() {
        return posicaoReal;
    }

    public void setPosicaoReal(int posicaoReal) {
        this.posicaoReal = posicaoReal;
    }
}


