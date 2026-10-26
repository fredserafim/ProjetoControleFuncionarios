package com.example.shiftsync.utils;

import com.example.shiftsync.entities.IntervaloJornada;
import com.example.shiftsync.entities.Turno;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegrasJornadaTest {

    @Test
    void calculaCargaEfetivaSemanalEJornadaQueAtravessaMeiaNoite() {
        Turno turno = turnoSemanal(LocalTime.of(22, 0), LocalTime.of(6, 0),
                Set.of(DayOfWeek.MONDAY),
                List.of(new IntervaloJornada(LocalTime.of(1, 0), LocalTime.of(1, 30))));

        RegrasJornada.validarECalcular(turno);

        assertEquals(450, turno.getCargaEfetivaMinutos());
        assertEquals(450, turno.getCargaSemanalPrevistaMinutos());
    }

    @Test
    void calculaCargaDeEscalaDozePorTrintaESeteDiasAlternados() {
        Turno turno = new Turno();
        turno.setNome("Plantão");
        turno.setTipoJornada("NOTURNA");
        turno.setTipoEscala(RegrasJornada.ESCALA_12X36);
        turno.setHorarioInicio(LocalTime.of(7, 0));
        turno.setHorarioFim(LocalTime.of(19, 0));
        turno.setDataReferencia(LocalDate.of(2026, 10, 26));
        turno.setIntervalos(List.of(
                new IntervaloJornada(LocalTime.of(12, 0), LocalTime.of(13, 0))));

        RegrasJornada.validarECalcular(turno);

        assertEquals(660, turno.getCargaEfetivaMinutos());
        assertEquals(2310, turno.getCargaSemanalPrevistaMinutos());
        assertTrue(RegrasJornada.trabalhaNaData(turno, LocalDate.of(2026, 10, 26)));
        assertFalse(RegrasJornada.trabalhaNaData(turno, LocalDate.of(2026, 10, 27)));
        assertTrue(RegrasJornada.trabalhaNaData(turno, LocalDate.of(2026, 10, 28)));
    }

    @Test
    void criaOcorrenciasComInicioEFimCorretos() {
        Turno turno = turnoSemanal(LocalTime.of(22, 0), LocalTime.of(6, 0),
                Set.of(DayOfWeek.MONDAY), List.of());

        List<RegrasJornada.Ocorrencia> ocorrencias = RegrasJornada.ocorrencias(
                turno,
                LocalDateTime.of(2026, 10, 26, 0, 0),
                LocalDateTime.of(2026, 10, 28, 0, 0));

        assertEquals(1, ocorrencias.size());
        assertEquals(LocalDateTime.of(2026, 10, 26, 22, 0), ocorrencias.get(0).inicio());
        assertEquals(LocalDateTime.of(2026, 10, 27, 6, 0), ocorrencias.get(0).fim());
    }

    @Test
    void rejeitaIntervalosSobrepostos() {
        Turno turno = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(18, 0),
                Set.of(DayOfWeek.MONDAY), List.of(
                        new IntervaloJornada(LocalTime.of(10, 0), LocalTime.of(11, 0)),
                        new IntervaloJornada(LocalTime.of(10, 30), LocalTime.of(11, 30))));

        assertThrows(ResponseStatusException.class, () -> RegrasJornada.validarECalcular(turno));
    }

    @Test
    void rejeitaDescansoMenorQueOnzeHorasEntreDiasTrabalhados() {
        Turno turno = turnoSemanal(LocalTime.of(6, 0), LocalTime.of(20, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), List.of());

        assertThrows(ResponseStatusException.class, () -> RegrasJornada.validarECalcular(turno));
    }

    @Test
    void rejeitaTransicaoQueNaoGaranteDescansoMinimo() {
        Turno anterior = turnoSemanal(LocalTime.of(6, 0), LocalTime.of(18, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        Turno novo = turnoSemanal(LocalTime.of(18, 30), LocalTime.of(6, 30),
                Set.of(DayOfWeek.MONDAY), List.of());

        assertThrows(ResponseStatusException.class, () -> RegrasJornada.validarTransicao(
                anterior, novo, LocalDateTime.of(2026, 10, 26, 18, 0)));
    }

    @Test
    void rejeitaVigenciaQueInterrompePlantaoEmAndamento() {
        Turno anterior = turnoSemanal(LocalTime.of(6, 0), LocalTime.of(18, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        Turno novo = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(17, 0),
                Set.of(DayOfWeek.MONDAY), List.of());

        assertThrows(ResponseStatusException.class, () -> RegrasJornada.validarTransicao(
                anterior, novo, LocalDateTime.of(2026, 10, 26, 12, 0)));
    }

    private static Turno turnoSemanal(
            LocalTime inicio, LocalTime fim, Set<DayOfWeek> dias,
            List<IntervaloJornada> intervalos) {
        Turno turno = new Turno();
        turno.setNome("Jornada");
        turno.setTipoJornada("PERSONALIZADA");
        turno.setTipoEscala(RegrasJornada.ESCALA_SEMANAL_FIXA);
        turno.setHorarioInicio(inicio);
        turno.setHorarioFim(fim);
        turno.setDiasSemana(dias);
        turno.setIntervalos(intervalos);
        return turno;
    }
}
