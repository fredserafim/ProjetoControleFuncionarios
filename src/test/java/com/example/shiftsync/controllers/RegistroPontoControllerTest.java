package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.IntervaloJornada;
import com.example.shiftsync.entities.IntervaloRealizacao;
import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.RegistroPontoAuditoria;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.RealizacaoTurnoRepository;
import com.example.shiftsync.repository.RegistroPontoAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o fluxo HTTP de ponto com repositórios simulados e um relógio controlado.
 *
 * Os cenários cobrem as quatro marcações, sequência, identidade por CPF,
 * jornadas pendentes e apuração em limites de mês.
 */
@ExtendWith(MockitoExtension.class)
class RegistroPontoControllerTest {

    private static final String CPF = "52998224725";
    private static final LocalDate DATA = LocalDate.of(2026, 10, 27);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Mock
    private RealizacaoTurnoRepository realizacaoTurnoRepository;

    @Mock
    private RegistroPontoAuditoriaRepository auditoriaRepository;

    private MockMvc mockMvc;
    private MutableClock clock;
    private Funcionario funcionario;
    private AtribuicaoTurno atribuicao;
    private AtomicReference<RealizacaoTurno> persistido;

    @BeforeEach
    void configurar() {
        clock = new MutableClock(LocalDateTime.of(2026, 10, 27, 9, 0));
        funcionario = new Funcionario();
        funcionario.setId(7L);
        funcionario.setNome("Ana Silva");
        funcionario.setCpf(CPF);
        funcionario.setStatus("A");

        Turno turno = new Turno();
        turno.setId(11L);
        turno.setNome("Jornada diurna");
        turno.setTipoJornada("DIURNA");
        turno.setTipoEscala("SEMANAL_FIXA");
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(17, 0));
        turno.setDiasSemana(Set.of(DayOfWeek.TUESDAY));
        turno.setIntervalos(List.of(
                new IntervaloJornada(LocalTime.of(12, 0), LocalTime.of(13, 0))));

        atribuicao = new AtribuicaoTurno();
        atribuicao.setId(3L);
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(turno);
        atribuicao.setVigenciaInicio(LocalDateTime.of(2026, 1, 1, 0, 0));

        persistido = new AtomicReference<>();
        lenient().when(funcionarioRepository.findAllByCpfNormalizado(CPF))
                .thenReturn(List.of(funcionario));
        lenient().when(funcionarioRepository.findByIdForUpdate(7L))
                .thenReturn(Optional.of(funcionario));
        lenient().when(atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(7L))
                .thenReturn(List.of(atribuicao));
        lenient().when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                3L, DATA.atTime(8, 0)))
                .thenAnswer(invocacao -> Optional.ofNullable(persistido.get()));
        lenient().when(realizacaoTurnoRepository.save(any(RealizacaoTurno.class)))
                .thenAnswer(invocacao -> {
                    RealizacaoTurno realizacao = invocacao.getArgument(0);
                    persistido.set(realizacao);
                    return realizacao;
                });
        lenient().when(auditoriaRepository.save(any(RegistroPontoAuditoria.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc = MockMvcBuilders.standaloneSetup(new RegistroPontoController(
                funcionarioRepository,
                atribuicaoTurnoRepository,
                realizacaoTurnoRepository,
                auditoriaRepository,
                clock)).build();
    }

    @Test
    void gravaQuatroMarcacoesEConsultaRegistroPersistido() throws Exception {
        registrar("ENTRADA", "2026-10-27T09:00:00", "EM_ANDAMENTO");
        registrar("SAIDA_INTERVALO", "2026-10-27T12:00:00", "EM_ANDAMENTO");
        registrar("RETORNO_INTERVALO", "2026-10-27T12:30:00", "EM_ANDAMENTO");
        registrar("SAIDA_FINAL", "2026-10-27T17:00:00", "IRREGULAR");

        mockMvc.perform(get("/ponto")
                        .param("cpf", CPF)
                        .param("data", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entrada").value("2026-10-27T09:00:00"))
                .andExpect(jsonPath("$.saidaIntervalo").value("2026-10-27T12:00:00"))
                .andExpect(jsonPath("$.retornoIntervalo").value("2026-10-27T12:30:00"))
                .andExpect(jsonPath("$.saidaFinal").value("2026-10-27T17:00:00"))
                .andExpect(jsonPath("$.minutosTrabalhados").value(450));

        assertEquals(1, persistido.get().getIntervalosNaoTrabalhados().size());
        assertEquals(450, minutosTrabalhados(persistido.get()));
        verify(realizacaoTurnoRepository, org.mockito.Mockito.times(4))
                .save(any(RealizacaoTurno.class));
        verify(auditoriaRepository, org.mockito.Mockito.times(4))
                .save(any(RegistroPontoAuditoria.class));
    }

    @Test
    void bloqueiaMarcacaoForaDaSequenciaEduplicada() throws Exception {
        postarMarcacao("RETORNO_INTERVALO")
                .andExpect(status().isBadRequest());
        assertNull(persistido.get());

        registrar("ENTRADA", "2026-10-27T09:00:00", "EM_ANDAMENTO");
        postarMarcacao("ENTRADA")
                .andExpect(status().isBadRequest());
        assertNotNull(persistido.get());
    }

    @Test
    void rejeitaCpfInvalidoEFuncionarioDeOutroCpf() throws Exception {
        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", "11111111111")
                        .with(cpfAutenticado()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", "11144477735")
                        .with(cpfAutenticado()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(realizacaoTurnoRepository, auditoriaRepository);
    }

    @Test
    void naoPermiteRetornoDoIntervaloSemSaidaAnterior() throws Exception {
        registrar("ENTRADA", "2026-10-27T09:00:00", "EM_ANDAMENTO");
        clock.definir(LocalDateTime.of(2026, 10, 27, 11, 0));
        clearInvocations(realizacaoTurnoRepository);
        postarMarcacao("RETORNO_INTERVALO")
                .andExpect(status().isBadRequest());

        verify(realizacaoTurnoRepository, never()).save(any(RealizacaoTurno.class));
    }

    @Test
    void consultaJornadaSemMarcacoesComoPendente() throws Exception {
        mockMvc.perform(get("/ponto")
                        .param("cpf", CPF)
                        .param("data", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.entrada").doesNotExist());
    }

    @Test
    void naoContaFaltaAntesDoFimDaJornadaEIncluiPendencia() throws Exception {
        atribuicao.setVigenciaInicio(DATA.atStartOfDay());

        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF)
                        .param("mes", "2026-10")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeFaltas").value(0))
                .andExpect(jsonPath("$.quantidadePendencias").value(1));

        clock.definir(LocalDateTime.of(2026, 10, 27, 18, 0));
        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF)
                        .param("mes", "2026-10")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeFaltas").value(1));
    }

    @Test
    void calculaResumoMensalPorMinutosAoAtravessarViradaDoMes() throws Exception {
        Turno turnoNoturno = atribuicao.getTurno();
        turnoNoturno.setHorarioInicio(LocalTime.of(22, 0));
        turnoNoturno.setHorarioFim(LocalTime.of(6, 0));
        turnoNoturno.setDiasSemana(Set.of(DayOfWeek.SATURDAY));
        turnoNoturno.setIntervalos(List.of(
                new IntervaloJornada(LocalTime.of(1, 0), LocalTime.of(1, 30))));
        atribuicao.setVigenciaInicio(LocalDateTime.of(2026, 10, 31, 0, 0));
        atribuicao.setVigenciaFim(LocalDateTime.of(2026, 11, 1, 6, 0));

        RealizacaoTurno noturna = new RealizacaoTurno();
        noturna.setAtribuicao(atribuicao);
        noturna.setInicioPlanejado(LocalDateTime.of(2026, 10, 31, 22, 0));
        noturna.setFimPlanejado(LocalDateTime.of(2026, 11, 1, 6, 0));
        noturna.setInicioReal(noturna.getInicioPlanejado());
        noturna.setInicioIntervaloReal(LocalDateTime.of(2026, 11, 1, 1, 0));
        noturna.setFimIntervaloReal(LocalDateTime.of(2026, 11, 1, 1, 30));
        noturna.setFimReal(noturna.getFimPlanejado());
        noturna.setIntervalosNaoTrabalhados(List.of(new IntervaloRealizacao(
                noturna.getInicioIntervaloReal(), noturna.getFimIntervaloReal())));
        noturna.setStatus(StatusRealizacaoTurno.REALIZADO);
        noturna.setConfirmadoEm(noturna.getFimReal());
        when(realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(7L))
                .thenReturn(List.of(noturna));

        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF)
                        .param("mes", "2026-10")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(120))
                .andExpect(jsonPath("$.minutosPrevistos").value(120));

        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF)
                        .param("mes", "2026-11")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(330))
                .andExpect(jsonPath("$.minutosPrevistos").value(330));
    }

    @Test
    void consultaRegistrosDePontoPorPeriodo() throws Exception {
        mockMvc.perform(get("/ponto/periodo")
                        .param("cpf", CPF)
                        .param("inicio", DATA.toString())
                        .param("fim", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].data").value(DATA.toString()));
    }

    private void registrar(String marcacao, String instante, String statusEsperado)
            throws Exception {
        clock.definir(LocalDateTime.parse(instante));
        postarMarcacao(marcacao)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(statusEsperado));
    }

    private org.springframework.test.web.servlet.ResultActions postarMarcacao(String marcacao)
            throws Exception {
        return mockMvc.perform(post("/ponto/marcacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "cpf": "%s",
                          "data": "%s",
                          "marcacao": "%s"
                        }
                        """.formatted(CPF, DATA, marcacao))
                .with(cpfAutenticado()));
    }

    private RequestPostProcessor cpfAutenticado() {
        return request -> {
            request.setAttribute("authenticatedCpf", CPF);
            return request;
        };
    }

    private int minutosTrabalhados(RealizacaoTurno realizacao) {
        long minutos = java.time.Duration.between(
                realizacao.getInicioReal(), realizacao.getInicioIntervaloReal()).toMinutes();
        minutos += java.time.Duration.between(
                realizacao.getFimIntervaloReal(), realizacao.getFimReal()).toMinutes();
        return Math.toIntExact(minutos);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(LocalDateTime inicial) {
            definir(inicial);
        }

        private void definir(LocalDateTime dataHora) {
            instant = dataHora.atZone(ZONE).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return ZONE;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
