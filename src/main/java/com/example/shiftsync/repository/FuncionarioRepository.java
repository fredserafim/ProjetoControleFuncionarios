package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario,Long> {

    List<Funcionario> findByDepartamento_Id(Long departamentoId);
}
