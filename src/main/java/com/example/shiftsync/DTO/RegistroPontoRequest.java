package com.example.shiftsync.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dados de identificação da jornada e horário informado para a marcação.
 */
public class RegistroPontoRequest {

    private String cpf;
    private LocalDate data;
    private TipoMarcacaoPonto marcacao;
    private LocalTime horario;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public TipoMarcacaoPonto getMarcacao() {
        return marcacao;
    }

    public void setMarcacao(TipoMarcacaoPonto marcacao) {
        this.marcacao = marcacao;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }
}
