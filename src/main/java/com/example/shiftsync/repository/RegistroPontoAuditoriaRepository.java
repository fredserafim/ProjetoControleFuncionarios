package com.example.shiftsync.repository;

import com.example.shiftsync.entities.RegistroPontoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroPontoAuditoriaRepository
        extends JpaRepository<RegistroPontoAuditoria, Long> {

    java.util.List<RegistroPontoAuditoria> findByRealizacao_Id(Long realizacaoId);
}
