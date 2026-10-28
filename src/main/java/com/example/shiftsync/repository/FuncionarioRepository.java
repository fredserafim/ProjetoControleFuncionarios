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
import java.util.Collection;

/**
 * Persistência e consultas específicas de funcionários.
 *
 * Além das buscas derivadas, há consultas com bloqueio pessimista para serializar
 * alterações concorrentes e uma consulta SQL nativa que compara CPFs sem máscara.
 */
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario,Long> {

    /** Lista funcionários vinculados ao identificador do departamento. */
    List<Funcionario> findByDepartamento_Id(Long departamentoId);

    /** Lê o funcionário com bloqueio de escrita até o fim da transação chamadora. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select funcionario from Funcionario funcionario where funcionario.id = :id")
    Optional<Funcionario> findByIdForUpdate(@Param("id") Long id);

    /** Compara o CPF persistido após remover caracteres que não sejam dígitos. */
    @Query(value = "select * from funcionario f where "
            + "regexp_replace(coalesce(f.cpf, ''), '[^0-9]', '', 'g') = :cpf",
            nativeQuery = true)
    List<Funcionario> findAllByCpfNormalizado(@Param("cpf") String cpf);

    /** Bloqueia os funcionários selecionados, em ordem de id, para reduzir conflitos entre transações. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select funcionario from Funcionario funcionario "
            + "where funcionario.id in :ids order by funcionario.id")
    List<Funcionario> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
