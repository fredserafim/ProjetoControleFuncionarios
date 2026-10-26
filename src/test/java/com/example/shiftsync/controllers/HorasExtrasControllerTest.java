package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.IntervaloRealizacao;
import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.RealizacaoTurnoRepository;
import com.example.shiftsync.repository.TurnoRopository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HorasExtrasControllerTest {

    private static final Long FUNCIONARIO_ID = 1L;
    private static final Long ATRIBUICAO_ID = 10L;
    private static final LocalDateTime INICIO_OUTUBRO =
            LocalDateTime.of(2026, 10, 1, 0, 0);

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private TurnoRopository turnoRepository;

    @Mock
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Mock
    private RealizacaoTurnoRepository realizacaoTurnoRepository;

    @InjectMocks
    private FuncionarioController funcionarioController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(funcionarioController).build();
    }

    @Test
    void calculaHorasExtrasQuandoTrabalhadoExcedeCargaPadrao() throws Exception {
        Turno turno = turnoSemanal(540);
        AtribuicaoTurno atribuicao = atribuicao(turno);
        RealizacaoTurno realizada = realizada(
                atribuicao, data(5, 8, 0), data(5, 18, 0), List.of());
        configurarApuracao(atribuicao, List.of(realizada));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(600))
                .andExpect(jsonPath("$.minutosCargaPadrao").value(540))
                .andExpect(jsonPath("$.minutosHorasExtras").value(60))
                .andExpect(jsonPath("$.mesReferencia").value("2026-10"))
                .andExpect(jsonPath("$.tipoApuracao").value("REALIZADA"));
    }

    @Test
    void horasExtrasSaoZeroQuandoTrabalhadoIgualaCargaPadrao() throws Exception {
        Turno turno = turnoSemanal(480);
        AtribuicaoTurno atribuicao = atribuicao(turno);
        configurarApuracao(atribuicao, List.of(
                realizada(atribuicao, data(5, 8, 0), data(5, 16, 0), List.of())));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(480))
                .andExpect(jsonPath("$.minutosHorasExtras").value(0));
    }

    @Test
    void horasExtrasSaoZeroQuandoTrabalhadoFicaAbaixoDaCargaPadrao() throws Exception {
        Turno turno = turnoSemanal(480);
        AtribuicaoTurno atribuicao = atribuicao(turno);
        configurarApuracao(atribuicao, List.of(
                realizada(atribuicao, data(5, 8, 0), data(5, 15, 0), List.of())));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(420))
                .andExpect(jsonPath("$.minutosHorasExtras").value(0));
    }

    @Test
    void ocorrenciaPlanejadaSemRegistroPermanecePendente() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        when(funcionarioRepository.findById(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(
                FUNCIONARIO_ID)).thenReturn(List.of(atribuicao));
        when(realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(FUNCIONARIO_ID))
                .thenReturn(List.of());

        mockMvc.perform(get("/funcionario/1/turnos-planejados")
                        .param("inicio", "2026-10-05T00:00:00")
                        .param("fim", "2026-10-06T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].turnoId").value(7));

        when(realizacaoTurnoRepository
                .findByAtribuicao_Funcionario_IdAndStatus(
                        FUNCIONARIO_ID, StatusRealizacaoTurno.REALIZADO))
                .thenReturn(List.of());
        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnosPendentes").value(31))
                .andExpect(jsonPath("$.minutosTrabalhados").value(0));
    }

    @Test
    void utilizaHorarioRealParcialEmVezDaDuracaoPlanejada() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        configurarApuracao(atribuicao, List.of(
                realizada(atribuicao, data(5, 8, 0), data(5, 11, 0), List.of())));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(180))
                .andExpect(jsonPath("$.turnosRealizados").value(1));
    }

    @Test
    void descontaIntervalosEfetivamenteNaoTrabalhados() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(450));
        RealizacaoTurno realizada = realizada(
                atribuicao, data(5, 8, 0), data(5, 16, 0),
                List.of(new IntervaloRealizacao(data(5, 12, 0), data(5, 12, 30))));
        configurarApuracao(atribuicao, List.of(realizada));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(450));
    }

    @Test
    void contabilizaCorretamenteTurnoQueAtravessaMeiaNoite() throws Exception {
        Turno turno = turnoSemanal(0);
        turno.setHorarioInicio(LocalTime.of(22, 0));
        turno.setHorarioFim(LocalTime.of(6, 0));
        AtribuicaoTurno atribuicao = atribuicao(turno);
        configurarApuracao(atribuicao, List.of(
                realizada(atribuicao, data(5, 22, 0), data(6, 6, 0), List.of())));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(480));
    }

    @Test
    void divideTurnoRealizadoNaMudancaDeMes() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(0));
        RealizacaoTurno realizada = realizada(
                atribuicao,
                LocalDateTime.of(2026, 10, 31, 22, 0),
                LocalDateTime.of(2026, 11, 1, 6, 0),
                List.of());
        configurarApuracao(atribuicao, List.of(realizada));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(120));

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(360));
    }

    @Test
    void geraOcorrenciasDeEscalaDozePorTrintaESSeisComDataReferencia() throws Exception {
        Turno turno = turnoSemanal(0);
        turno.setTipoEscala("12X36");
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(20, 0));
        turno.setDataReferencia(LocalDate.of(2026, 10, 1));
        AtribuicaoTurno atribuicao = atribuicao(turno);
        when(funcionarioRepository.findById(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(
                FUNCIONARIO_ID)).thenReturn(List.of(atribuicao));
        when(realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(FUNCIONARIO_ID))
                .thenReturn(List.of());

        mockMvc.perform(get("/funcionario/1/turnos-planejados")
                        .param("inicio", "2026-10-01T00:00:00")
                        .param("fim", "2026-10-06T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].inicioPlanejado").value("2026-10-01T08:00:00"))
                .andExpect(jsonPath("$[1].inicioPlanejado").value("2026-10-03T08:00:00"))
                .andExpect(jsonPath("$[2].inicioPlanejado").value("2026-10-05T08:00:00"));
    }

    @Test
    void informaPendenciaQuandoCargaPadraoNaoFoiConfigurada() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(null));
        configurarApuracao(atribuicao, List.of());

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosCargaPadrao").doesNotExist())
                .andExpect(jsonPath("$.minutosHorasExtras").doesNotExist())
                .andExpect(jsonPath("$.tipoApuracao").value("PADRAO_PENDENTE"))
                .andExpect(jsonPath("$.pendencia").exists());
    }

    @Test
    void consideraJornadaIniciadaDuranteOPrimeiroDiaDoMes() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        atribuicao.setVigenciaInicio(LocalDateTime.of(2026, 10, 1, 8, 0));
        configurarApuracao(atribuicao, List.of());

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosCargaPadrao").value(480))
                .andExpect(jsonPath("$.tipoApuracao").value("REALIZADA"));
    }

    @Test
    void rejeitaSegundaConfirmacaoDaMesmaOcorrencia() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        when(funcionarioRepository.findByIdForUpdate(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findById(ATRIBUICAO_ID))
                .thenReturn(Optional.of(atribuicao));
        when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                ATRIBUICAO_ID, data(5, 8, 0)))
                .thenReturn(Optional.of(new RealizacaoTurno()));

        mockMvc.perform(post("/funcionario/1/realizacoes-turno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atribuicaoTurnoId": 10,
                                  "inicioPlanejado": "2026-10-05T08:00:00",
                                  "status": "NAO_REALIZADO"
                                }
                                """))
                .andExpect(status().isConflict());
        verify(realizacaoTurnoRepository, never()).save(any(RealizacaoTurno.class));
    }

    @Test
    void retornaNaoEncontradoParaFuncionarioInexistente() throws Exception {
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaMesInvalido() throws Exception {
        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "outubro-2026"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void somaUmaUnicaVezFaixasEfetivasSobrepostas() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        List<RealizacaoTurno> realizacoes = List.of(
                realizada(atribuicao, data(5, 8, 0), data(5, 12, 0), List.of()),
                realizada(atribuicao, data(6, 8, 0), data(5, 12, 0), List.of()));
        realizacoes.get(1).setInicioReal(data(5, 8, 0));
        configurarApuracao(atribuicao, realizacoes);

        mockMvc.perform(get("/funcionario/1/horas-extras").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosTrabalhados").value(240));
    }

    @Test
    void marcaExplicitamenteOcorrenciaComoNaoRealizada() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        when(funcionarioRepository.findByIdForUpdate(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findById(ATRIBUICAO_ID))
                .thenReturn(Optional.of(atribuicao));
        when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                ATRIBUICAO_ID, data(5, 8, 0))).thenReturn(Optional.empty());
        when(realizacaoTurnoRepository.save(any(RealizacaoTurno.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(post("/funcionario/1/realizacoes-turno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atribuicaoTurnoId": 10,
                                  "inicioPlanejado": "2026-10-05T08:00:00",
                                  "status": "NAO_REALIZADO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NAO_REALIZADO"));

        verify(realizacaoTurnoRepository).save(any(RealizacaoTurno.class));
    }

    @Test
    void confirmaTurnoRealizadoComIntervaloEHorarioReal() throws Exception {
        AtribuicaoTurno atribuicao = atribuicao(turnoSemanal(480));
        when(funcionarioRepository.findByIdForUpdate(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findById(ATRIBUICAO_ID))
                .thenReturn(Optional.of(atribuicao));
        when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                ATRIBUICAO_ID, data(5, 8, 0))).thenReturn(Optional.empty());
        when(realizacaoTurnoRepository.save(any(RealizacaoTurno.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(post("/funcionario/1/realizacoes-turno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atribuicaoTurnoId": 10,
                                  "inicioPlanejado": "2026-10-05T08:00:00",
                                  "status": "REALIZADO",
                                  "inicioReal": "2026-10-05T08:15:00",
                                  "fimReal": "2026-10-05T16:15:00",
                                  "intervalosNaoTrabalhados": [
                                    {
                                      "inicio": "2026-10-05T12:00:00",
                                      "fim": "2026-10-05T12:30:00"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REALIZADO"))
                .andExpect(jsonPath("$.minutosTrabalhados").value(450))
                .andExpect(jsonPath("$.inicioReal").value("2026-10-05T08:15:00"));
    }

    @Test
    void rejeitaRegistroRealizadoIncompleto() throws Exception {
        when(funcionarioRepository.findByIdForUpdate(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findById(ATRIBUICAO_ID))
                .thenReturn(Optional.of(atribuicao(turnoSemanal(480))));
        when(realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                ATRIBUICAO_ID, data(5, 8, 0))).thenReturn(Optional.empty());

        mockMvc.perform(post("/funcionario/1/realizacoes-turno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atribuicaoTurnoId": 10,
                                  "inicioPlanejado": "2026-10-05T08:00:00",
                                  "status": "REALIZADO",
                                  "inicioReal": "2026-10-05T08:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest());
        verify(realizacaoTurnoRepository, never()).save(any(RealizacaoTurno.class));
    }

    private void configurarApuracao(
            AtribuicaoTurno atribuicao, List<RealizacaoTurno> realizacoes) {
        when(funcionarioRepository.findById(FUNCIONARIO_ID))
                .thenReturn(Optional.of(funcionario()));
        when(atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(
                FUNCIONARIO_ID)).thenReturn(List.of(atribuicao));
        when(realizacaoTurnoRepository
                .findByAtribuicao_Funcionario_IdAndStatus(
                        FUNCIONARIO_ID, StatusRealizacaoTurno.REALIZADO))
                .thenReturn(realizacoes.stream()
                        .filter(item -> item.getStatus() == StatusRealizacaoTurno.REALIZADO)
                        .toList());
        when(realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(FUNCIONARIO_ID))
                .thenReturn(realizacoes);
    }

    private Funcionario funcionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(FUNCIONARIO_ID);
        funcionario.setNome("Ana Silva");
        return funcionario;
    }

    private Turno turnoSemanal(Integer cargaMensal) {
        Turno turno = new Turno();
        turno.setId(7L);
        turno.setNome("Jornada");
        turno.setTipoJornada("DIURNA");
        turno.setTipoEscala("SEMANAL_FIXA");
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(16, 0));
        turno.setDiasSemana(EnumSet.allOf(DayOfWeek.class));
        turno.setCargaMensalPadraoMinutos(cargaMensal);
        return turno;
    }

    private AtribuicaoTurno atribuicao(Turno turno) {
        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setId(ATRIBUICAO_ID);
        atribuicao.setFuncionario(funcionario());
        atribuicao.setTurno(turno);
        atribuicao.setVigenciaInicio(INICIO_OUTUBRO);
        return atribuicao;
    }

    private RealizacaoTurno realizada(
            AtribuicaoTurno atribuicao,
            LocalDateTime inicioReal,
            LocalDateTime fimReal,
            List<IntervaloRealizacao> intervalos) {
        RealizacaoTurno realizacao = new RealizacaoTurno();
        realizacao.setAtribuicao(atribuicao);
        realizacao.setInicioPlanejado(inicioReal.withHour(8).withMinute(0));
        realizacao.setFimPlanejado(realizacao.getInicioPlanejado().plusHours(8));
        realizacao.setStatus(StatusRealizacaoTurno.REALIZADO);
        realizacao.setInicioReal(inicioReal);
        realizacao.setFimReal(fimReal);
        realizacao.setIntervalosNaoTrabalhados(new ArrayList<>(intervalos));
        return realizacao;
    }

    private LocalDateTime data(int dia, int hora, int minuto) {
        return LocalDateTime.of(2026, 10, dia, hora, minuto);
    }
}
