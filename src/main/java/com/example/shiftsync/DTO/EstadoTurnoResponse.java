package com.example.shiftsync.DTO;

import java.time.LocalDate;

/** Resultado da consulta que informa se um turno prevê trabalho em uma data. */
public class EstadoTurnoResponse {

    private LocalDate data;
    private boolean trabalha;
    private String estado;

    public EstadoTurnoResponse(LocalDate data, boolean trabalha) {
        this.data = data;
        this.trabalha = trabalha;
        this.estado = trabalha ? "TRABALHO" : "DESCANSO";
    }

    public LocalDate getData() {
        return data;
    }

    public boolean isTrabalha() {
        return trabalha;
    }

    public String getEstado() {
        return estado;
    }
}
