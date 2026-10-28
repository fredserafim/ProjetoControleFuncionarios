package com.example.shiftsync.repository;

import com.example.shiftsync.entities.AtribuicaoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Consultas de persistência para as vigências de turnos dos funcionários.
 *
 * O Spring Data implementa os métodos a partir dos nomes; os sublinhados
 * atravessam relacionamentos, como funcionario.id e turno.id.
 */
@Repository
public interface AtribuicaoTurnoRepository extends JpaRepository<AtribuicaoTurno, Long> {

    /** Busca a atribuição ainda ativa mais recente do funcionário. */
    Optional<AtribuicaoTurno> findFirstByFuncionario_IdAndVigenciaFimIsNullOrderByVigenciaInicioDesc(
            Long funcionarioId);

    /** Lista atribuições ativas que usam o turno informado. */
    List<AtribuicaoTurno> findByTurno_IdAndVigenciaFimIsNull(Long turnoId);

    /** Lista o histórico completo de atribuições que usam o turno. */
    List<AtribuicaoTurno> findByTurno_Id(Long turnoId);

    /** Recupera o histórico do funcionário em ordem crescente de início de vigência. */
    List<AtribuicaoTurno> findByFuncionario_IdOrderByVigenciaInicioAsc(Long funcionarioId);
}
