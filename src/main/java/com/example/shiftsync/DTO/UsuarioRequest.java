package com.example.shiftsync.DTO;

public class UsuarioRequest {

    public UsuarioRequest(){}

    private Long empresa_id;

    private String nome;

    private String cpf;

    private String dataNascimento;

    private String senha;

    public String getNome() {
        return nome;}
    public void setNome(String nome) {
        this.nome = nome;}

    public String getCpf() {
        return cpf;}
    public void setCpf(String cpf) {
        this.cpf = cpf;}

    public String getDataNascimento() {
        return dataNascimento;}
    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;}

    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Long getEmpresa_id() {
        return empresa_id;
    }

    public void setEmpresa_id(Long empresa_id) {
        this.empresa_id = empresa_id;
    }
}
