package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Turno;

import java.time.LocalDateTime;

public class TurnoConsultaResponse {

    public TurnoConsultaResponse() {
    }


    public TurnoConsultaResponse(Turno turno) {
        this.horario = turno.getHorario();
        this.periodo = turno.getPeriodo();
        this.horaExtra = turno.getHoraExtra();
        this.dataCadastro = turno.getDataCadastro();
        this.dataAtualizacao = turno.getDataAtualizacao();
        this.status = turno.getStatus();
        this.id = turno.getId();
    }

    private Long horario;

    private String periodo;

    private Long horaExtra;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

    private String status;

    private Long id;

    public Long getHorario() {
        return horario;
    }

    public void setHorario(Long horario) {
        this.horario = horario;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Long getHoraExtra() {
        return horaExtra;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
