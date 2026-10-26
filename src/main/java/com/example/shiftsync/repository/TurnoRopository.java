package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Turno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnoRopository  extends JpaRepository<Turno,Long> {
    //Optional<List<Turno>> getTurnosByHoraExtraIsEmpty();
    //Optional<List<Turno>> getTurnosByDepartamentos_Id(Long departamentosId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select turno from Turno turno where turno.id = :id")
    java.util.Optional<Turno> findByIdForUpdate(@Param("id") Long id);
}
