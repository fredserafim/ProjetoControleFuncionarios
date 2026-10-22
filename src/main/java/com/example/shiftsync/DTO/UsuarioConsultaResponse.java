package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Usuario;

import java.time.LocalDateTime;

public class UsuarioConsultaResponse {

    public UsuarioConsultaResponse(){}
    public UsuarioConsultaResponse(Usuario usuario){
        this.cpf = usuario.getCpf();
        this.nome = usuario.getNome();
        this.dataNascimento = usuario.getDataNascimento();
        this.dataCadastro = usuario.getDataCadastro();
        this.dataAtualizacao = usuario.getDataAtualizacao();
        this.id = usuario.getId();
        this.status = usuario.getStatus();
        if (usuario.getEmpresa()!=null){
            this.empresa_id = usuario.getEmpresa().getId();
            this.razaoSocialEmpresa = usuario.getEmpresa().getRazaoSocial();
        }

    }

    private Long empresa_id;

    private String nome;

    private String cpf;

    private String dataNascimento;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

    private String razaoSocialEmpresa;

    private Long id;

    private String status;



    public Long getEmpresa_id() {
        return empresa_id;
    }

    public void setEmpresa_id(Long empresa_id) {
        this.empresa_id = empresa_id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
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

    public String getRazaoSocialEmpresa() {
        return razaoSocialEmpresa;
    }

    public void setRazaoSocialEmpresa(String razaoSocialEmpresa) {
        this.razaoSocialEmpresa = razaoSocialEmpresa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
