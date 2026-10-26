package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    @Mock
    private TurnoRopository turnoRepository;

    @Mock
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

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
}
