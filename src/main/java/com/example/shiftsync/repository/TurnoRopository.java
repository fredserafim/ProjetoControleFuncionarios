package com.example.shiftsync.repository;

import com.example.shiftsync.entities.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRopository  extends JpaRepository<Turno,Long> {

    Optional<List<Turno>> findAllByHoraExtraIsNotEmpty();

}
