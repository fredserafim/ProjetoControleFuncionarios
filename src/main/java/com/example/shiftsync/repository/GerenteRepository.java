package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Gerente;
import com.example.shiftsync.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Usa as operações CRUD fornecidas pelo JpaRepository para a entidade Gerente. */
@Repository
public interface GerenteRepository extends JpaRepository<Gerente,Long> {

}
