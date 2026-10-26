package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Turno;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        this.nome = turno.getNome();
        this.tipoJornada = turno.getTipoJornada();
        this.tipoEscala = turno.getTipoEscala();
        this.diasSemana = List.copyOf(turno.getDiasSemana());
        this.horarioInicio = turno.getHorarioInicio();
        this.horarioFim = turno.getHorarioFim();
        this.intervalos = turno.getIntervalos().stream()
                .map(IntervaloJornadaResponse::new)
                .toList();
        this.dataReferencia = turno.getDataReferencia();
        this.cargaEfetivaMinutos = turno.getCargaEfetivaMinutos();
        this.cargaSemanalPrevistaMinutos = turno.getCargaSemanalPrevistaMinutos();
        this.cargaDiariaContratualMinutos = turno.getCargaDiariaContratualMinutos();
        this.cargaSemanalContratualMinutos = turno.getCargaSemanalContratualMinutos();
        this.cargaMensalPadraoMinutos = turno.getCargaMensalPadraoMinutos();
    }

    private Long horario;

    private String periodo;

    private Long horaExtra;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

    private String status;

    private Long id;

    private String nome;

    private String tipoJornada;

    private String tipoEscala;

    private List<DayOfWeek> diasSemana;

    private LocalTime horarioInicio;

    private LocalTime horarioFim;

    private List<IntervaloJornadaResponse> intervalos;

    private LocalDate dataReferencia;

    private Integer cargaEfetivaMinutos;

    private Integer cargaSemanalPrevistaMinutos;

    private Integer cargaDiariaContratualMinutos;

    private Integer cargaSemanalContratualMinutos;

    private Integer cargaMensalPadraoMinutos;

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

    public String getNome() {
        return nome;
    }

    public String getTipoJornada() {
        return tipoJornada;
    }

    public String getTipoEscala() {
        return tipoEscala;
    }

    public List<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public List<IntervaloJornadaResponse> getIntervalos() {
        return intervalos;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public Integer getCargaEfetivaMinutos() {
        return cargaEfetivaMinutos;
    }

    public Integer getCargaSemanalPrevistaMinutos() {
        return cargaSemanalPrevistaMinutos;
    }

    public Integer getCargaDiariaContratualMinutos() {
        return cargaDiariaContratualMinutos;
    }

    public Integer getCargaSemanalContratualMinutos() {
        return cargaSemanalContratualMinutos;
    }

    public Integer getCargaMensalPadraoMinutos() {
        return cargaMensalPadraoMinutos;
    }
}
