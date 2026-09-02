package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Turno;

public class DepartamentoRequest {

    public DepartamentoRequest() {
    }

    public DepartamentoRequest(Long id, String setor, Turno turno, Funcionario funcionario) {
        this.id = id;
        this.setor = setor;
        this.turno = turno;
        this.funcionario = funcionario;
    }

    private Long id;

    private String setor;

    private Turno turno;

    private Funcionario funcionario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }
}
