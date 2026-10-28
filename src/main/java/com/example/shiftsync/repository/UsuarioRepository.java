package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Persistência de usuários e consulta pelo CPF usado no login. */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    /** Procura a conta cujo CPF corresponda exatamente ao valor recebido. */
    Optional<Usuario> findByCpf(String cpf);

}
