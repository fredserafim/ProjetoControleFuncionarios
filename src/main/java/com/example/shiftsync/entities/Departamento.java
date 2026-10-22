package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Departamento {

    public Departamento(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String departamento;

    @OneToMany
    private List<Turno> turno;


    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

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

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public List<Turno> getTurno() {
        return turno;
    }
    public void setTurno(List<Turno> turno) {
        this.turno = turno;
    }

    //public Funcionario getFuncionario() {
     //   return funcionario;
    //}
    //public void setFuncionario(Funcionario funcionario) {
     //   this.funcionario = funcionario;
    //}  devo fazer um ontomany

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    public void setDataAtualizacao(LocalDateTime dataAtualização) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
