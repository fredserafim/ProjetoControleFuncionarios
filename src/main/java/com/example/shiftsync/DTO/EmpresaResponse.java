package com.example.shiftsync.DTO;

/** Resposta resumida de operações de cadastro e atualização de empresa. */
public class EmpresaResponse {

    public EmpresaResponse() {
    }

    private Long id;

    private String mensagem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
