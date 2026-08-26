package com.example.shiftsync.entities;

public class Usuario {

    public  Usuario(){
        this.id = 1L;
    }
    public Usuario(String nome, String cpf, String dataNascimento){
        this.id = 1000L;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
    }

    private Long id;

    private String nome;

    private String cpf;

    private String dataNascimento;

    public Long getId(){
         return this.id;}
    public void setId(Long id) {
        this.id = id;}

    public String getNome(){
        return this.nome;}
    public void setNome(String nome){
        this.nome = nome;}

    public String getCpf(){
        return this.cpf;}
    public void setCpf(String cpf){
        this.cpf = cpf;}

    public String getDataNascimento(){
        return this.dataNascimento;}
    public void setDataNascimento(String dataNascimento){
        this.dataNascimento = dataNascimento;}




}
