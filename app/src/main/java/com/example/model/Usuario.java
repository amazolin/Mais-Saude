package com.example.model;

public class Usuario {
    public String nome;
    public int idade;
    public String sexo;
    public String imagemPerfil;
    public double peso;
    public double altura;
    public String tipoSanguineo;
    public String email;
    //public String senha;
    public long xp;
    public long ultimoRegistroSonoTimestamp;

    public Usuario() {} // necessário para o Firebase

    public Usuario(String nome, String email, String imagemPerfil) { // COnstrutor para login com os dados do Google
        this.nome = nome;
        this.email = email;
        this.imagemPerfil = imagemPerfil;
        this.idade = 0;
        this.sexo = "Não informado"; // Valor padrão
        this.peso = 0.0; // Valor padrão
        this.altura = 0.0; // Valor padrão
        this.tipoSanguineo = "Não informado"; // Valor padrão
    }

    public Usuario(String nome, int idade, String sexo, String imagemPerfil, double peso, double altura, String tipoSanguineo, String email) {
        this.nome = nome;
        this.idade = idade;
        this.sexo = sexo;
        this.imagemPerfil = imagemPerfil;
        this.peso = peso;
        this.altura = altura;
        this.tipoSanguineo = tipoSanguineo;
        this.email = email;
        //this.senha = senha;
        this.xp = 0;
        this.ultimoRegistroSonoTimestamp = 0;
    }
}