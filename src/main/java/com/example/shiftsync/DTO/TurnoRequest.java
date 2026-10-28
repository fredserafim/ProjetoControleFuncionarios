package com.example.shiftsync.DTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Valores aceitos para cadastrar ou atualizar uma configuração de turno.
 *
 * Os arrays de dias e intervalos descrevem a escala recorrente; campos de carga
 * contratual são opcionais e não substituem a carga efetiva calculada.
 */
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

    private String nome;

    private String tipoJornada;

    private String tipoEscala;

    private List<DayOfWeek> diasSemana = new ArrayList<>();

    private LocalTime horarioInicio;

    private LocalTime horarioFim;

    private List<IntervaloJornadaRequest> intervalos = new ArrayList<>();

    private LocalDate dataReferencia;

    private Integer cargaDiariaContratualMinutos;

    private Integer cargaSemanalContratualMinutos;

    private Integer cargaMensalPadraoMinutos;


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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoJornada() {
        return tipoJornada;
    }

    public void setTipoJornada(String tipoJornada) {
        this.tipoJornada = tipoJornada;
    }

    public String getTipoEscala() {
        return tipoEscala;
    }

    public void setTipoEscala(String tipoEscala) {
        this.tipoEscala = tipoEscala;
    }

    public List<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(List<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana == null ? new ArrayList<>() : new ArrayList<>(diasSemana);
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }

    public List<IntervaloJornadaRequest> getIntervalos() {
        return intervalos;
    }

    public void setIntervalos(List<IntervaloJornadaRequest> intervalos) {
        this.intervalos = intervalos == null
                ? new ArrayList<>() : new ArrayList<>(intervalos);
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public Integer getCargaDiariaContratualMinutos() {
        return cargaDiariaContratualMinutos;
    }

    public void setCargaDiariaContratualMinutos(Integer cargaDiariaContratualMinutos) {
        this.cargaDiariaContratualMinutos = cargaDiariaContratualMinutos;
    }

    public Integer getCargaSemanalContratualMinutos() {
        return cargaSemanalContratualMinutos;
    }

    public void setCargaSemanalContratualMinutos(Integer cargaSemanalContratualMinutos) {
        this.cargaSemanalContratualMinutos = cargaSemanalContratualMinutos;
    }

    public Integer getCargaMensalPadraoMinutos() {
        return cargaMensalPadraoMinutos;
    }

    public void setCargaMensalPadraoMinutos(Integer cargaMensalPadraoMinutos) {
        this.cargaMensalPadraoMinutos = cargaMensalPadraoMinutos;
    }
}
