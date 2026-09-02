package com.example.shiftsync.entities;

import java.time.LocalDateTime;

public class Turno {

    public Turno(){}

    private Long id;

    private Long horario;

    private String periodo;

    private Long horaExtra;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualização;

    private String status;

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
