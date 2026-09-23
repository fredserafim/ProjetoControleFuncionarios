package com.example.shiftsync.DTO;

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
