package com.example.shiftsync.repository;

import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RealizacaoTurnoRepository extends JpaRepository<RealizacaoTurno, Long> {

    Optional<RealizacaoTurno> findByAtribuicao_IdAndInicioPlanejado(
            Long atribuicaoId, java.time.LocalDateTime inicioPlanejado);

    List<RealizacaoTurno> findByAtribuicao_Funcionario_Id(Long funcionarioId);

    List<RealizacaoTurno> findByAtribuicao_Funcionario_IdAndStatus(
            Long funcionarioId, StatusRealizacaoTurno status);
}
