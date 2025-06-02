package com.example.model; // Ou o pacote apropriado

import com.google.firebase.database.ServerValue; // Para timestamps do servidor

public class Sono {
    public double horasDormidas; // Horas de sono (NOME CORRIGIDO)
    public Object data;    // Para usar ServerValue.TIMESTAMP

    public Sono() {
        // Construtor vazio necessário para o Firebase
    }

    // O nome do parâmetro do construtor pode ser o que você preferir (ex: horasDeSono),
    // o importante é que ele seja atribuído ao campo correto da classe (horasDormidas).
    public Sono(double horasDeSono) {
        this.horasDormidas = horasDeSono;
        this.data = ServerValue.TIMESTAMP; // Define a data/hora atual no servidor
    }

    // Getters e Setters atualizados para o nome de campo correto
    public double getHorasDormidas() {
        return horasDormidas;
    }

    public void setHorasDormidas(double horasDormidas) {
        this.horasDormidas = horasDormidas;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}