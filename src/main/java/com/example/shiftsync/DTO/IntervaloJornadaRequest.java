package com.example.shiftsync.DTO;

import java.time.LocalTime;

/** Horários enviados para configurar uma pausa prevista em um turno. */
public class IntervaloJornadaRequest {

    private LocalTime inicio;
    private LocalTime fim;

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
