package com.example.shiftsync.entities;

import java.time.LocalDateTime;

public class Departamento {

    public Departamento(){}

    private Long id;

    private String setor;

    private Turno turno;

    private Funcionario funcionario;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualização;

    private String status;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getSetor(){
        return this.setor;
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

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualização() {
        return dataAtualização;
    }
    public void setDataAtualização(LocalDateTime dataAtualização) {
        this.dataAtualização = dataAtualização;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
