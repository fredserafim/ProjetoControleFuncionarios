package com.example.shiftsync.entities;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;

/**
 * Par de horários que representa uma pausa prevista no modelo de turno.
 *
 * {@code @Embeddable} indica que o JPA guarda esses valores como parte de outra
 * entidade, sem criar uma identidade ou tabela própria para cada intervalo.
 */
@Embeddable
public class IntervaloJornada {

    private LocalTime inicio;
    private LocalTime fim;

    public IntervaloJornada() {
    }

    /**
     * Cria um intervalo previsto com início e fim.
     *
     * @param inicio horário previsto para começar a pausa.
     * @param fim horário previsto para terminar a pausa.
     */
    public IntervaloJornada(LocalTime inicio, LocalTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalTime inicio) {
        this.inicio = inicio;
    }

    public LocalTime getFim() {
        return fim;
    }

    public void setFim(LocalTime fim) {
        this.fim = fim;
    }
}
