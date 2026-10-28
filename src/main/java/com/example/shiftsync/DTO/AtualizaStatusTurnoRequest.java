package com.example.shiftsync.DTO;

/** Dados recebidos para alterar somente o status de um turno. */
public class AtualizaStatusTurnoRequest {

    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
