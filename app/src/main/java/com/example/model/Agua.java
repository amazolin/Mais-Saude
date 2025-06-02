package com.example.model;

import com.google.firebase.database.ServerValue;

public class Agua {
    public int quantidade; // ml de água
    public Object data;

    public Agua() {
        // Construtor vazio necessário para o Firebase
    }

    public Agua(int quantidade) {
        this.quantidade = quantidade;
        this.data = ServerValue.TIMESTAMP;
    }

    // Getters e Setters
    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}