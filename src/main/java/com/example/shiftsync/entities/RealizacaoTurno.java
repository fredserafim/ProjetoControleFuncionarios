package com.example.shiftsync.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_realizacao_atribuicao_inicio_planejado",
        columnNames = {"atribuicao_turno_id", "inicio_planejado"}))
public class RealizacaoTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atribuicao_turno_id", nullable = false)
    private AtribuicaoTurno atribuicao;

    @Column(name = "inicio_planejado", nullable = false)
    private LocalDateTime inicioPlanejado;

    @Column(name = "fim_planejado", nullable = false)
    private LocalDateTime fimPlanejado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRealizacaoTurno status;

    private LocalDateTime inicioReal;

    private LocalDateTime inicioIntervaloReal;

    private LocalDateTime fimIntervaloReal;

    private LocalDateTime fimReal;

    @ElementCollection
    @CollectionTable(
            name = "realizacao_turno_intervalo",
            joinColumns = @JoinColumn(name = "realizacao_turno_id"))
    @OrderColumn(name = "ordem")
    private List<IntervaloRealizacao> intervalosNaoTrabalhados = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime confirmadoEm;

    public Long getId() {
        return id;
    }

    public AtribuicaoTurno getAtribuicao() {
        return atribuicao;
    }

    public void setAtribuicao(AtribuicaoTurno atribuicao) {
        this.atribuicao = atribuicao;
    }

    public LocalDateTime getInicioPlanejado() {
        return inicioPlanejado;
    }

    public void setInicioPlanejado(LocalDateTime inicioPlanejado) {
        this.inicioPlanejado = inicioPlanejado;
    }

    public LocalDateTime getFimPlanejado() {
        return fimPlanejado;
    }

    public void setFimPlanejado(LocalDateTime fimPlanejado) {
        this.fimPlanejado = fimPlanejado;
    }

    public StatusRealizacaoTurno getStatus() {
        return status;
    }

    public void setStatus(StatusRealizacaoTurno status) {
        this.status = status;
    }

    public LocalDateTime getInicioReal() {
        return inicioReal;
    }

    public void setInicioReal(LocalDateTime inicioReal) {
        this.inicioReal = inicioReal;
    }

    public LocalDateTime getFimReal() {
        return fimReal;
    }

    public void setFimReal(LocalDateTime fimReal) {
        this.fimReal = fimReal;
    }

    public LocalDateTime getInicioIntervaloReal() {
        return inicioIntervaloReal;
    }

    public void setInicioIntervaloReal(LocalDateTime inicioIntervaloReal) {
        this.inicioIntervaloReal = inicioIntervaloReal;
    }

    public LocalDateTime getFimIntervaloReal() {
        return fimIntervaloReal;
    }

    public void setFimIntervaloReal(LocalDateTime fimIntervaloReal) {
        this.fimIntervaloReal = fimIntervaloReal;
    }

    public List<IntervaloRealizacao> getIntervalosNaoTrabalhados() {
        return intervalosNaoTrabalhados;
    }

    public void setIntervalosNaoTrabalhados(List<IntervaloRealizacao> intervalosNaoTrabalhados) {
        this.intervalosNaoTrabalhados = intervalosNaoTrabalhados == null
                ? new ArrayList<>() : new ArrayList<>(intervalosNaoTrabalhados);
    }

    public LocalDateTime getConfirmadoEm() {
        return confirmadoEm;
    }

    public void setConfirmadoEm(LocalDateTime confirmadoEm) {
        this.confirmadoEm = confirmadoEm;
    }
}
