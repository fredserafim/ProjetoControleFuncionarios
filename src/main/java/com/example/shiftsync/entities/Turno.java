package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Turno {

    public Turno(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long horario;

    private String periodo;

    private Long horaExtra;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

    private String status;

    @ManyToOne
    @JoinColumn(name = "departamentos_id")
    private Departamento departamentos;

    public void setDepartamentos(Departamento departamentos) {
        this.departamentos = departamentos;
    }

    public Departamento getDepartamentos() {
        return departamentos;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getPeriodo(){
        return this.periodo;
    }
    public void setPeriodo(String periodo){
        this.periodo = periodo;
    }

    public Long getHorario(){
        return this.horario;
    }
    public void setHorario(Long horario){
        this.horario = horario;
    }

    public Long getHoraExtra(){
        return this.horaExtra;
    }
    public void setHoraExtra(Long horaExtra) {
        this.horaExtra = horaExtra;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
