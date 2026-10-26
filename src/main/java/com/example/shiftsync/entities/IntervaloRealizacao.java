package com.example.shiftsync.entities;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class IntervaloRealizacao {

    private LocalDateTime inicio;
    private LocalDateTime fim;

    public IntervaloRealizacao() {
    }

    public IntervaloRealizacao(LocalDateTime inicio, LocalDateTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }
}
