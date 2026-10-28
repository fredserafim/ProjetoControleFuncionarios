package com.example.shiftsync.DTO;

import java.time.LocalDate;

/**
 * Dados de identificação da jornada e da próxima marcação do ponto.
 *
 * O cliente não envia horário: a API usa o relógio do servidor, reduzindo a
 * possibilidade de alterar retroativamente o instante informado.
 */
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
