package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario,Long> {

    List<Funcionario> findByDepartamento_Id(Long departamentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select funcionario from Funcionario funcionario where funcionario.id = :id")
    Optional<Funcionario> findByIdForUpdate(@Param("id") Long id);
}
