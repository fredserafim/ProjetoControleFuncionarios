package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AtribuicaoTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @Column(nullable = false)
    private LocalDateTime vigenciaInicio;

    private LocalDateTime vigenciaFim;

    public Long getId() {
        return id;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public LocalDateTime getVigenciaInicio() {
        return vigenciaInicio;
    }

    public void setVigenciaInicio(LocalDateTime vigenciaInicio) {
        this.vigenciaInicio = vigenciaInicio;
    }

    public LocalDateTime getVigenciaFim() {
        return vigenciaFim;
    }

    public void setVigenciaFim(LocalDateTime vigenciaFim) {
        this.vigenciaFim = vigenciaFim;
    }
}
