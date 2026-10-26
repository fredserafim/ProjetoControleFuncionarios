package com.example.shiftsync.DTO;

import com.example.shiftsync.utils.RegrasJornada;

import java.time.LocalDateTime;

public class OcorrenciaTurnoResponse {

    private LocalDateTime inicio;
    private LocalDateTime fim;

    public OcorrenciaTurnoResponse(RegrasJornada.Ocorrencia ocorrencia) {
        this.inicio = ocorrencia.inicio();
        this.fim = ocorrencia.fim();
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }
}
