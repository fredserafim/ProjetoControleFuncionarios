package com.example.shiftsync.entities;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/**
 * Intervalo efetivamente não trabalhado dentro de uma realização de turno.
 *
 * Como é embutível, seus horários são persistidos na tabela de coleção vinculada
 * à realização, em vez de possuir uma tabela de entidade independente.
 */
@Embeddable
public class IntervaloRealizacao {

    private LocalDateTime inicio;
    private LocalDateTime fim;

    public IntervaloRealizacao() {
    }

    /**
     * Cria um intervalo efetivo de pausa.
     *
     * @param inicio instante em que o trabalho foi interrompido.
     * @param fim instante em que o trabalho foi retomado.
     */
    public IntervaloRealizacao(LocalDateTime inicio, LocalDateTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }
}
