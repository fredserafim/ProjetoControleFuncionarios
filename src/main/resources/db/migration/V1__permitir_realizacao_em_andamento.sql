ALTER TABLE realizacao_turno
    DROP CONSTRAINT IF EXISTS realizacao_turno_status_check;

ALTER TABLE realizacao_turno
    ADD CONSTRAINT realizacao_turno_status_check
    CHECK (status IN ('REALIZADO', 'EM_ANDAMENTO', 'NAO_REALIZADO'));
