package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_ponto_auditoria")
public class RegistroPontoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "realizacao_turno_id", nullable = false)
    private RealizacaoTurno realizacao;

    @Column(nullable = false)
    private String marcacao;

    @Column(nullable = false)
    private LocalDateTime registradoEm;

    @Column(nullable = false)
    private String registradoPorCpf;

    public Long getId() {
        return id;
    }

    public RealizacaoTurno getRealizacao() {
        return realizacao;
    }

    public void setRealizacao(RealizacaoTurno realizacao) {
        this.realizacao = realizacao;
    }

    public String getMarcacao() {
        return marcacao;
    }

    public void setMarcacao(String marcacao) {
        this.marcacao = marcacao;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public void setRegistradoEm(LocalDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }

    public String getRegistradoPorCpf() {
        return registradoPorCpf;
    }

    public void setRegistradoPorCpf(String registradoPorCpf) {
        this.registradoPorCpf = registradoPorCpf;
    }
}
