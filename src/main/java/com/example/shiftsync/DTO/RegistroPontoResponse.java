package com.example.shiftsync.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resumo diário das marcações persistidas e da jornada que estava planejada.
 *
 * Horários não marcados ficam nulos; minutos trabalhados só são fornecidos
 * quando a jornada está concluída, evitando tratar um cálculo parcial como final.
 */
public class RegistroPontoResponse {

    private final Long funcionarioId;
    private final String nomeFuncionario;
    private final LocalDate data;
    private final LocalDateTime inicioPlanejado;
    private final LocalDateTime fimPlanejado;
    private final LocalDateTime inicioIntervaloPrevisto;
    private final LocalDateTime fimIntervaloPrevisto;
    private final LocalDateTime entrada;
    private final LocalDateTime saidaIntervalo;
    private final LocalDateTime retornoIntervalo;
    private final LocalDateTime saidaFinal;
    private final String status;
    private final Integer minutosTrabalhados;

    public RegistroPontoResponse(
            Long funcionarioId,
            String nomeFuncionario,
            LocalDate data,
            LocalDateTime inicioPlanejado,
            LocalDateTime fimPlanejado,
            LocalDateTime inicioIntervaloPrevisto,
            LocalDateTime fimIntervaloPrevisto,
            LocalDateTime entrada,
            LocalDateTime saidaIntervalo,
            LocalDateTime retornoIntervalo,
            LocalDateTime saidaFinal,
            String status,
            Integer minutosTrabalhados) {
        this.funcionarioId = funcionarioId;
        this.nomeFuncionario = nomeFuncionario;
        this.data = data;
        this.inicioPlanejado = inicioPlanejado;
        this.fimPlanejado = fimPlanejado;
        this.inicioIntervaloPrevisto = inicioIntervaloPrevisto;
        this.fimIntervaloPrevisto = fimIntervaloPrevisto;
        this.entrada = entrada;
        this.saidaIntervalo = saidaIntervalo;
        this.retornoIntervalo = retornoIntervalo;
        this.saidaFinal = saidaFinal;
        this.status = status;
        this.minutosTrabalhados = minutosTrabalhados;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalDateTime getInicioPlanejado() {
        return inicioPlanejado;
    }

    public LocalDateTime getFimPlanejado() {
        return fimPlanejado;
    }

    public LocalDateTime getInicioIntervaloPrevisto() {
        return inicioIntervaloPrevisto;
    }

    public LocalDateTime getFimIntervaloPrevisto() {
        return fimIntervaloPrevisto;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public LocalDateTime getSaidaIntervalo() {
        return saidaIntervalo;
    }

    public LocalDateTime getRetornoIntervalo() {
        return retornoIntervalo;
    }

    public LocalDateTime getSaidaFinal() {
        return saidaFinal;
    }

    public String getStatus() {
        return status;
    }

    public Integer getMinutosTrabalhados() {
        return minutosTrabalhados;
    }
}
