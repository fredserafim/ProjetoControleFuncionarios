package com.example.shiftsync.DTO;

/**
 * Indicadores mensais do ponto e das jornadas previstas.
 *
 * As quantidades e durações são separadas para distinguir atraso, falta,
 * pendência e excedente. Horas extras apuradas ficam nulas quando falta uma
 * carga mensal padrão vigente.
 */
public class ResumoMensalPontoResponse {

    private final Long funcionarioId;
    private final String nomeFuncionario;
    private final String mesReferencia;
    private final int minutosTrabalhados;
    private final int minutosPrevistos;
    private final Integer minutosExcedentesPrevistos;
    private final Integer minutosHorasExtrasApuradas;
    private final int quantidadeFaltas;
    private final int quantidadePendencias;
    private final int registrosIncompletos;
    private final int quantidadeAtrasos;
    private final int minutosAtraso;
    private final int quantidadeSaidasAntecipadas;
    private final int minutosSaidasAntecipadas;
    private final int minutosIntervalosExcedentes;
    private final int minutosIntervalosInsuficientes;

    public ResumoMensalPontoResponse(
            Long funcionarioId,
            String nomeFuncionario,
            String mesReferencia,
            int minutosTrabalhados,
            int minutosPrevistos,
            Integer minutosExcedentesPrevistos,
            Integer minutosHorasExtrasApuradas,
            int quantidadeFaltas,
            int quantidadePendencias,
            int registrosIncompletos,
            int quantidadeAtrasos,
            int minutosAtraso,
            int quantidadeSaidasAntecipadas,
            int minutosSaidasAntecipadas,
            int minutosIntervalosExcedentes,
            int minutosIntervalosInsuficientes) {
        this.funcionarioId = funcionarioId;
        this.nomeFuncionario = nomeFuncionario;
        this.mesReferencia = mesReferencia;
        this.minutosTrabalhados = minutosTrabalhados;
        this.minutosPrevistos = minutosPrevistos;
        this.minutosExcedentesPrevistos = minutosExcedentesPrevistos;
        this.minutosHorasExtrasApuradas = minutosHorasExtrasApuradas;
        this.quantidadeFaltas = quantidadeFaltas;
        this.quantidadePendencias = quantidadePendencias;
        this.registrosIncompletos = registrosIncompletos;
        this.quantidadeAtrasos = quantidadeAtrasos;
        this.minutosAtraso = minutosAtraso;
        this.quantidadeSaidasAntecipadas = quantidadeSaidasAntecipadas;
        this.minutosSaidasAntecipadas = minutosSaidasAntecipadas;
        this.minutosIntervalosExcedentes = minutosIntervalosExcedentes;
        this.minutosIntervalosInsuficientes = minutosIntervalosInsuficientes;
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

    public int getMinutosPrevistos() {
        return minutosPrevistos;
    }

    public Integer getMinutosExcedentesPrevistos() {
        return minutosExcedentesPrevistos;
    }

    public Integer getMinutosHorasExtrasApuradas() {
        return minutosHorasExtrasApuradas;
    }

    public int getQuantidadeFaltas() {
        return quantidadeFaltas;
    }

    public int getQuantidadePendencias() {
        return quantidadePendencias;
    }

    public int getRegistrosIncompletos() {
        return registrosIncompletos;
    }

    public int getQuantidadeAtrasos() {
        return quantidadeAtrasos;
    }

    public int getMinutosAtraso() {
        return minutosAtraso;
    }

    public int getQuantidadeSaidasAntecipadas() {
        return quantidadeSaidasAntecipadas;
    }

    public int getMinutosSaidasAntecipadas() {
        return minutosSaidasAntecipadas;
    }

    public int getMinutosIntervalosExcedentes() {
        return minutosIntervalosExcedentes;
    }

    public int getMinutosIntervalosInsuficientes() {
        return minutosIntervalosInsuficientes;
    }
}
