package com.example.shiftsync.DTO;

import com.example.shiftsync.utils.RegrasJornada;

import java.time.LocalDateTime;

/** Intervalo de uma jornada recorrente devolvido por consulta de ocorrências. */
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
