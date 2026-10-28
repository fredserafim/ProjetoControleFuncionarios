package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Modelo reutilizável de jornada: horários, dias de trabalho, escala e cargas.
 *
 * {@code @ElementCollection} guarda dias e pausas em tabelas auxiliares sem
 * transformá-los em entidades independentes. Horários usam tipos temporais
 * adequados: {@link LocalTime} para hora do dia e {@link LocalDate} como
 * referência de ciclo, como na escala 12x36.
 */
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

    private String nome;

    private String tipoJornada;

    private String tipoEscala;

    private LocalTime horarioInicio;

    private LocalTime horarioFim;

    private LocalDate dataReferencia;

    /** Carga efetiva calculada a partir da duração e pausas do turno. */
    private Integer cargaEfetivaMinutos;

    private Integer cargaSemanalPrevistaMinutos;

    private Integer cargaDiariaContratualMinutos;

    private Integer cargaSemanalContratualMinutos;

    private Integer cargaMensalPadraoMinutos;

    /** Dias da semana em que a escala semanal fixa prevê trabalho. */
    @ElementCollection
    @CollectionTable(name = "turno_dia_semana", joinColumns = @JoinColumn(name = "turno_id"))
    @Column(name = "dia_semana", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> diasSemana = new HashSet<>();

    /** Pausas previstas, ordenadas para serem calculadas dentro do plantão. */
    @ElementCollection
    @CollectionTable(name = "turno_intervalo", joinColumns = @JoinColumn(name = "turno_id"))
    @OrderColumn(name = "ordem")
    private List<IntervaloJornada> intervalos = new ArrayList<>();

    public Set<Usuario> getUsuario() {
        return usuarios;
    }

    public void setUsuario(Set<Usuario> usuario) {
        this.usuarios = usuario;
    }

    /** Usuários associados no relacionamento muitos-para-muitos já existente. */
    @ManyToMany(mappedBy = "turno" )

    private Set<Usuario> usuarios =  new HashSet<>();

    /** Relação legada de turno para turno, mantida para compatibilidade do modelo. */
    @ManyToOne
    @JoinColumn(name = "turno_id")
    private Turno turno;

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
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

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public Integer getCargaEfetivaMinutos() {
        return cargaEfetivaMinutos;
    }

    public void setCargaEfetivaMinutos(Integer cargaEfetivaMinutos) {
        this.cargaEfetivaMinutos = cargaEfetivaMinutos;
    }

    public Integer getCargaSemanalPrevistaMinutos() {
        return cargaSemanalPrevistaMinutos;
    }

    public void setCargaSemanalPrevistaMinutos(Integer cargaSemanalPrevistaMinutos) {
        this.cargaSemanalPrevistaMinutos = cargaSemanalPrevistaMinutos;
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

    public Set<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(Set<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana == null ? new HashSet<>() : new HashSet<>(diasSemana);
    }

    public List<IntervaloJornada> getIntervalos() {
        return intervalos;
    }

    public void setIntervalos(List<IntervaloJornada> intervalos) {
        this.intervalos = intervalos == null ? new ArrayList<>() : new ArrayList<>(intervalos);
    }
}
