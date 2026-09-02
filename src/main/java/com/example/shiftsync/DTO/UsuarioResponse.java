package com.example.shiftsync.DTO;

import java.security.PrivateKey;

public class UsuarioResponse {

    public UsuarioResponse(){}

    public UsuarioResponse(Long id, String mensagem) {
        this.id = id;
        this.mensagem = mensagem;}

    private Long id;
    private String mensagem;

    public Long getId() {
        return id;}
    public void setId(Long id) {
        this.id = id;}

    public String getMensagem() {
        return mensagem;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;}
}
