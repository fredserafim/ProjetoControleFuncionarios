package com.example.shiftsync.repository;

import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso às realizações planejadas e ao estado efetivo das jornadas.
 *
 * O JpaRepository oferece persistência e CRUD; os métodos abaixo localizam
 * ocorrências por atribuição, funcionário ou estado.
 */
public interface RealizacaoTurnoRepository extends JpaRepository<RealizacaoTurno, Long> {

    /** Localiza a confirmação única de uma ocorrência planejada de uma atribuição. */
    Optional<RealizacaoTurno> findByAtribuicao_IdAndInicioPlanejado(
            Long atribuicaoId, java.time.LocalDateTime inicioPlanejado);

    /** Lista todas as realizações ligadas às atribuições do funcionário. */
    List<RealizacaoTurno> findByAtribuicao_Funcionario_Id(Long funcionarioId);

    /** Filtra as realizações do funcionário por estado, por exemplo, em andamento. */
    List<RealizacaoTurno> findByAtribuicao_Funcionario_IdAndStatus(
            Long funcionarioId, StatusRealizacaoTurno status);
}
