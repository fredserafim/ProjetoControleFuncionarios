package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.util.List;

/**
 * Cadastro persistido de uma empresa e de seus dados de identificação.
 *
 * Os campos marcados como únicos são protegidos também pelo banco. A coleção de
 * usuários é o lado inverso da relação: {@code mappedBy} aponta para o atributo
 * {@code empresa} mantido em {@link Usuario}.
 */
@Entity
public class Empresa{
    public Empresa() {
    }



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    private String razaoSocial;

    private String nomeFantasia;

    /** Número de CNPJ, com unicidade garantida pelo banco de dados. */
    @Column(unique = true)
    private String cnpj;

    @Column(unique = true)
    private String incricaoEstadual;

    /** Usuários vinculados à empresa; o lado dono da associação fica em Usuario. */
    @OneToMany(mappedBy = "empresa")
    private List<Usuario> usuarios;

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getIncricaoEstadual() {
        return incricaoEstadual;
    }

    public void setIncricaoEstadual(String incricaoEstadual) {
        this.incricaoEstadual = incricaoEstadual;
    }
}
