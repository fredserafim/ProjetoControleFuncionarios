package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Turno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistência de modelos de turno.
 *
 * O nome {@code TurnoRopository} é mantido como está no projeto; o método especial
 * usa JPQL e bloqueio pessimista para proteger atualizações concorrentes.
 */
@Repository
public interface TurnoRopository  extends JpaRepository<Turno,Long> {
    //Optional<List<Turno>> getTurnosByHoraExtraIsEmpty();
    //Optional<List<Turno>> getTurnosByDepartamentos_Id(Long departamentosId);

    /** Carrega o turno indicado e solicita ao banco um bloqueio de escrita. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select turno from Turno turno where turno.id = :id")
    java.util.Optional<Turno> findByIdForUpdate(@Param("id") Long id);
}
