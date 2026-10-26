package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.IntervaloJornada;

import java.time.LocalTime;

public class IntervaloJornadaResponse {

    private LocalTime inicio;
    private LocalTime fim;

    public IntervaloJornadaResponse(IntervaloJornada intervalo) {
        this.inicio = intervalo.getInicio();
        this.fim = intervalo.getFim();
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFim() {
        return fim;
    }
}
