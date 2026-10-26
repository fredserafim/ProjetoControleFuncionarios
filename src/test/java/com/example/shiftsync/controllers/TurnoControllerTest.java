package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.TurnoRopository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    @Mock
    private TurnoRopository turnoRepository;

    @Mock
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private TurnoController turnoController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController).build();
    }

    @Test
    void consultaSeJornadaEstaPrevistaParaAData() throws Exception {
        when(turnoRepository.findById(12L)).thenReturn(Optional.of(jornadaSemanal()));

        mockMvc.perform(get("/turnos/12/trabalha").param("data", "2026-10-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("2026-10-26"))
                .andExpect(jsonPath("$.trabalha").value(true));
    }

    @Test
    void listaOcorrenciasEHorariosDaJornada() throws Exception {
        when(turnoRepository.findById(12L)).thenReturn(Optional.of(jornadaSemanal()));

        mockMvc.perform(get("/turnos/12/ocorrencias")
                        .param("inicio", "2026-10-26T00:00:00")
                        .param("fim", "2026-10-28T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio").value("2026-10-26T08:00:00"))
                .andExpect(jsonPath("$[0].fim").value("2026-10-26T17:00:00"));
    }

    @Test
    void retorna409ENaoPersisteAtualizacaoQueCriaConflito() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(7L);
        Turno turnoAlterado = jornadaSemanal();
        turnoAlterado.setHorarioFim(LocalTime.of(12, 0));
        turnoAlterado.setPeriodo("Jornada diurna");
        Turno outroTurno = jornadaSemanal();
        outroTurno.setId(13L);
        outroTurno.setNome("Turno concorrente");
        outroTurno.setHorarioInicio(LocalTime.of(11, 0));
        outroTurno.setHorarioFim(LocalTime.of(15, 0));
        LocalDateTime vigencia = LocalDateTime.of(2026, 10, 26, 0, 0);
        AtribuicaoTurno atribuicaoAlterada = atribuicao(1L, funcionario, turnoAlterado, vigencia);
        AtribuicaoTurno atribuicaoConcorrente = atribuicao(2L, funcionario, outroTurno, vigencia);

        when(atribuicaoTurnoRepository.findByTurno_Id(12L))
                .thenReturn(List.of(atribuicaoAlterada));
        when(funcionarioRepository.findAllByIdForUpdate(List.of(7L)))
                .thenReturn(List.of(funcionario));
        when(turnoRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(turnoAlterado));
        when(atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(7L))
                .thenReturn(List.of(atribuicaoAlterada, atribuicaoConcorrente));

        mockMvc.perform(put("/turnos/12")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Jornada atualizada",
                                  "tipoJornada": "DIURNA",
                                  "tipoEscala": "SEMANAL_FIXA",
                                  "diasSemana": ["MONDAY"],
                                  "horarioInicio": "09:00:00",
                                  "horarioFim": "13:00:00",
                                  "intervalos": []
                                }
                                """))
                .andExpect(status().isConflict());

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void abortaEdicaoSeUmaAtribuicaoConcorrenteNaoFoiBloqueada() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(7L);
        Turno turno = jornadaSemanal();
        AtribuicaoTurno atribuicao = atribuicao(
                1L, funcionario, turno, LocalDateTime.of(2026, 10, 26, 0, 0));
        when(atribuicaoTurnoRepository.findByTurno_Id(12L))
                .thenReturn(List.of(), List.of(atribuicao));
        when(turnoRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(turno));

        mockMvc.perform(put("/turnos/12")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Jornada atualizada",
                                  "tipoJornada": "DIURNA",
                                  "tipoEscala": "SEMANAL_FIXA",
                                  "diasSemana": ["MONDAY"],
                                  "horarioInicio": "09:00:00",
                                  "horarioFim": "17:00:00",
                                  "intervalos": []
                                }
                                """))
                .andExpect(status().isConflict());

        verify(funcionarioRepository, never()).findAllByIdForUpdate(any());
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    private static Turno jornadaSemanal() {
        Turno turno = new Turno();
        turno.setId(12L);
        turno.setNome("Jornada diurna");
        turno.setTipoJornada("DIURNA");
        turno.setTipoEscala("SEMANAL_FIXA");
        turno.setDiasSemana(Set.of(DayOfWeek.MONDAY));
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(17, 0));
        return turno;
    }

    private static AtribuicaoTurno atribuicao(
            Long id, Funcionario funcionario, Turno turno, LocalDateTime vigencia) {
        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setId(id);
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(turno);
        atribuicao.setVigenciaInicio(vigencia);
        return atribuicao;
    }
}
