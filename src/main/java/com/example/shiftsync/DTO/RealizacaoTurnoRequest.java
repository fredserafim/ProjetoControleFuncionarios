package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.StatusRealizacaoTurno;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RealizacaoTurnoRequest {

    private Long atribuicaoTurnoId;
    private LocalDateTime inicioPlanejado;
    private StatusRealizacaoTurno status;
    private LocalDateTime inicioReal;
    private LocalDateTime fimReal;
    private List<IntervaloRealizacaoRequest> intervalosNaoTrabalhados = new ArrayList<>();

    public Long getAtribuicaoTurnoId() {
        return atribuicaoTurnoId;
    }

    public void setAtribuicaoTurnoId(Long atribuicaoTurnoId) {
        this.atribuicaoTurnoId = atribuicaoTurnoId;
    }

    public LocalDateTime getInicioPlanejado() {
        return inicioPlanejado;
    }

    public void setInicioPlanejado(LocalDateTime inicioPlanejado) {
        this.inicioPlanejado = inicioPlanejado;
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

    public List<IntervaloRealizacaoRequest> getIntervalosNaoTrabalhados() {
        return intervalosNaoTrabalhados;
    }

    public void setIntervalosNaoTrabalhados(
            List<IntervaloRealizacaoRequest> intervalosNaoTrabalhados) {
        this.intervalosNaoTrabalhados = intervalosNaoTrabalhados == null
                ? new ArrayList<>() : new ArrayList<>(intervalosNaoTrabalhados);
    }
}
