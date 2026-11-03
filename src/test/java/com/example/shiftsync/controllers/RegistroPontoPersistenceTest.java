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
import com.example.shiftsync.repository.UsuarioRepository;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.entities.IntervaloJornada;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void persisteERecuperaMarcacoesEHistoricoDeAuditoria() {
        String cpfTeste = String.format(
                "%011d", Math.abs(System.nanoTime()) % 100_000_000_000L);
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Funcionário de teste de ponto");
        funcionario.setCpf(cpfTeste);
        funcionario.setTelefone("48999991234");
        funcionario.setCep("88801000");
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
        Funcionario funcionarioRecuperado =
                funcionarioRepository.findAllByCpfNormalizado(cpfTeste).get(0);
        assertEquals("48999991234", funcionarioRecuperado.getTelefone());
        assertEquals("88801000", funcionarioRecuperado.getCep());
        assertEquals(LocalDateTime.of(2026, 10, 27, 8, 5), recuperada.getInicioReal());
        assertEquals(LocalDateTime.of(2026, 10, 27, 12, 0),
                recuperada.getInicioIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 10, 27, 12, 30),
                recuperada.getFimIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 10, 27, 17, 10), recuperada.getFimReal());
        assertEquals(1, recuperada.getIntervalosNaoTrabalhados().size());
        List<RegistroPontoAuditoria> auditorias =
                auditoriaRepository.findByRealizacao_Id(realizacaoId);
        assertEquals(1, auditorias.size());
        assertEquals("52998224725", auditorias.get(0).getRegistradoPorCpf());
        assertEquals(1, funcionarioRepository.findAllByCpfNormalizado(cpfTeste).size());
    }

    @Test
    void consultaERegistraQuatroHorariosPelaApiERecuperaDoBanco() throws Exception {
        LocalDate data = LocalDate.of(2026, 11, 3);
        String cpfGestor = "52998224725";
        String cpfFuncionario = "11144477735";

        Usuario gestor = new Usuario("Gestor de teste", cpfGestor, "1990-01-01");
        gestor.setStatus("A");
        usuarioRepository.saveAndFlush(gestor);

        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Funcionário de teste API");
        funcionario.setCpf(cpfFuncionario);
        funcionario.setStatus("A");
        funcionarioRepository.saveAndFlush(funcionario);

        Turno turno = new Turno();
        turno.setNome("Jornada administrativa");
        turno.setStatus("A");
        turno.setTipoJornada("DIURNA");
        turno.setTipoEscala("SEMANAL_FIXA");
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(18, 0));
        turno.setDiasSemana(Set.of(DayOfWeek.TUESDAY));
        turno.setIntervalos(List.of(
                new IntervaloJornada(LocalTime.of(12, 0), LocalTime.of(13, 30))));
        turnoRepository.saveAndFlush(turno);

        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(turno);
        atribuicao.setVigenciaInicio(data.atStartOfDay());
        atribuicaoTurnoRepository.saveAndFlush(atribuicao);

        Clock clock = Clock.fixed(
                data.atTime(20, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant(),
                ZoneId.of("America/Sao_Paulo"));
        MockMvc api = MockMvcBuilders.standaloneSetup(new RegistroPontoController(
                        funcionarioRepository,
                        atribuicaoTurnoRepository,
                        realizacaoTurnoRepository,
                        auditoriaRepository,
                        usuarioRepository,
                        clock))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        api.perform(get("/ponto")
                        .param("cpf", cpfFuncionario)
                        .param("data", data.toString())
                        .with(request -> {
                            request.setAttribute("authenticatedCpf", cpfGestor);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFuncionario").value("Funcionário de teste API"));

        registrarPelaApi(api, cpfGestor, cpfFuncionario, data, "ENTRADA", "08:00");
        registrarPelaApi(api, cpfGestor, cpfFuncionario, data, "SAIDA_INTERVALO", "12:00");
        registrarPelaApi(api, cpfGestor, cpfFuncionario, data, "RETORNO_INTERVALO", "13:30");
        registrarPelaApi(api, cpfGestor, cpfFuncionario, data, "SAIDA_FINAL", "18:00");

        entityManager.flush();
        entityManager.clear();

        api.perform(get("/ponto")
                        .param("cpf", cpfFuncionario)
                        .param("data", data.toString())
                        .with(request -> {
                            request.setAttribute("authenticatedCpf", cpfGestor);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entrada").value("2026-11-03T08:00:00"))
                .andExpect(jsonPath("$.saidaIntervalo").value("2026-11-03T12:00:00"))
                .andExpect(jsonPath("$.retornoIntervalo").value("2026-11-03T13:30:00"))
                .andExpect(jsonPath("$.saidaFinal").value("2026-11-03T18:00:00"));

        RealizacaoTurno persistida = realizacaoTurnoRepository
                .findByAtribuicao_IdAndInicioPlanejado(
                        atribuicao.getId(), data.atTime(8, 0))
                .orElseThrow();
        assertEquals(LocalDateTime.of(2026, 11, 3, 8, 0), persistida.getInicioReal());
        assertEquals(LocalDateTime.of(2026, 11, 3, 12, 0),
                persistida.getInicioIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 11, 3, 13, 30),
                persistida.getFimIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 11, 3, 18, 0), persistida.getFimReal());
        assertEquals(4, auditoriaRepository.findByRealizacao_Id(persistida.getId()).size());
    }

    private void registrarPelaApi(
            MockMvc api,
            String cpfGestor,
            String cpfFuncionario,
            LocalDate data,
            String marcacao,
            String horario) throws Exception {
        api.perform(post("/ponto/marcacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "%s",
                                  "data": "%s",
                                  "marcacao": "%s",
                                  "horario": "%s",
                                  "registradoPorCpf": "00000000000"
                                }
                                """.formatted(cpfFuncionario, data, marcacao, horario))
                        .with(request -> {
                            request.setAttribute("authenticatedCpf", cpfGestor);
                            return request;
                        }))
                .andExpect(status().isOk());
    }
}
