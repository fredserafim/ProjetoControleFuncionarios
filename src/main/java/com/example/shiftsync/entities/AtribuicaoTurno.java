package com.example.shiftsync.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registra qual turno foi atribuído a um funcionário e durante qual período.
 *
 * A vigência histórica permite que uma mudança de jornada não apague a
 * atribuição anterior. O fim nulo representa a atribuição que ainda está ativa.
 * O JPA mapeia cada relação {@code ManyToOne} para uma chave estrangeira.
 */
@Entity
public class AtribuicaoTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Funcionário a quem esta vigência de turno pertence. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    /** Modelo de jornada aplicado durante a vigência desta atribuição. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    /** Primeiro instante em que esta atribuição pode ser usada. */
    @Column(nullable = false)
    private LocalDateTime vigenciaInicio;

    /** Instante final exclusivo; nulo indica que a atribuição continua vigente. */
    private LocalDateTime vigenciaFim;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
