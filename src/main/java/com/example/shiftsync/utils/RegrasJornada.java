package com.example.shiftsync.utils;

import com.example.shiftsync.entities.IntervaloJornada;
import com.example.shiftsync.entities.Turno;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RegrasJornada {

    public static final String ESCALA_SEMANAL_FIXA = "SEMANAL_FIXA";
    public static final String ESCALA_12X36 = "12X36";
    public static final String ESCALA_PERSONALIZADA = "PERSONALIZADA";
    public static final long DESCANSO_MINIMO_MINUTOS = 11 * 60L;

    private static final long CICLO_12X36_MINUTOS = 48 * 60L;
    private static final long DURACAO_12X36_MINUTOS = 12 * 60L;

    private RegrasJornada() {
    }

    public static boolean possuiConfiguracaoJornada(Turno turno) {
        return turno.getHorarioInicio() != null
                || turno.getHorarioFim() != null
                || turno.getTipoEscala() != null
                || turno.getTipoJornada() != null;
    }

    public static void validarECalcular(Turno turno) {
        if (turno.getNome() == null || turno.getNome().isBlank()) {
            throw requisicaoInvalida("Informe o nome da jornada.");
        }
        if (!List.of("DIURNA", "NOTURNA", "PERSONALIZADA")
                .contains(turno.getTipoJornada())) {
            throw requisicaoInvalida("Informe um tipo de jornada válido.");
        }
        if (!List.of(ESCALA_SEMANAL_FIXA, ESCALA_12X36, ESCALA_PERSONALIZADA)
                .contains(turno.getTipoEscala())) {
            throw requisicaoInvalida("Informe um tipo de escala válido.");
        }
        if (turno.getHorarioInicio() == null || turno.getHorarioFim() == null) {
            throw requisicaoInvalida("Informe os horários de início e término.");
        }
        if (turno.getTipoEscala().equals(ESCALA_12X36)
                && turno.getDataReferencia() == null) {
            throw requisicaoInvalida("Informe a data de referência da escala 12x36.");
        }
        if (!turno.getTipoEscala().equals(ESCALA_12X36)
                && (turno.getDiasSemana() == null || turno.getDiasSemana().isEmpty())) {
            throw requisicaoInvalida("Selecione ao menos um dia da semana.");
        }

        long duracaoTurno = duracaoTurnoMinutos(turno);
        if (turno.getTipoEscala().equals(ESCALA_12X36)
                && duracaoTurno != DURACAO_12X36_MINUTOS) {
            throw requisicaoInvalida(
                    "A escala 12x36 exige 12 horas entre o início e o término do plantão.");
        }

        long duracaoIntervalos = validarIntervalos(turno, duracaoTurno);
        long cargaEfetiva = duracaoTurno - duracaoIntervalos;
        if (cargaEfetiva <= 0) {
            throw requisicaoInvalida("A carga horária efetiva deve ser positiva.");
        }
        if (cargaEfetiva > Integer.MAX_VALUE) {
            throw requisicaoInvalida("A carga horária calculada excede o limite permitido.");
        }

        validarCargaContratual(turno.getCargaDiariaContratualMinutos());
        validarCargaContratual(turno.getCargaSemanalContratualMinutos());
        validarCargaContratual(turno.getCargaMensalPadraoMinutos());

        turno.setCargaEfetivaMinutos((int) cargaEfetiva);
        turno.setCargaSemanalPrevistaMinutos(calcularCargaSemanalPrevista(turno, cargaEfetiva));
        validarDescansoSemanal(turno);
    }

    public static long duracaoTurnoMinutos(Turno turno) {
        long inicio = minutoDoDia(turno.getHorarioInicio());
        long fim = minutoDoDia(turno.getHorarioFim());
        if (fim <= inicio) {
            fim += 24 * 60L;
        }
        return fim - inicio;
    }

    public static List<Ocorrencia> ocorrencias(
            Turno turno, LocalDateTime inicioInclusivo, LocalDateTime fimExclusivo) {
        if (inicioInclusivo == null || fimExclusivo == null
                || !fimExclusivo.isAfter(inicioInclusivo)) {
            throw requisicaoInvalida("Informe um período de consulta válido.");
        }

        List<Ocorrencia> ocorrencias = new ArrayList<>();
        long duracao = duracaoTurnoMinutos(turno);
        LocalDate primeiroDia = inicioInclusivo.toLocalDate().minusDays(1);
        LocalDate ultimoDia = fimExclusivo.toLocalDate();

        if (ESCALA_12X36.equals(turno.getTipoEscala())) {
            LocalDateTime referencia = turno.getDataReferencia().atTime(turno.getHorarioInicio());
            long distanciaMinutos = ChronoUnit.MINUTES.between(referencia, inicioInclusivo);
            long cicloInicial = Math.floorDiv(distanciaMinutos, CICLO_12X36_MINUTOS) - 1;
            LocalDateTime inicio = referencia.plusMinutes(cicloInicial * CICLO_12X36_MINUTOS);
            while (inicio.isBefore(fimExclusivo)) {
                adicionarSeSobrepoe(ocorrencias, inicio, duracao, inicioInclusivo, fimExclusivo);
                inicio = inicio.plusMinutes(CICLO_12X36_MINUTOS);
            }
        } else {
            for (LocalDate dia = primeiroDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
                if (turno.getDiasSemana().contains(dia.getDayOfWeek())) {
                    LocalDateTime inicio = dia.atTime(turno.getHorarioInicio());
                    adicionarSeSobrepoe(ocorrencias, inicio, duracao, inicioInclusivo, fimExclusivo);
                }
            }
        }
        ocorrencias.sort(Comparator.comparing(Ocorrencia::inicio));
        return ocorrencias;
    }

    public static boolean trabalhaNaData(Turno turno, LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        return ocorrencias(turno, inicioDia, inicioDia.plusDays(1)).stream()
                .anyMatch(ocorrencia -> ocorrencia.inicio().toLocalDate().equals(data));
    }

    public static Ocorrencia ultimaOcorrenciaAntes(
            Turno turno, LocalDateTime limiteExclusivo, LocalDateTime vigenciaMinima) {
        LocalDateTime inicioBusca = limiteExclusivo.minusDays(15);
        return ocorrencias(turno, inicioBusca, limiteExclusivo).stream()
                .filter(ocorrencia -> !ocorrencia.inicio().isBefore(vigenciaMinima))
                .filter(ocorrencia -> !ocorrencia.fim().isAfter(limiteExclusivo))
                .max(Comparator.comparing(Ocorrencia::inicio))
                .orElse(null);
    }

    public static Ocorrencia primeiraOcorrenciaApos(
            Turno turno, LocalDateTime limiteInclusivo) {
        LocalDateTime fimBusca = limiteInclusivo.plusDays(15);
        return ocorrencias(turno, limiteInclusivo, fimBusca).stream()
                .filter(ocorrencia -> !ocorrencia.inicio().isBefore(limiteInclusivo))
                .min(Comparator.comparing(Ocorrencia::inicio))
                .orElse(null);
    }

    public static void validarTransicao(
            Turno jornadaAnterior, Turno novaJornada, LocalDateTime vigencia) {
        if (!possuiConfiguracaoJornada(jornadaAnterior)
                || !possuiConfiguracaoJornada(novaJornada)) {
            return;
        }

        LocalDateTime inicioBusca = vigencia.minusDays(15);
        boolean plantaoEmAndamento = ocorrencias(
                        jornadaAnterior, inicioBusca, vigencia.plusDays(1)).stream()
                .anyMatch(ocorrencia -> ocorrencia.inicio().isBefore(vigencia)
                        && ocorrencia.fim().isAfter(vigencia));
        if (plantaoEmAndamento) {
            throw requisicaoInvalida(
                    "A vigência da nova jornada não pode interromper um plantão em andamento.");
        }

        Ocorrencia anterior = ultimaOcorrenciaAntes(
                jornadaAnterior, vigencia, inicioBusca);
        Ocorrencia proxima = primeiraOcorrenciaApos(novaJornada, vigencia);
        if (anterior != null && proxima != null) {
            long descanso = Duration.between(anterior.fim(), proxima.inicio()).toMinutes();
            if (descanso < DESCANSO_MINIMO_MINUTOS) {
                throw requisicaoInvalida(
                        "A troca de jornada deve garantir pelo menos 11 horas de descanso.");
            }
        }
    }

    public static Optional<ConflitoSobreposicao> encontrarSobreposicao(
            Turno primeiro,
            LocalDateTime vigenciaInicioPrimeiro,
            LocalDateTime vigenciaFimPrimeiro,
            Turno segundo,
            LocalDateTime vigenciaInicioSegundo,
            LocalDateTime vigenciaFimSegundo) {
        if (!possuiConfiguracaoJornada(primeiro)
                || !possuiConfiguracaoJornada(segundo)) {
            return Optional.empty();
        }

        LocalDateTime inicioComum = maximo(vigenciaInicioPrimeiro, vigenciaInicioSegundo);
        LocalDateTime fimComum = minimo(vigenciaFimPrimeiro, vigenciaFimSegundo);
        if (fimComum != null && !fimComum.isAfter(inicioComum)) {
            return Optional.empty();
        }

        // Weekly and 12x36 schedules repeat together every 14 days.
        LocalDateTime fimBusca = fimComum == null
                || fimComum.isAfter(inicioComum.plusDays(15))
                ? inicioComum.plusDays(15) : fimComum;
        List<Ocorrencia> ocorrenciasPrimeiro =
                ocorrencias(primeiro, inicioComum.minusDays(1), fimBusca);
        List<Ocorrencia> ocorrenciasSegundo =
                ocorrencias(segundo, inicioComum.minusDays(1), fimBusca);

        ConflitoSobreposicao conflitoTrabalho = null;
        ConflitoSobreposicao conflitoPausa = null;
        for (Ocorrencia ocorrenciaPrimeiro : ocorrenciasPrimeiro) {
            for (Ocorrencia ocorrenciaSegundo : ocorrenciasSegundo) {
                Ocorrencia sobreposicaoOcorrencias = intersecao(
                        ocorrenciaPrimeiro, ocorrenciaSegundo, inicioComum, fimBusca);
                if (sobreposicaoOcorrencias == null) {
                    continue;
                }

                for (Ocorrencia segmentoPrimeiro : segmentosTrabalho(
                        primeiro, ocorrenciaPrimeiro)) {
                    for (Ocorrencia segmentoSegundo : segmentosTrabalho(
                            segundo, ocorrenciaSegundo)) {
                        Ocorrencia sobreposicaoTrabalho = intersecao(
                                segmentoPrimeiro, segmentoSegundo, inicioComum, fimBusca);
                        if (sobreposicaoTrabalho != null) {
                            conflitoTrabalho = conflitoMaisAntigo(
                                    conflitoTrabalho, sobreposicaoTrabalho);
                        }
                    }
                }

                conflitoPausa = conflitoMaisAntigo(conflitoPausa, sobreposicaoOcorrencias);
            }
        }

        // A pausa não é uma autorização para atribuir outra jornada simultânea.
        return Optional.ofNullable(conflitoTrabalho != null ? conflitoTrabalho : conflitoPausa);
    }

    private static List<Ocorrencia> segmentosTrabalho(Turno turno, Ocorrencia ocorrencia) {
        long duracao = Duration.between(ocorrencia.inicio(), ocorrencia.fim()).toMinutes();
        long inicioTurno = minutoDoDia(turno.getHorarioInicio());
        List<long[]> intervalos = (turno.getIntervalos() == null
                ? List.<IntervaloJornada>of() : turno.getIntervalos()).stream()
                .map(intervalo -> {
                    long inicio = minutoDoDia(intervalo.getInicio()) - inicioTurno;
                    if (inicio < 0) {
                        inicio += 24 * 60L;
                    }
                    long fim = minutoDoDia(intervalo.getFim()) - inicioTurno;
                    if (fim <= inicio) {
                        fim += 24 * 60L;
                    }
                    return new long[]{inicio, fim};
                })
                .sorted(Comparator.comparingLong(intervalo -> intervalo[0]))
                .toList();

        List<Ocorrencia> segmentos = new ArrayList<>();
        long cursor = 0;
        for (long[] intervalo : intervalos) {
            if (intervalo[0] > cursor) {
                segmentos.add(new Ocorrencia(
                        ocorrencia.inicio().plusMinutes(cursor),
                        ocorrencia.inicio().plusMinutes(intervalo[0])));
            }
            cursor = Math.max(cursor, intervalo[1]);
        }
        if (cursor < duracao) {
            segmentos.add(new Ocorrencia(
                    ocorrencia.inicio().plusMinutes(cursor), ocorrencia.fim()));
        }
        return segmentos;
    }

    private static Ocorrencia intersecao(
            Ocorrencia primeiro, Ocorrencia segundo,
            LocalDateTime inicioLimite, LocalDateTime fimLimite) {
        LocalDateTime inicio = maximo(
                inicioLimite, maximo(primeiro.inicio(), segundo.inicio()));
        LocalDateTime fim = minimo(
                fimLimite, minimo(primeiro.fim(), segundo.fim()));
        return fim == null || !fim.isAfter(inicio) ? null : new Ocorrencia(inicio, fim);
    }

    private static ConflitoSobreposicao conflitoMaisAntigo(
            ConflitoSobreposicao atual, Ocorrencia candidato) {
        if (atual == null || candidato.inicio().isBefore(atual.inicio())) {
            return new ConflitoSobreposicao(candidato.inicio(), candidato.fim());
        }
        return atual;
    }

    private static LocalDateTime maximo(LocalDateTime primeiro, LocalDateTime segundo) {
        return primeiro.isAfter(segundo) ? primeiro : segundo;
    }

    private static LocalDateTime minimo(LocalDateTime primeiro, LocalDateTime segundo) {
        if (primeiro == null) {
            return segundo;
        }
        return segundo == null || primeiro.isBefore(segundo) ? primeiro : segundo;
    }

    private static long validarIntervalos(Turno turno, long duracaoTurno) {
        List<IntervaloJornada> intervalos = turno.getIntervalos() == null
                ? List.of() : turno.getIntervalos();
        List<long[]> intervalosMinutos = new ArrayList<>();
        long total = 0;
        long inicioTurno = minutoDoDia(turno.getHorarioInicio());

        for (IntervaloJornada intervalo : intervalos) {
            if (intervalo.getInicio() == null || intervalo.getFim() == null) {
                throw requisicaoInvalida("Informe o início e o término de cada intervalo.");
            }
            long inicio = minutoDoDia(intervalo.getInicio()) - inicioTurno;
            if (inicio < 0) {
                inicio += 24 * 60L;
            }
            long fim = minutoDoDia(intervalo.getFim()) - inicioTurno;
            if (fim <= inicio) {
                fim += 24 * 60L;
            }
            if (fim <= inicio || fim > duracaoTurno) {
                throw requisicaoInvalida("Cada intervalo deve estar dentro do período da jornada.");
            }
            intervalosMinutos.add(new long[]{inicio, fim});
            total += fim - inicio;
        }

        intervalosMinutos.sort(Comparator.comparingLong(intervalo -> intervalo[0]));
        for (int indice = 1; indice < intervalosMinutos.size(); indice++) {
            if (intervalosMinutos.get(indice)[0] < intervalosMinutos.get(indice - 1)[1]) {
                throw requisicaoInvalida("Os intervalos da jornada não podem se sobrepor.");
            }
        }
        return total;
    }

    private static void validarDescansoSemanal(Turno turno) {
        if (ESCALA_12X36.equals(turno.getTipoEscala())) {
            return;
        }
        LocalDateTime inicio = LocalDate.of(2024, 1, 1).atStartOfDay();
        List<Ocorrencia> ocorrencias = ocorrencias(turno, inicio, inicio.plusDays(16));
        for (int indice = 1; indice < ocorrencias.size(); indice++) {
            long descanso = Duration.between(
                    ocorrencias.get(indice - 1).fim(),
                    ocorrencias.get(indice).inicio()).toMinutes();
            if (descanso < DESCANSO_MINIMO_MINUTOS) {
                throw requisicaoInvalida(
                        "A jornada deve garantir pelo menos 11 horas de descanso entre turnos.");
            }
        }
    }

    private static int calcularCargaSemanalPrevista(Turno turno, long cargaEfetiva) {
        long minutos = ESCALA_12X36.equals(turno.getTipoEscala())
                ? (cargaEfetiva * 7 + 1) / 2
                : cargaEfetiva * turno.getDiasSemana().size();
        if (minutos > Integer.MAX_VALUE) {
            throw requisicaoInvalida("A carga horária semanal excede o limite permitido.");
        }
        return (int) minutos;
    }

    private static void validarCargaContratual(Integer carga) {
        if (carga != null && carga < 0) {
            throw requisicaoInvalida("As cargas contratuais devem ser zero ou positivas.");
        }
    }

    private static void adicionarSeSobrepoe(
            List<Ocorrencia> ocorrencias,
            LocalDateTime inicio,
            long duracaoMinutos,
            LocalDateTime inicioInclusivo,
            LocalDateTime fimExclusivo) {
        LocalDateTime fim = inicio.plusMinutes(duracaoMinutos);
        if (inicio.isBefore(fimExclusivo) && fim.isAfter(inicioInclusivo)) {
            ocorrencias.add(new Ocorrencia(inicio, fim));
        }
    }

    private static long minutoDoDia(LocalTime horario) {
        return horario.getHour() * 60L + horario.getMinute();
    }

    private static ResponseStatusException requisicaoInvalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public record Ocorrencia(LocalDateTime inicio, LocalDateTime fim) {
    }

    public record ConflitoSobreposicao(LocalDateTime inicio, LocalDateTime fim) {
    }
}
