package com.example.shiftsync.DTO;

public class LoginResponse {

    public LoginResponse(){}

    private String mensagem;

    private String token;

    public String getMensagem() {
        return mensagem;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
