package com.example.shiftsync.DTO;

/** Dados recebidos para alterar somente o status de uma conta de usuário. */
public class AtualizaStatusUsuarioRequest {

    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
