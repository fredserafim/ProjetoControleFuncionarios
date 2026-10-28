package com.example.shiftsync.repository;


import com.example.shiftsync.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Operações de persistência de empresas, complementadas por consulta de CNPJ. */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa,Long> {

    /** Busca uma empresa pelo CNPJ e representa ausência com Optional vazio. */
    Optional<Empresa> getEmpresaByCnpj(String Long);
}
