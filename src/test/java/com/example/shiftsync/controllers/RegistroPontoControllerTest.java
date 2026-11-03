package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.IntervaloJornada;
import com.example.shiftsync.entities.IntervaloRealizacao;
import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.RegistroPontoAuditoria;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.RealizacaoTurnoRepository;
import com.example.shiftsync.repository.RegistroPontoAuditoriaRepository;
import com.example.shiftsync.repository.UsuarioRepository;
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

    private static final String CPF_GESTOR = "52998224725";
    private static final String CPF_FUNCIONARIO = "11144477735";
    private static final String CPF_OUTRO_FUNCIONARIO = "93541134780";
    private static final LocalDate DATA = LocalDate.of(2026, 11, 3);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Mock
    private RealizacaoTurnoRepository realizacaoTurnoRepository;

    @Mock
    private RegistroPontoAuditoriaRepository auditoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;
    private MutableClock clock;
    private Funcionario funcionario;
    private AtribuicaoTurno atribuicao;
    private AtomicReference<RealizacaoTurno> persistido;

    @BeforeEach
    void configurar() {
        clock = new MutableClock(LocalDateTime.of(2026, 11, 4, 20, 0));
        funcionario = new Funcionario();
        funcionario.setId(7L);
        funcionario.setNome("Ana Silva");
        funcionario.setCpf(CPF_FUNCIONARIO);
        funcionario.setStatus("A");
        Funcionario outroFuncionario = new Funcionario();
        outroFuncionario.setId(8L);
        outroFuncionario.setNome("Bruno Souza");
        outroFuncionario.setCpf(CPF_OUTRO_FUNCIONARIO);
        outroFuncionario.setStatus("A");
        Usuario gestor = new Usuario("Gestor", CPF_GESTOR, "1990-01-01");
        gestor.setStatus("A");

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
        lenient().when(funcionarioRepository.findAllByCpfNormalizado(CPF_FUNCIONARIO))
                .thenReturn(List.of(funcionario));
        lenient().when(funcionarioRepository.findAllByCpfNormalizado(CPF_OUTRO_FUNCIONARIO))
                .thenReturn(List.of(outroFuncionario));
        lenient().when(usuarioRepository.findByCpf(CPF_GESTOR))
                .thenReturn(Optional.of(gestor));
        lenient().when(funcionarioRepository.findByIdForUpdate(7L))
                .thenReturn(Optional.of(funcionario));
        lenient().when(atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(7L))
                .thenReturn(List.of(atribuicao));
        lenient().when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                3L, DATA.atTime(8, 0)))
                .thenAnswer(invocacao -> Optional.ofNullable(persistido.get()));
        lenient().when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                3L, DATA.atTime(22, 0)))
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
                usuarioRepository,
                clock)).build();
    }

    @Test
    void gravaQuatroMarcacoesEConsultaRegistroPersistido() throws Exception {
        registrar("ENTRADA", "08:00", "EM_ANDAMENTO");
        registrar("SAIDA_INTERVALO", "12:00", "EM_ANDAMENTO");
        registrar("RETORNO_INTERVALO", "13:30", "EM_ANDAMENTO");
        registrar("SAIDA_FINAL", "18:00", "IRREGULAR");

        mockMvc.perform(get("/ponto")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("data", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entrada").value("2026-11-03T08:00:00"))
                .andExpect(jsonPath("$.saidaIntervalo").value("2026-11-03T12:00:00"))
                .andExpect(jsonPath("$.retornoIntervalo").value("2026-11-03T13:30:00"))
                .andExpect(jsonPath("$.saidaFinal").value("2026-11-03T18:00:00"))
                .andExpect(jsonPath("$.minutosTrabalhados").value(510));

        assertEquals(LocalDateTime.of(2026, 11, 3, 8, 0), persistido.get().getInicioReal());
        assertEquals(LocalDateTime.of(2026, 11, 3, 12, 0),
                persistido.get().getInicioIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 11, 3, 13, 30),
                persistido.get().getFimIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 11, 3, 18, 0), persistido.get().getFimReal());
        assertEquals(1, persistido.get().getIntervalosNaoTrabalhados().size());
        assertEquals(510, minutosTrabalhados(persistido.get()));
        verify(realizacaoTurnoRepository, org.mockito.Mockito.times(4))
                .save(any(RealizacaoTurno.class));
        org.mockito.ArgumentCaptor<RegistroPontoAuditoria> auditorias =
                org.mockito.ArgumentCaptor.forClass(RegistroPontoAuditoria.class);
        verify(auditoriaRepository, org.mockito.Mockito.times(4)).save(auditorias.capture());
        auditorias.getAllValues().forEach(auditoria -> {
            assertEquals(CPF_GESTOR, auditoria.getRegistradoPorCpf());
            assertEquals(LocalDateTime.of(2026, 11, 4, 20, 0), auditoria.getRegistradoEm());
        });
    }

    @Test
    void bloqueiaMarcacaoForaDaSequenciaEduplicada() throws Exception {
        postarMarcacao("RETORNO_INTERVALO", "13:30")
                .andExpect(status().isBadRequest());
        assertNull(persistido.get());

        registrar("ENTRADA", "09:00", "EM_ANDAMENTO");
        postarMarcacao("ENTRADA", "10:00")
                .andExpect(status().isBadRequest());
        assertNotNull(persistido.get());
    }

    @Test
    void rejeitaHorarioVazioOuInvalido() throws Exception {
        postarMarcacao("ENTRADA", null)
                .andExpect(status().isBadRequest());
        postarMarcacao("ENTRADA", "")
                .andExpect(status().isBadRequest());
        postarMarcacao("ENTRADA", "25:90")
                .andExpect(status().isBadRequest());
        assertNull(persistido.get());
        verify(realizacaoTurnoRepository, never()).save(any(RealizacaoTurno.class));
    }

    @Test
    void gestorConsultaFuncionarioComCpfDiferenteDoJwt() throws Exception {
        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", "11111111111")
                        .with(cpfAutenticado()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", CPF_OUTRO_FUNCIONARIO)
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Bruno Souza"));

        verifyNoInteractions(realizacaoTurnoRepository, auditoriaRepository);
    }

    @Test
    void gestorRegistraPontoDeOutroFuncionarioEPermaneceResponsavelNaAuditoria()
            throws Exception {
        Funcionario outroFuncionario = new Funcionario();
        outroFuncionario.setId(8L);
        outroFuncionario.setNome("Bruno Souza");
        outroFuncionario.setCpf(CPF_OUTRO_FUNCIONARIO);
        outroFuncionario.setStatus("A");
        AtribuicaoTurno outraAtribuicao = new AtribuicaoTurno();
        outraAtribuicao.setId(4L);
        outraAtribuicao.setFuncionario(outroFuncionario);
        outraAtribuicao.setTurno(atribuicao.getTurno());
        outraAtribuicao.setVigenciaInicio(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(funcionarioRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(outroFuncionario));
        when(atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(8L))
                .thenReturn(List.of(outraAtribuicao));
        when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                4L, DATA.atTime(8, 0))).thenReturn(Optional.empty());

        postarMarcacao(CPF_OUTRO_FUNCIONARIO, DATA, "ENTRADA", "08:00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entrada").value("2026-11-03T08:00:00"));

        verify(auditoriaRepository).save(org.mockito.ArgumentMatchers.argThat(
                auditoria -> CPF_GESTOR.equals(auditoria.getRegistradoPorCpf())
                        && "ENTRADA".equals(auditoria.getMarcacao())));
    }

    @Test
    void rejeitaAcessoSemGestorAutenticadoEmTodosOsEndpoints() throws Exception {
        mockMvc.perform(get("/ponto/funcionario").param("cpf", CPF_FUNCIONARIO))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/ponto")
                        .param("cpf", CPF_FUNCIONARIO).param("data", DATA.toString()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/ponto/periodo")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("inicio", DATA.toString()).param("fim", DATA.toString()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF_FUNCIONARIO).param("mes", "2026-11"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/ponto/marcacoes")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf":"%s","data":"%s","marcacao":"ENTRADA","horario":"08:00"}
                                """.formatted(CPF_FUNCIONARIO, DATA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejeitaGestorInativoFuncionarioInativoEFuncionarioInexistente() throws Exception {
        when(usuarioRepository.findByCpf(CPF_GESTOR)).thenReturn(Optional.empty());
        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", CPF_FUNCIONARIO).with(cpfAutenticado()))
                .andExpect(status().isUnauthorized());

        Usuario gestorInativo = new Usuario("Gestor", CPF_GESTOR, "1990-01-01");
        gestorInativo.setStatus("I");
        when(usuarioRepository.findByCpf(CPF_GESTOR)).thenReturn(Optional.of(gestorInativo));
        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", CPF_FUNCIONARIO).with(cpfAutenticado()))
                .andExpect(status().isForbidden());

        Usuario gestorAtivo = new Usuario("Gestor", CPF_GESTOR, "1990-01-01");
        gestorAtivo.setStatus("A");
        when(usuarioRepository.findByCpf(CPF_GESTOR)).thenReturn(Optional.of(gestorAtivo));
        funcionario.setStatus("I");
        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", CPF_FUNCIONARIO).with(cpfAutenticado()))
                .andExpect(status().isForbidden());

        funcionario.setStatus("A");
        when(funcionarioRepository.findAllByCpfNormalizado(CPF_OUTRO_FUNCIONARIO))
                .thenReturn(List.of());
        mockMvc.perform(get("/ponto/funcionario")
                        .param("cpf", CPF_OUTRO_FUNCIONARIO).with(cpfAutenticado()))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoPermiteConsultarOuRegistrarEmDataSemJornadaVigente() throws Exception {
        when(atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(7L))
                .thenReturn(List.of());

        mockMvc.perform(get("/ponto")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("data", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isNotFound());

        postarMarcacao("ENTRADA", "08:00")
                .andExpect(status().isNotFound());
        verify(realizacaoTurnoRepository, never()).save(any(RealizacaoTurno.class));
        verify(auditoriaRepository, never()).save(any(RegistroPontoAuditoria.class));
    }

    @Test
    void permiteLancarJornadaPassadaMasRejeitaDataEHorarioFuturos() throws Exception {
        registrar("ENTRADA", "08:00", "EM_ANDAMENTO");
        assertEquals(LocalDateTime.of(2026, 11, 3, 8, 0), persistido.get().getInicioReal());

        clock.definir(DATA.atTime(10, 0));
        postarMarcacao("SAIDA_INTERVALO", "12:00")
                .andExpect(status().isBadRequest());
        postarMarcacao(DATA.plusDays(7), "ENTRADA", "08:00")
                .andExpect(status().isBadRequest());
        assertNull(persistido.get().getInicioIntervaloReal());
    }

    @Test
    void associaCorretamenteMarcacoesDeJornadaNoturnaAoDiaSeguinte() throws Exception {
        Turno turnoNoturno = atribuicao.getTurno();
        turnoNoturno.setHorarioInicio(LocalTime.of(22, 0));
        turnoNoturno.setHorarioFim(LocalTime.of(6, 0));
        turnoNoturno.setIntervalos(List.of(
                new IntervaloJornada(LocalTime.of(1, 0), LocalTime.of(1, 30))));

        registrar("ENTRADA", "22:00", "EM_ANDAMENTO");
        registrar("SAIDA_INTERVALO", "01:00", "EM_ANDAMENTO");
        registrar("RETORNO_INTERVALO", "01:30", "EM_ANDAMENTO");
        registrar("SAIDA_FINAL", "06:00", "CONCLUIDO");

        assertEquals(LocalDateTime.of(2026, 11, 3, 22, 0), persistido.get().getInicioReal());
        assertEquals(LocalDateTime.of(2026, 11, 4, 1, 0),
                persistido.get().getInicioIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 11, 4, 1, 30),
                persistido.get().getFimIntervaloReal());
        assertEquals(LocalDateTime.of(2026, 11, 4, 6, 0), persistido.get().getFimReal());
    }

    @Test
    void naoPermiteRetornoDoIntervaloSemSaidaAnterior() throws Exception {
        registrar("ENTRADA", "09:00", "EM_ANDAMENTO");
        clock.definir(DATA.atTime(20, 0));
        clearInvocations(realizacaoTurnoRepository);
        postarMarcacao("RETORNO_INTERVALO", "12:30")
                .andExpect(status().isBadRequest());

        verify(realizacaoTurnoRepository, never()).save(any(RealizacaoTurno.class));
    }

    @Test
    void consultaJornadaSemMarcacoesComoPendente() throws Exception {
        mockMvc.perform(get("/ponto")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("data", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.entrada").doesNotExist());
    }

    @Test
    void naoContaFaltaAntesDoFimDaJornadaEIncluiPendencia() throws Exception {
        atribuicao.setVigenciaInicio(LocalDate.of(2026, 10, 27).atStartOfDay());
        clock.definir(LocalDateTime.of(2026, 10, 27, 9, 0));

        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("mes", "2026-10")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeFaltas").value(0))
                .andExpect(jsonPath("$.quantidadePendencias").value(1));

        clock.definir(LocalDateTime.of(2026, 10, 27, 18, 0));
        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF_FUNCIONARIO)
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
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("mes", "2026-10")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(120))
                .andExpect(jsonPath("$.minutosPrevistos").value(120));

        mockMvc.perform(get("/ponto/resumo-mensal")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("mes", "2026-11")
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(330))
                .andExpect(jsonPath("$.minutosPrevistos").value(330));
    }

    @Test
    void consultaRegistrosDePontoPorPeriodo() throws Exception {
        mockMvc.perform(get("/ponto/periodo")
                        .param("cpf", CPF_FUNCIONARIO)
                        .param("inicio", DATA.toString())
                        .param("fim", DATA.toString())
                        .with(cpfAutenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].data").value(DATA.toString()));
    }

    private void registrar(String marcacao, String horario, String statusEsperado)
            throws Exception {
        postarMarcacao(marcacao, horario)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(statusEsperado));
    }

    private org.springframework.test.web.servlet.ResultActions postarMarcacao(
            String marcacao, String horario)
            throws Exception {
        return postarMarcacao(DATA, marcacao, horario);
    }

    private org.springframework.test.web.servlet.ResultActions postarMarcacao(
            LocalDate data, String marcacao, String horario)
            throws Exception {
        return postarMarcacao(CPF_FUNCIONARIO, data, marcacao, horario);
    }

    private org.springframework.test.web.servlet.ResultActions postarMarcacao(
            String cpfFuncionario, LocalDate data, String marcacao, String horario)
            throws Exception {
        String horarioJson = horario == null ? "" : "\"horario\": \"%s\",\n".formatted(horario);
        return mockMvc.perform(post("/ponto/marcacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "cpf": "%s",
                          "data": "%s",
                          %s
                          "registradoPorCpf": "00000000000",
                          "marcacao": "%s"
                        }
                        """.formatted(cpfFuncionario, data, horarioJson, marcacao))
                .with(cpfAutenticado()));
    }

    private RequestPostProcessor cpfAutenticado() {
        return request -> {
            request.setAttribute("authenticatedCpf", CPF_GESTOR);
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
