package com.example.shiftsync.DTO;

public class FuncionarioRequest {

    public FuncionarioRequest() {
    }

    public FuncionarioRequest(Long id, String nome, String departamento,
         String turno, String funcao,String cpf, String telefone, String endereco) {
        this.id = id;
        this.nome = nome;
        this.departamento = departamento;
        this.turno = turno;
        this.funcao = funcao;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    

    private Long id;

    private String nome;

    private String cpf;

    private String telefone;

    private String endereco;

    private Long departamentoId;

    // Retained to accept requests from clients that still submit the department name.
    private String departamento;

    private String turno;

    private String funcao;

    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public Long getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Long departamentoId) {
        this.departamentoId = departamentoId;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
