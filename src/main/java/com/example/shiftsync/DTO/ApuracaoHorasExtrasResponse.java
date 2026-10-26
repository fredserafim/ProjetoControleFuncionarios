package com.example.shiftsync.DTO;

public class ApuracaoHorasExtrasResponse {

    private Long funcionarioId;
    private String nomeFuncionario;
    private String mesReferencia;
    private Integer minutosTrabalhados;
    private Integer minutosCargaPadrao;
    private Integer minutosHorasExtras;
    private int turnosRealizados;
    private int turnosPendentes;
    private int turnosNaoRealizados;
    private String tipoApuracao;
    private String pendencia;

    public ApuracaoHorasExtrasResponse(
            Long funcionarioId,
            String nomeFuncionario,
            String mesReferencia,
            int minutosTrabalhados,
            Integer minutosCargaPadrao,
            Integer minutosHorasExtras,
            int turnosRealizados,
            int turnosPendentes,
            int turnosNaoRealizados,
            String tipoApuracao,
            String pendencia) {
        this.funcionarioId = funcionarioId;
        this.nomeFuncionario = nomeFuncionario;
        this.mesReferencia = mesReferencia;
        this.minutosTrabalhados = minutosTrabalhados;
        this.minutosCargaPadrao = minutosCargaPadrao;
        this.minutosHorasExtras = minutosHorasExtras;
        this.turnosRealizados = turnosRealizados;
        this.turnosPendentes = turnosPendentes;
        this.turnosNaoRealizados = turnosNaoRealizados;
        this.tipoApuracao = tipoApuracao;
        this.pendencia = pendencia;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public int getMinutosTrabalhados() {
        return minutosTrabalhados;
    }

    public Integer getMinutosCargaPadrao() {
        return minutosCargaPadrao;
    }

    public Integer getMinutosHorasExtras() {
        return minutosHorasExtras;
    }

    public int getTurnosRealizados() {
        return turnosRealizados;
    }

    public int getTurnosPendentes() {
        return turnosPendentes;
    }

    public int getTurnosNaoRealizados() {
        return turnosNaoRealizados;
    }

    public String getTipoApuracao() {
        return tipoApuracao;
    }

    public String getPendencia() {
        return pendencia;
    }
}
