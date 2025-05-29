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
    public String senha;

    public Usuario() {} // necessário para o Firebase

    public Usuario(String nome, int idade, String sexo, String imagemPerfil, double peso, double altura, String tipoSanguineo, String email, String senha) {
        this.nome = nome;
        this.idade = idade;
        this.sexo = sexo;
        this.imagemPerfil = imagemPerfil;
        this.peso = peso;
        this.altura = altura;
        this.tipoSanguineo = tipoSanguineo;
        this.email = email;
        this.senha = senha;
    }
}