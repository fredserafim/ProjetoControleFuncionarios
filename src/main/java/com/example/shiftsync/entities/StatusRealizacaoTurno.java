package com.example.shiftsync.entities;

/**
 * Situação persistida de uma ocorrência de turno.
 *
 * {@code EM_ANDAMENTO} representa uma jornada com marcações parciais,
 * {@code REALIZADO} indica conclusão e {@code NAO_REALIZADO} registra a decisão
 * administrativa disponível no fluxo legado de confirmação.
 */
public enum StatusRealizacaoTurno {
    REALIZADO,
    EM_ANDAMENTO,
    NAO_REALIZADO
}
