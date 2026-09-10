package com.example.shiftsync.DTO;

public class LoginRequest {

    public LoginRequest(){}


    private String login;
    private String senha;


    public String getSenha() {
        return senha;}
    public void setSenha(String senha) {
        this.senha = senha;}

    public String getLogin() {
        return login;}
    public void setLogin(String login) {
        this.login = login;}
}
