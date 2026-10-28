package com.example.shiftsync.DTO;

import java.time.LocalDateTime;

/** Pausa efetiva informada ao confirmar uma realização de turno pelo fluxo legado. */
public class IntervaloRealizacaoRequest {

    private LocalDateTime inicio;
    private LocalDateTime fim;

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
