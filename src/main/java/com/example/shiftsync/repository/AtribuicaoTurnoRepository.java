package com.example.shiftsync.repository;

import com.example.shiftsync.entities.AtribuicaoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AtribuicaoTurnoRepository extends JpaRepository<AtribuicaoTurno, Long> {

    Optional<AtribuicaoTurno> findFirstByFuncionario_IdAndVigenciaFimIsNullOrderByVigenciaInicioDesc(
            Long funcionarioId);

    List<AtribuicaoTurno> findByTurno_IdAndVigenciaFimIsNull(Long turnoId);

    List<AtribuicaoTurno> findByFuncionario_IdOrderByVigenciaInicioAsc(Long funcionarioId);
}
