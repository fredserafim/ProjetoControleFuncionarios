package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Turno;

public class FuncionarioRequest {

    public FuncionarioRequest() {
    }

    public FuncionarioRequest(Long id, String nome, Departamento departamento, Turno turno, String funcao) {
        this.id = id;
        this.nome = nome;
        this.departamento = departamento;
        this.turno = turno;
        this.funcao = funcao;
        this.status = status;
    }

    private Long id;

    private String nome;

    private Departamento departamento;

    private Turno turno;

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

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
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
