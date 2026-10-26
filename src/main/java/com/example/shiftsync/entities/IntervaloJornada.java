package com.example.shiftsync.entities;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public class IntervaloJornada {

    private LocalTime inicio;
    private LocalTime fim;

    public IntervaloJornada() {
    }

    public IntervaloJornada(LocalTime inicio, LocalTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalTime inicio) {
        this.inicio = inicio;
    }

    public LocalTime getFim() {
        return fim;
    }

    public void setFim(LocalTime fim) {
        this.fim = fim;
    }
}
