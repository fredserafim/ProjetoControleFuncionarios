package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.StatusRealizacaoTurno;

import java.time.LocalDateTime;

public class RealizacaoTurnoResponse {

    private Long id;
    private Long atribuicaoTurnoId;
    private Long turnoId;
    private String nomeTurno;
    private LocalDateTime inicioPlanejado;
    private LocalDateTime fimPlanejado;
    private StatusRealizacaoTurno status;
    private LocalDateTime inicioReal;
    private LocalDateTime fimReal;
    private Integer minutosTrabalhados;
    private LocalDateTime confirmadoEm;

    public RealizacaoTurnoResponse(RealizacaoTurno realizacao, Integer minutosTrabalhados) {
        this.id = realizacao.getId();
        this.atribuicaoTurnoId = realizacao.getAtribuicao().getId();
        this.turnoId = realizacao.getAtribuicao().getTurno().getId();
        this.nomeTurno = realizacao.getAtribuicao().getTurno().getNome();
        this.inicioPlanejado = realizacao.getInicioPlanejado();
        this.fimPlanejado = realizacao.getFimPlanejado();
        this.status = realizacao.getStatus();
        this.inicioReal = realizacao.getInicioReal();
        this.fimReal = realizacao.getFimReal();
        this.minutosTrabalhados = minutosTrabalhados;
        this.confirmadoEm = realizacao.getConfirmadoEm();
    }

    public Long getId() {
        return id;
    }

    public Long getAtribuicaoTurnoId() {
        return atribuicaoTurnoId;
    }

    public Long getTurnoId() {
        return turnoId;
    }

    public String getNomeTurno() {
        return nomeTurno;
    }

    public LocalDateTime getInicioPlanejado() {
        return inicioPlanejado;
    }

    public LocalDateTime getFimPlanejado() {
        return fimPlanejado;
    }

    public StatusRealizacaoTurno getStatus() {
        return status;
    }

    public LocalDateTime getInicioReal() {
        return inicioReal;
    }

    public LocalDateTime getFimReal() {
        return fimReal;
    }

    public Integer getMinutosTrabalhados() {
        return minutosTrabalhados;
    }

    public LocalDateTime getConfirmadoEm() {
        return confirmadoEm;
    }
}
