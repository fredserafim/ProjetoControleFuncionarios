package com.example.shiftsync.DTO;

import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Departamento;

import java.time.LocalDateTime;

public class FuncionarioResponse {

    public FuncionarioResponse() {
    }

    public FuncionarioResponse(Long id, String mensagem) {
        this.id = id;
        this.mensagem = mensagem;
    }

    public FuncionarioResponse(Long id, String mensagem, String cpf, String telefone, String endereco) {
        this(id, mensagem);
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public FuncionarioResponse(Long id, String mensagem, String cpf, String telefone, String endereco,
                               Departamento departamento) {
        this(id, mensagem, cpf, telefone, endereco);
        if (departamento != null) {
            this.departamentoId = departamento.getId();
            this.departamento = departamento.getDepartamento();
        }
    }

    private Long id;
    private String mensagem;
    private String cpf;
    private String telefone;
    private String endereco;
    private String nome;
    private String funcao;
    private String turno;
    private Long departamentoId;
    private String departamento;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private String status;

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

    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public FuncionarioResponse(Funcionario funcionario) {
        this.id = funcionario.getId();
        this.nome = funcionario.getNome();
        this.cpf = funcionario.getCpf();
        this.telefone = funcionario.getTelefone();
        this.endereco = funcionario.getEndereco();
        this.funcao = funcionario.getFuncao();
        this.turno = funcionario.getTurno();
        this.dataCadastro = funcionario.getDataCadastro();
        this.dataAtualizacao = funcionario.getDataAtualizacao();
        this.status = funcionario.getStatus();
        if (funcionario.getDepartamento() != null) {
            this.departamentoId = funcionario.getDepartamento().getId();
            this.departamento = funcionario.getDepartamento().getDepartamento();
        }
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFuncao() {
        return funcao;
    }
    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getTurno() {
        return turno;
    }
    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Long getDepartamentoId() {
        return departamentoId;
    }
    public void setDepartamentoId(Long departamentoId) {
        this.departamentoId = departamentoId;
    }

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
