package com.example.shiftsync.DTO;

/** Dados recebidos para alterar somente o status de um funcionário. */
public class AtualizaStatusFuncionarioRequest {

    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
