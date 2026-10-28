package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.Turno;

import java.time.LocalDateTime;

/**
 * Ocorrência planejada de uma atribuição, possivelmente associada a uma confirmação.
 *
 * Permite ao cliente comparar a programação com o estado da realização sem
 * expor objetos JPA completos.
 */
public class TurnoPlanejadoResponse {

    private Long atribuicaoTurnoId;
    private Long turnoId;
    private String nomeTurno;
    private LocalDateTime inicioPlanejado;
    private LocalDateTime fimPlanejado;
    private String status;
    private Long realizacaoId;

    public TurnoPlanejadoResponse(
            Long atribuicaoTurnoId,
            Turno turno,
            LocalDateTime inicioPlanejado,
            LocalDateTime fimPlanejado,
            RealizacaoTurno realizacao) {
        this.atribuicaoTurnoId = atribuicaoTurnoId;
        this.turnoId = turno.getId();
        this.nomeTurno = turno.getNome();
        this.inicioPlanejado = inicioPlanejado;
        this.fimPlanejado = fimPlanejado;
        this.status = realizacao == null ? "PENDENTE" : realizacao.getStatus().name();
        this.realizacaoId = realizacao == null ? null : realizacao.getId();
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

    public String getStatus() {
        return status;
    }

    public Long getRealizacaoId() {
        return realizacaoId;
    }
}
