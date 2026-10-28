package com.example.shiftsync.repository;

import com.example.shiftsync.entities.RegistroPontoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persiste eventos de auditoria e consulta as marcações de uma realização específica. */
public interface RegistroPontoAuditoriaRepository
        extends JpaRepository<RegistroPontoAuditoria, Long> {

    /** Retorna as marcações de auditoria relacionadas à realização informada. */
    java.util.List<RegistroPontoAuditoria> findByRealizacao_Id(Long realizacaoId);
}
