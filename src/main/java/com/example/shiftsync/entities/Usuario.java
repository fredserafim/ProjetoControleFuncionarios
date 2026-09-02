package com.example.shiftsync.entities;

import java.time.LocalDateTime;

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

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualização;

    private String status;



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


    public LocalDateTime getDataCadastro() {
        return dataCadastro;}
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;}

    public LocalDateTime getDataAtualização() {
        return dataAtualização;}
    public void setDataAtualização(LocalDateTime dataAtualização) {
        this.dataAtualização = dataAtualização;}

    public String getStatus() {
        return status;}
    public void setStatus(String status) {
        this.status = status;}
}
