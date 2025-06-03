package com.example.model;

import com.google.firebase.database.ServerValue;

public class Exercicio {

    public int minutosExercicio, minutosBicicleta, minutosCorrida, minutosNatacao;
    public Object data;
    public Exercicio () {

    }

    public Exercicio(int minutosExercicio, int minutosBicicleta, int minutosCorrida, int minutosNatacao, Object data) {
        this.minutosExercicio = minutosExercicio;
        this.minutosBicicleta = minutosBicicleta;
        this.minutosCorrida = minutosCorrida;
        this.minutosNatacao = minutosNatacao;
        this.data = ServerValue.TIMESTAMP;
    }

    public int getMinutosExercicio() {
        return minutosExercicio;
    }

    public void setMinutosExercicio(int minutosExercicio) {
        this.minutosExercicio = minutosExercicio;
    }

    public int getMinutosBicicleta() {
        return minutosBicicleta;
    }

    public void setMinutosBicicleta(int minutosBicicleta) {
        this.minutosBicicleta = minutosBicicleta;
    }

    public int getMinutosCorrida() {
        return minutosCorrida;
    }

    public void setMinutosCorrida(int minutosCorrida) {
        this.minutosCorrida = minutosCorrida;
    }

    public int getMinutosNatacao() {
        return minutosNatacao;
    }

    public void setMinutosNatacao(int minutosNatacao) {
        this.minutosNatacao = minutosNatacao;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
