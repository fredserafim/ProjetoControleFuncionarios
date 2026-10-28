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

/**
 * Testes unitários da geração e validação de escalas.
 *
 * Os cenários cobrem cargas efetivas, meia-noite, escala 12x36, descanso,
 * intervalos, transições de vigência e conflitos entre jornadas.
 */
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

    @Test
    void detectaSobreposicaoSemanalEPermiteFimExclusivo() {
        Turno primeiro = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(12, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        Turno sobreposto = turnoSemanal(LocalTime.of(11, 0), LocalTime.of(15, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        Turno consecutivo = turnoSemanal(LocalTime.of(12, 0), LocalTime.of(16, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 26, 0, 0);

        assertTrue(RegrasJornada.encontrarSobreposicao(
                primeiro, inicio, null, sobreposto, inicio, null).isPresent());
        assertTrue(RegrasJornada.encontrarSobreposicao(
                primeiro, inicio, null, consecutivo, inicio, null).isEmpty());
    }

    @Test
    void detectaSobreposicaoEntreJornadaNoturnaEDiaSeguinte() {
        Turno noturno = turnoSemanal(LocalTime.of(22, 0), LocalTime.of(6, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        Turno diurno = turnoSemanal(LocalTime.of(5, 30), LocalTime.of(8, 0),
                Set.of(DayOfWeek.TUESDAY), List.of());
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 26, 0, 0);

        var conflito = RegrasJornada.encontrarSobreposicao(
                noturno, inicio, null, diurno, inicio, null).orElseThrow();

        assertEquals(LocalDateTime.of(2026, 10, 27, 5, 30), conflito.inicio());
        assertEquals(LocalDateTime.of(2026, 10, 27, 6, 0), conflito.fim());
    }

    @Test
    void consideraDiasDaSemanaETransicoesDeVigencia() {
        Turno segunda = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(12, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        Turno terca = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(12, 0),
                Set.of(DayOfWeek.TUESDAY), List.of());
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 26, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 2, 0, 0);

        assertTrue(RegrasJornada.encontrarSobreposicao(
                segunda, inicio, null, terca, inicio, null).isEmpty());
        assertTrue(RegrasJornada.encontrarSobreposicao(
                segunda, inicio, fim, segunda, fim, null).isEmpty());
        assertTrue(RegrasJornada.encontrarSobreposicao(
                segunda, inicio, null, segunda, inicio.plusDays(1), null).isPresent());
    }

    @Test
    void consideraSomenteTrechoDaOcorrenciaDentroDaVigenciaSobreposta() {
        Turno primeiro = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(12, 0),
                Set.of(DayOfWeek.TUESDAY), List.of());
        Turno segundo = turnoSemanal(LocalTime.of(11, 0), LocalTime.of(14, 0),
                Set.of(DayOfWeek.TUESDAY), List.of());
        LocalDateTime vigencia = LocalDateTime.of(2026, 10, 27, 9, 0);

        var conflito = RegrasJornada.encontrarSobreposicao(
                primeiro, vigencia, null, segundo, vigencia, null).orElseThrow();

        assertEquals(LocalDateTime.of(2026, 10, 27, 11, 0), conflito.inicio());
        assertEquals(LocalDateTime.of(2026, 10, 27, 12, 0), conflito.fim());
    }

    @Test
    void consideraReferenciaDaEscalaDozePorTrintaESete() {
        Turno escala12x36 = turno12x36(LocalDate.of(2026, 10, 26));
        Turno terca = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(12, 0),
                Set.of(DayOfWeek.TUESDAY), List.of());
        Turno segunda = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(12, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 26, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 10, 27, 0, 0);

        assertTrue(RegrasJornada.encontrarSobreposicao(
                escala12x36, inicio, fim, terca, inicio, fim).isEmpty());
        assertTrue(RegrasJornada.encontrarSobreposicao(
                escala12x36, inicio, fim, segunda, inicio, fim).isPresent(),
                () -> "12x36: " + RegrasJornada.ocorrencias(
                        escala12x36, inicio.minusDays(1), inicio.plusDays(2))
                        + ", semanal: " + RegrasJornada.ocorrencias(
                        segunda, inicio.minusDays(1), inicio.plusDays(2)));
    }

    @Test
    void intervaloNaoRemuneradoNaoAutorizaSegundaAtribuicaoSilenciosamente() {
        Turno comIntervalo = turnoSemanal(LocalTime.of(8, 0), LocalTime.of(18, 0),
                Set.of(DayOfWeek.MONDAY),
                List.of(new IntervaloJornada(LocalTime.of(12, 0), LocalTime.of(13, 30))));
        Turno duranteIntervalo = turnoSemanal(LocalTime.of(12, 30), LocalTime.of(13, 0),
                Set.of(DayOfWeek.MONDAY), List.of());
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 26, 0, 0);

        assertTrue(RegrasJornada.encontrarSobreposicao(
                comIntervalo, inicio, null, duranteIntervalo, inicio, null).isPresent());
    }

    private static Turno turno12x36(LocalDate referencia) {
        Turno turno = new Turno();
        turno.setNome("Plantão 12x36");
        turno.setTipoJornada("DIURNA");
        turno.setTipoEscala(RegrasJornada.ESCALA_12X36);
        turno.setDataReferencia(referencia);
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(20, 0));
        return turno;
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
