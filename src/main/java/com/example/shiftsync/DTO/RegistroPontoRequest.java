package com.example.shiftsync.DTO;

import java.time.LocalDate;

public class RegistroPontoRequest {

    private String cpf;
    private LocalDate data;
    private TipoMarcacaoPonto marcacao;

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
}
