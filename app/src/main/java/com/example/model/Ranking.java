package com.example.model;

public class Ranking {
    private String nome;
    private long xp;

    public Ranking() {}

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

    @Override
    public String toString() {
        return nome + " - " + xp + " XP";
    }
}

