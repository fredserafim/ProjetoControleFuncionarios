package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Consultas padrão e busca sem diferenciar maiúsculas de minúsculas para departamentos. */
@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento,Long> {

    /** Procura um departamento pelo nome, ignorando diferenças entre maiúsculas e minúsculas. */
    Optional<Departamento> findByDepartamentoIgnoreCase(String departamento);

}
