package com.example.shiftsync.DTO;

public class TurnoRequest {

    public TurnoRequest() {
    }

    public TurnoRequest(Long horario, String periodo, Long horaExtra,String status) {
        this.id = id;
        this.horario = horario;
        this.periodo = periodo;
        this.horaExtra = horaExtra;
        this.status = status;
    }

    private Long id;

    private Long horario;

    private String periodo;

    private Long horaExtra;

    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
