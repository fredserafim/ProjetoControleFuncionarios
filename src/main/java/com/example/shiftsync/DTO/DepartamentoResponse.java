package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Departamento;

import java.time.LocalDateTime;

/** Visão do departamento devolvida pela API, sem retornar a entidade JPA inteira. */
public class DepartamentoResponse {

    public DepartamentoResponse() {}

    public DepartamentoResponse(Long id, String mensagem) {
        this.id = id;
        this.mensagem = mensagem;}

    public DepartamentoResponse(Departamento departamento) {
        this.id = departamento.getId();
        this.departamento = departamento.getDepartamento();
        this.dataCadastro = departamento.getDataCadastro();
        this.dataAtualizacao = departamento.getDataAtualizacao();
        this.status = departamento.getStatus();
    }

    private Long id;
    private String mensagem;
    private String departamento;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private String status;

    public Long getId() {
        return id;}
    public void setId(Long id) {
        this.id = id;}

    public String getMensagem() {
        return mensagem;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;}

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
