package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.IntervaloRealizacao;
import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.RegistroPontoAuditoria;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.RealizacaoTurnoRepository;
import com.example.shiftsync.repository.RegistroPontoAuditoriaRepository;
import com.example.shiftsync.repository.TurnoRopository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de integração que verifica o mapeamento e a persistência real do ponto.
 *
 * Usa o contexto Spring e EntityManager para confirmar que as marcações e a
 * auditoria são recuperadas do banco dentro da transação do teste.
 */
@SpringBootTest
@Transactional
class RegistroPontoPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private TurnoRopository turnoRepository;

    @Autowired
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Autowired
    private RealizacaoTurnoRepository realizacaoTurnoRepository;

    @Autowired
    private RegistroPontoAuditoriaRepository auditoriaRepository;

    @Test
    void persisteERecuperaMarcacoesEHistoricoDeAuditoria() {
        String cpfTeste = String.format(
                "%011d", Math.abs(System.nanoTime()) % 100_000_000_000L);
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Funcionário de teste de ponto");
        funcionario.setCpf(cpfTeste);
        funcionario.setStatus("A");
        funcionarioRepository.saveAndFlush(funcionario);

        Turno turno = new Turno();
        turno.setNome("Jornada de teste");
        turnoRepository.saveAndFlush(turno);

        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(turno);
        atribuicao.setVigenciaInicio(LocalDateTime.of(2026, 1, 1, 0, 0));
        atribuicaoTurnoRepository.saveAndFlush(atribuicao);

        RealizacaoTurno realizacao = new RealizacaoTurno();
        realizacao.setAtribuicao(atribuicao);
        realizacao.setInicioPlanejado(LocalDateTime.of(2026, 10, 27, 8, 0));
        realizacao.setFimPlanejado(LocalDateTime.of(2026, 10, 27, 17, 0));
        realizacao.setInicioReal(LocalDateTime.of(2026, 10, 27, 8, 5));
        realizacao.setInicioIntervaloReal(LocalDateTime.of(2026, 10, 27, 12, 0));
        realizacao.setFimIntervaloReal(LocalDateTime.of(2026, 10, 27, 12, 30));
        realizacao.setFimReal(LocalDateTime.of(2026, 10, 27, 17, 10));
        realizacao.setIntervalosNaoTrabalhados(List.of(new IntervaloRealizacao(
                realizacao.getInicioIntervaloReal(), realizacao.getFimIntervaloReal())));
        realizacao.setStatus(StatusRealizacaoTurno.REALIZADO);
        realizacao.setConfirmadoEm(LocalDateTime.of(2026, 10, 27, 17, 10));
        realizacaoTurnoRepository.saveAndFlush(realizacao);

        RegistroPontoAuditoria auditoria = new RegistroPontoAuditoria();
        auditoria.setRealizacao(realizacao);
        auditoria.setMarcacao("SAIDA_FINAL");
        auditoria.setRegistradoEm(LocalDateTime.of(2026, 10, 27, 17, 10));
        auditoria.setRegistradoPorCpf("52998224725");
        auditoriaRepository.saveAndFlush(auditoria);

        Long realizacaoId = realizacao.getId();
        entityManager.clear();

        RealizacaoTurno recuperada = realizacaoTurnoRepository.findById(realizacaoId)
                .orElseThrow();
        assertEquals(LocalDateTime.of(2026, 10, 27, 8, 5), recuperada.getInicioReal());
        assertEquals(LocalDateTime.of(2026, 10, 27, 12, 0),
                recuperada.getInicioIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 10, 27, 12, 30),
                recuperada.getFimIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 10, 27, 17, 10), recuperada.getFimReal());
        assertEquals(1, recuperada.getIntervalosNaoTrabalhados().size());
        assertEquals(1, auditoriaRepository.findByRealizacao_Id(realizacaoId).size());
        assertEquals(1, funcionarioRepository.findAllByCpfNormalizado(cpfTeste).size());
    }
}
