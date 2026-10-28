package com.example.shiftsync.DTO;

/** Dados do gerente expostos pela API após uma operação de cadastro. */
public class GerenteResponse {

    public GerenteResponse() {
    }

    public GerenteResponse(Long id, String mensagem) {
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
