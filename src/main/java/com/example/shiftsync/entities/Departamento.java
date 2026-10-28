package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade persistida que representa uma área organizacional da empresa.
 *
 * {@code @Entity} faz o Hibernate mapear a classe para uma tabela. O identificador
 * é gerado pelo banco, e os métodos de acesso permitem que JPA e as demais camadas
 * leiam e atualizem os campos.
 */
@Entity
public class Departamento {

    public Departamento(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String departamento;

    /** Turnos associados ao departamento conforme o relacionamento configurado no JPA. */
    @OneToMany
    private List<Turno> turno;


    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

    private String status;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public List<Turno> getTurno() {
        return turno;
    }
    public void setTurno(List<Turno> turno) {
        this.turno = turno;
    }

    //public Funcionario getFuncionario() {
     //   return funcionario;
    //}
    //public void setFuncionario(Funcionario funcionario) {
     //   this.funcionario = funcionario;
    //}  devo fazer um ontomany

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    public void setDataAtualizacao(LocalDateTime dataAtualização) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
