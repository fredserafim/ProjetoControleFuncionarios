package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Turno;

public class DepartamentoRequest {

    public DepartamentoRequest() {
    }

    public DepartamentoRequest(Long id, String departamento, Turno turno, Funcionario funcionario) {
        this.id = id;
        this.departamento = departamento;
        this.turno = turno;
        this.funcionario = funcionario;
        this.status = status;
    }

    private Long id;

    private String departamento;

    private Turno turno;

    private Funcionario funcionario;

    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento() {
        this.departamento = departamento;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
