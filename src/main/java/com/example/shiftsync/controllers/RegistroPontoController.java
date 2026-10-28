package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.RegistroPontoRequest;
import com.example.shiftsync.DTO.RegistroPontoResponse;
import com.example.shiftsync.DTO.ResumoMensalPontoResponse;
import com.example.shiftsync.DTO.TipoMarcacaoPonto;
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
import com.example.shiftsync.utils.CpfUtils;
import com.example.shiftsync.utils.RegrasJornada;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoints de consulta, marcação e apuração mensal do ponto.
 *
 * A identidade do funcionário é conferida com o CPF presente no JWT. As marcações
 * usam o Clock injetado, respeitam a sequência temporal e são registradas também
 * na tabela de auditoria.
 */
@RestController
@RequestMapping("/ponto")
public class RegistroPontoController {

    private final FuncionarioRepository funcionarioRepository;
    private final AtribuicaoTurnoRepository atribuicaoTurnoRepository;
    private final RealizacaoTurnoRepository realizacaoTurnoRepository;
    private final RegistroPontoAuditoriaRepository auditoriaRepository;
    private final Clock clock;

    /** Recebe os repositórios e o relógio usados pelo fluxo de ponto. */
    public RegistroPontoController(
            FuncionarioRepository funcionarioRepository,
            AtribuicaoTurnoRepository atribuicaoTurnoRepository,
            RealizacaoTurnoRepository realizacaoTurnoRepository,
            RegistroPontoAuditoriaRepository auditoriaRepository,
            Clock clock) {
        this.funcionarioRepository = funcionarioRepository;
        this.atribuicaoTurnoRepository = atribuicaoTurnoRepository;
        this.realizacaoTurnoRepository = realizacaoTurnoRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.clock = clock;
    }

    /** Identifica o titular do token sem expor outros dados cadastrais. */
    @GetMapping("/funcionario")
    @Operation(summary = "Confirma o funcionário autenticado pelo CPF")
    public ResponseEntity<FuncionarioIdentificacao> consultarFuncionario(
            @RequestParam String cpf, HttpServletRequest request) {
        Funcionario funcionario = buscarFuncionarioAutenticado(cpf, request);
        return ResponseEntity.ok(
                new FuncionarioIdentificacao(funcionario.getId(), funcionario.getNome()));
    }

    /** Consulta as marcações de uma jornada do funcionário autenticado. */
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Consulta o registro de ponto de uma jornada")
    public ResponseEntity<RegistroPontoResponse> consultarPonto(
            @RequestParam String cpf,
            @RequestParam LocalDate data,
            HttpServletRequest request) {
        Funcionario funcionario = buscarFuncionarioAutenticado(cpf, request);
        OcorrenciaFuncionario ocorrencia = buscarOcorrencia(funcionario, data);
        RealizacaoTurno realizacao = realizacaoTurnoRepository
                .findByAtribuicao_IdAndInicioPlanejado(
                        ocorrencia.atribuicao().getId(), ocorrencia.ocorrencia().inicio())
                .orElse(null);
        return ResponseEntity.ok(criarResposta(funcionario, data, ocorrencia, realizacao));
    }

    /**
     * Monta o calendário de jornadas do período e associa cada realização existente.
     * O limite de 32 dias evita consultas arbitrariamente grandes.
     */
    @GetMapping("/periodo")
    @Transactional(readOnly = true)
    @Operation(summary = "Consulta os registros de ponto em um período")
    public ResponseEntity<List<RegistroPontoResponse>> consultarPeriodo(
            @RequestParam String cpf,
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            HttpServletRequest request) {
        Funcionario funcionario = buscarFuncionarioAutenticado(cpf, request);
        if (fim.isBefore(inicio) || ChronoUnit.DAYS.between(inicio, fim) > 31) {
            throw requisicaoInvalida(
                    "Informe um período válido de até 32 dias corridos.");
        }
        LocalDateTime inicioPeriodo = inicio.atStartOfDay();
        LocalDateTime fimPeriodo = fim.plusDays(1).atStartOfDay();
        Map<ChaveOcorrencia, RealizacaoTurno> porOcorrencia = new HashMap<>();
        realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(funcionario.getId())
                .forEach(realizacao -> porOcorrencia.put(chave(realizacao), realizacao));

        List<OcorrenciaFuncionario> ocorrencias = new ArrayList<>();
        for (AtribuicaoTurno atribuicao : atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(funcionario.getId())) {
            Turno turno = atribuicao.getTurno();
            if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
                continue;
            }
            RegrasJornada.ocorrencias(turno, inicioPeriodo.minusDays(1), fimPeriodo)
                    .stream()
                    .filter(ocorrencia -> !ocorrencia.inicio().isBefore(inicioPeriodo)
                            && ocorrencia.inicio().isBefore(fimPeriodo))
                    .filter(ocorrencia -> dentroDaVigencia(atribuicao, ocorrencia))
                    .map(ocorrencia -> new OcorrenciaFuncionario(atribuicao, ocorrencia))
                    .forEach(ocorrencias::add);
        }
        return ResponseEntity.ok(ocorrencias.stream()
                .sorted(Comparator.comparing(
                        item -> item.ocorrencia().inicio()))
                .map(item -> criarResposta(
                        funcionario,
                        item.ocorrencia().inicio().toLocalDate(),
                        item,
                        porOcorrencia.get(new ChaveOcorrencia(
                                item.atribuicao().getId(), item.ocorrencia().inicio()))))
                .toList());
    }

    /**
     * Grava a próxima etapa cronológica (entrada, saída/retorno de intervalo ou saída final).
     *
     * A operação é transacional e bloqueia o funcionário para evitar duas marcações
     * concorrentes; a mesma data e o tipo de marcação são registrados para auditoria.
     */
    @PostMapping("/marcacoes")
    @Transactional
    @Operation(summary = "Registra a próxima marcação de ponto com horário do servidor")
    public ResponseEntity<RegistroPontoResponse> registrarMarcacao(
            @RequestBody RegistroPontoRequest requestBody,
            HttpServletRequest request) {
        if (requestBody == null || requestBody.getData() == null
                || requestBody.getMarcacao() == null) {
            throw requisicaoInvalida("Informe o CPF, a data e o tipo de marcação.");
        }
        Funcionario funcionario = buscarFuncionarioAutenticado(requestBody.getCpf(), request);
        Funcionario funcionarioBloqueado = funcionarioRepository
                .findByIdForUpdate(funcionario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Funcionário não encontrado."));
        validarFuncionarioAtivo(funcionarioBloqueado);

        LocalDateTime agora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        OcorrenciaFuncionario ocorrencia = buscarOcorrencia(
                funcionarioBloqueado, requestBody.getData());
        List<IntervaloJornada> intervalosJornada =
                ocorrencia.atribuicao().getTurno().getIntervalos();
        if (intervalosJornada != null && intervalosJornada.size() > 1) {
            throw requisicaoInvalida(
                    "O registro com quatro marcações suporta jornadas com até um intervalo.");
        }
        validarJanelaDeRegistro(requestBody.getData(), ocorrencia.ocorrencia(), agora);

        RealizacaoTurno realizacao = realizacaoTurnoRepository
                .findByAtribuicao_IdAndInicioPlanejado(
                        ocorrencia.atribuicao().getId(), ocorrencia.ocorrencia().inicio())
                .orElseGet(() -> novaRealizacao(ocorrencia));
        validarProximaMarcacao(realizacao, requestBody.getMarcacao(), agora);
        aplicarMarcacao(realizacao, requestBody.getMarcacao(), agora);
        realizacao.setConfirmadoEm(agora);
        RealizacaoTurno salva = realizacaoTurnoRepository.save(realizacao);

        RegistroPontoAuditoria auditoria = new RegistroPontoAuditoria();
        auditoria.setRealizacao(salva);
        auditoria.setMarcacao(requestBody.getMarcacao().name());
        auditoria.setRegistradoEm(agora);
        auditoria.setRegistradoPorCpf(CpfUtils.normalizar(requestBody.getCpf()));
        auditoriaRepository.save(auditoria);

        return ResponseEntity.ok(criarResposta(
                funcionarioBloqueado, requestBody.getData(), ocorrencia, salva));
    }

    /**
     * Consolida horas previstas/realizadas e ocorrências do mês informado.
     * A apuração mensal de extras depende de uma carga padrão vigente; sem ela,
     * o campo de extras permanece indisponível em vez de assumir uma regra.
     */
    @GetMapping("/resumo-mensal")
    @Transactional(readOnly = true)
    @Operation(summary = "Consulta a apuração mensal e as ocorrências de ponto")
    public ResponseEntity<ResumoMensalPontoResponse> consultarResumoMensal(
            @RequestParam String cpf,
            @RequestParam String mes,
            HttpServletRequest request) {
        Funcionario funcionario = buscarFuncionarioAutenticado(cpf, request);
        YearMonth referencia;
        try {
            referencia = YearMonth.parse(mes);
        } catch (DateTimeParseException excecao) {
            throw requisicaoInvalida("O mês deve estar no formato yyyy-MM.");
        }

        LocalDateTime inicioMes = referencia.atDay(1).atStartOfDay();
        LocalDateTime fimMes = referencia.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime agora = LocalDateTime.now(clock);
        List<RealizacaoTurno> realizacoes =
                realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(funcionario.getId());
        Map<ChaveOcorrencia, RealizacaoTurno> porOcorrencia = new HashMap<>();
        realizacoes.forEach(realizacao -> porOcorrencia.put(chave(realizacao), realizacao));

        int minutosConcluidos = realizacoes.stream()
                .filter(realizacao -> realizacao.getStatus() == StatusRealizacaoTurno.REALIZADO)
                .flatMap(realizacao -> faixasTrabalhadas(realizacao).stream())
                .map(faixa -> recortar(faixa, inicioMes, fimMes))
                .flatMap(Optional::stream)
                .mapToInt(faixa -> Math.toIntExact(
                        Duration.between(faixa.inicio(), faixa.fim()).toMinutes()))
                .sum();
        int minutosTrabalhados = minutosConcluidos;
        int registrosIncompletos = 0;
        for (RealizacaoTurno realizacao : realizacoes) {
            if (realizacao.getStatus() == StatusRealizacaoTurno.EM_ANDAMENTO) {
                if (estaNoMes(realizacao.getInicioPlanejado(), inicioMes, fimMes)) {
                    registrosIncompletos++;
                }
                minutosTrabalhados += minutosParciaisConfirmados(realizacao, inicioMes, fimMes);
            }
        }

        int minutosPrevistos = 0;
        int faltas = 0;
        int pendencias = 0;
        int quantidadeAtrasos = 0;
        int minutosAtraso = 0;
        int quantidadeSaidasAntecipadas = 0;
        int minutosSaidasAntecipadas = 0;
        int intervalosExcedentes = 0;
        int intervalosInsuficientes = 0;
        for (AtribuicaoTurno atribuicao : atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(funcionario.getId())) {
            Turno turno = atribuicao.getTurno();
            if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
                continue;
            }
            for (RegrasJornada.Ocorrencia planejada : RegrasJornada.ocorrencias(
                    turno, inicioMes.minusDays(1), fimMes)) {
                if (!dentroDaVigencia(atribuicao, planejada)) {
                    continue;
                }
                minutosPrevistos += minutosPrevistosNoMes(
                        turno, planejada, inicioMes, fimMes);
                if (!estaNoMes(planejada.inicio(), inicioMes, fimMes)) {
                    continue;
                }
                RealizacaoTurno realizacao = porOcorrencia.get(
                        new ChaveOcorrencia(atribuicao.getId(), planejada.inicio()));
                if (realizacao == null) {
                    if (agora.isAfter(planejada.fim())) {
                        faltas++;
                    } else {
                        pendencias++;
                    }
                    continue;
                }
                if (realizacao.getStatus() == StatusRealizacaoTurno.NAO_REALIZADO) {
                    if (agora.isAfter(planejada.fim())) {
                        faltas++;
                    }
                    continue;
                }
                if (realizacao.getInicioReal() == null) {
                    continue;
                }
                if (realizacao.getInicioReal().isAfter(planejada.inicio())) {
                    quantidadeAtrasos++;
                    minutosAtraso += Math.toIntExact(Duration.between(
                            planejada.inicio(), realizacao.getInicioReal()).toMinutes());
                }
                if (realizacao.getStatus() != StatusRealizacaoTurno.REALIZADO) {
                    intervalosExcedentes += minutosIntervaloExcedente(turno, realizacao);
                    intervalosInsuficientes += minutosIntervaloInsuficiente(turno, realizacao);
                    continue;
                }
                if (realizacao.getFimReal() != null
                        && realizacao.getFimReal().isBefore(planejada.fim())) {
                    quantidadeSaidasAntecipadas++;
                    minutosSaidasAntecipadas += Math.toIntExact(Duration.between(
                            realizacao.getFimReal(), planejada.fim()).toMinutes());
                }
                intervalosExcedentes += minutosIntervaloExcedente(turno, realizacao);
                intervalosInsuficientes += minutosIntervaloInsuficiente(turno, realizacao);
            }
        }

        Integer cargaMensal = cargaMensalVigente(funcionario.getId(), referencia.atDay(1));
        Integer extrasApuradas = cargaMensal == null
                ? null : Math.max(0, minutosConcluidos - cargaMensal);
        Integer excedentesPrevistos = Math.max(0, minutosTrabalhados - minutosPrevistos);
        return ResponseEntity.ok(new ResumoMensalPontoResponse(
                funcionario.getId(),
                funcionario.getNome(),
                referencia.toString(),
                minutosTrabalhados,
                minutosPrevistos,
                excedentesPrevistos,
                extrasApuradas,
                faltas,
                pendencias,
                registrosIncompletos,
                quantidadeAtrasos,
                minutosAtraso,
                quantidadeSaidasAntecipadas,
                minutosSaidasAntecipadas,
                intervalosExcedentes,
                intervalosInsuficientes));
    }

    /**
     * Confere o CPF informado contra o assunto do JWT e localiza um único funcionário ativo.
     *
     * Rejeita CPF inválido, sessão sem identidade, tentativa de consultar outra pessoa,
     * ausência do cadastro e duplicidade de CPF, usando códigos HTTP distintos.
     */
    private Funcionario buscarFuncionarioAutenticado(
            String cpf, HttpServletRequest request) {
        if (!CpfUtils.isValidCpf(cpf)) {
            throw requisicaoInvalida("Informe um CPF válido.");
        }
        Object cpfAutenticado = request.getAttribute("authenticatedCpf");
        String cpfNormalizado = CpfUtils.normalizar(cpf);
        if (!(cpfAutenticado instanceof String cpfDaSessao) || cpfDaSessao.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "A sessão autenticada não identificou o usuário.");
        }
        if (!cpfNormalizado.equals(CpfUtils.normalizar(cpfDaSessao))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "O registro de ponto só pode ser consultado pelo próprio funcionário.");
        }
        List<Funcionario> encontrados =
                funcionarioRepository.findAllByCpfNormalizado(cpfNormalizado);
        if (encontrados.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Funcionário não encontrado.");
        }
        if (encontrados.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O CPF está associado a mais de um funcionário; procure o administrador.");
        }
        Funcionario funcionario = encontrados.get(0);
        validarFuncionarioAtivo(funcionario);
        return funcionario;
    }

    /** Exige status ativo antes de permitir consultas ou novas marcações do titular. */
    private void validarFuncionarioAtivo(Funcionario funcionario) {
        if (!"A".equalsIgnoreCase(funcionario.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "O funcionário não está ativo para registrar ponto.");
        }
    }

    /**
     * Localiza a jornada cuja ocorrência começa na data indicada e está na vigência.
     *
     * A busca inclui dias vizinhos para permitir plantões noturnos; ao final exige
     * exatamente uma ocorrência, retornando 404 ou 409 se houver zero ou várias.
     */
    private OcorrenciaFuncionario buscarOcorrencia(Funcionario funcionario, LocalDate data) {
        LocalDateTime inicioBusca = data.minusDays(1).atStartOfDay();
        LocalDateTime fimBusca = data.plusDays(2).atStartOfDay();
        List<OcorrenciaFuncionario> ocorrencias = new ArrayList<>();
        for (AtribuicaoTurno atribuicao : atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(funcionario.getId())) {
            Turno turno = atribuicao.getTurno();
            if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
                continue;
            }
            RegrasJornada.ocorrencias(turno, inicioBusca, fimBusca).stream()
                    .filter(ocorrencia -> ocorrencia.inicio().toLocalDate().equals(data))
                    .filter(ocorrencia -> dentroDaVigencia(atribuicao, ocorrencia))
                    .map(ocorrencia -> new OcorrenciaFuncionario(atribuicao, ocorrencia))
                    .forEach(ocorrencias::add);
        }
        if (ocorrencias.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Não há jornada prevista para esta data.");
        }
        if (ocorrencias.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Há mais de uma jornada prevista para esta data.");
        }
        return ocorrencias.get(0);
    }

    /** Considera início inclusivo e fim exclusivo ao verificar se uma jornada estava atribuída. */
    private boolean dentroDaVigencia(
            AtribuicaoTurno atribuicao, RegrasJornada.Ocorrencia ocorrencia) {
        return !ocorrencia.inicio().isBefore(atribuicao.getVigenciaInicio())
                && (atribuicao.getVigenciaFim() == null
                || ocorrencia.inicio().isBefore(atribuicao.getVigenciaFim()));
    }

    /** Restringe a marcação à jornada atual, incluindo pequena tolerância após seu término. */
    private void validarJanelaDeRegistro(
            LocalDate data, RegrasJornada.Ocorrencia ocorrencia, LocalDateTime agora) {
        boolean dataAtual = data.equals(agora.toLocalDate());
        boolean jornadaNoturnaEmCurso = data.equals(agora.toLocalDate().minusDays(1))
                && ocorrencia.fim().toLocalDate().equals(agora.toLocalDate())
                && agora.isBefore(ocorrencia.fim().plusHours(4));
        if ((!dataAtual && !jornadaNoturnaEmCurso)
                || agora.isBefore(ocorrencia.inicio().minusHours(12))
                || agora.isAfter(ocorrencia.fim().plusHours(4))) {
            throw requisicaoInvalida(
                    "As marcações só podem ser feitas durante a jornada atual.");
        }
    }

    /** Cria o registro inicial em andamento para uma ocorrência ainda sem marcações. */
    private RealizacaoTurno novaRealizacao(OcorrenciaFuncionario ocorrencia) {
        RealizacaoTurno realizacao = new RealizacaoTurno();
        realizacao.setAtribuicao(ocorrencia.atribuicao());
        realizacao.setInicioPlanejado(ocorrencia.ocorrencia().inicio());
        realizacao.setFimPlanejado(ocorrencia.ocorrencia().fim());
        realizacao.setStatus(StatusRealizacaoTurno.EM_ANDAMENTO);
        realizacao.setConfirmadoEm(LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES));
        return realizacao;
    }

    /** Exige a próxima etapa esperada e impede horários fora de ordem ou finalizações repetidas. */
    private void validarProximaMarcacao(
            RealizacaoTurno realizacao, TipoMarcacaoPonto marcacao, LocalDateTime agora) {
        if (realizacao.getStatus() == StatusRealizacaoTurno.REALIZADO
                || realizacao.getStatus() == StatusRealizacaoTurno.NAO_REALIZADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Esta jornada já possui um registro finalizado.");
        }
        TipoMarcacaoPonto proxima = realizacao.getInicioReal() == null
                ? TipoMarcacaoPonto.ENTRADA
                : realizacao.getInicioIntervaloReal() == null
                ? TipoMarcacaoPonto.SAIDA_INTERVALO
                : realizacao.getFimIntervaloReal() == null
                ? TipoMarcacaoPonto.RETORNO_INTERVALO
                : realizacao.getFimReal() == null
                ? TipoMarcacaoPonto.SAIDA_FINAL : null;
        if (marcacao != proxima) {
            throw requisicaoInvalida(proxima == null
                    ? "Todas as marcações desta jornada já foram registradas."
                    : "A próxima marcação deve ser: " + proxima.name() + ".");
        }
        LocalDateTime anterior = switch (marcacao) {
            case ENTRADA -> null;
            case SAIDA_INTERVALO -> realizacao.getInicioReal();
            case RETORNO_INTERVALO -> realizacao.getInicioIntervaloReal();
            case SAIDA_FINAL -> realizacao.getFimIntervaloReal();
        };
        if (anterior != null && !agora.isAfter(anterior)) {
            throw requisicaoInvalida(
                    "A nova marcação precisa ocorrer depois da marcação anterior.");
        }
    }

    /** Atualiza o campo correspondente à marcação e conclui a realização na saída final. */
    private void aplicarMarcacao(
            RealizacaoTurno realizacao, TipoMarcacaoPonto marcacao, LocalDateTime agora) {
        switch (marcacao) {
            case ENTRADA -> realizacao.setInicioReal(agora);
            case SAIDA_INTERVALO -> realizacao.setInicioIntervaloReal(agora);
            case RETORNO_INTERVALO -> {
                realizacao.setFimIntervaloReal(agora);
                realizacao.setIntervalosNaoTrabalhados(List.of(
                        new IntervaloRealizacao(realizacao.getInicioIntervaloReal(), agora)));
            }
            case SAIDA_FINAL -> {
                realizacao.setFimReal(agora);
                realizacao.setStatus(StatusRealizacaoTurno.REALIZADO);
            }
        }
    }

    /**
     * Converte a ocorrência e suas marcações em resposta para a interface.
     * Calcula também status, horário previsto de pausa e total efetivo quando concluído.
     */
    private RegistroPontoResponse criarResposta(
            Funcionario funcionario,
            LocalDate data,
            OcorrenciaFuncionario ocorrencia,
            RealizacaoTurno realizacao) {
        Turno turno = ocorrencia.atribuicao().getTurno();
        LocalDateTime intervaloInicio = null;
        LocalDateTime intervaloFim = null;
        List<IntervaloJornada> intervalos = turno.getIntervalos() == null
                ? List.of() : turno.getIntervalos();
        if (!intervalos.isEmpty()) {
            IntervaloJornada intervalo = intervalos.get(0);
            long inicioRelativo = minutosDoDia(intervalo.getInicio())
                    - minutosDoDia(turno.getHorarioInicio());
            if (inicioRelativo < 0) {
                inicioRelativo += 24 * 60L;
            }
            long fimRelativo = minutosDoDia(intervalo.getFim())
                    - minutosDoDia(turno.getHorarioInicio());
            if (fimRelativo <= inicioRelativo) {
                fimRelativo += 24 * 60L;
            }
            intervaloInicio = ocorrencia.ocorrencia().inicio().plusMinutes(inicioRelativo);
            intervaloFim = ocorrencia.ocorrencia().inicio().plusMinutes(fimRelativo);
        }

        String status = realizacao == null ? "PENDENTE"
                : realizacao.getStatus() == StatusRealizacaoTurno.EM_ANDAMENTO
                ? "EM_ANDAMENTO"
                : realizacao.getStatus() == StatusRealizacaoTurno.REALIZADO
                && irregular(turno, ocorrencia.ocorrencia(), realizacao)
                ? "IRREGULAR" : "CONCLUIDO";
        Integer minutosTrabalhados = realizacao != null
                && realizacao.getStatus() == StatusRealizacaoTurno.REALIZADO
                ? minutosEfetivos(realizacao) : null;
        LocalDateTime saidaIntervalo = realizacao == null
                ? null : realizacao.getInicioIntervaloReal();
        LocalDateTime retornoIntervalo = realizacao == null
                ? null : realizacao.getFimIntervaloReal();
        if (realizacao != null && saidaIntervalo == null
                && !realizacao.getIntervalosNaoTrabalhados().isEmpty()) {
            saidaIntervalo = realizacao.getIntervalosNaoTrabalhados().get(0).getInicio();
            retornoIntervalo = realizacao.getIntervalosNaoTrabalhados().get(0).getFim();
        }
        return new RegistroPontoResponse(
                funcionario.getId(),
                funcionario.getNome(),
                data,
                ocorrencia.ocorrencia().inicio(),
                ocorrencia.ocorrencia().fim(),
                intervaloInicio,
                intervaloFim,
                realizacao == null ? null : realizacao.getInicioReal(),
                saidaIntervalo,
                retornoIntervalo,
                realizacao == null ? null : realizacao.getFimReal(),
                status,
                minutosTrabalhados);
    }

    /** Identifica desvios de horário e de duração de intervalo na realização concluída. */
    private boolean irregular(
            Turno turno, RegrasJornada.Ocorrencia planejada, RealizacaoTurno realizacao) {
        return realizacao.getInicioReal().isAfter(planejada.inicio())
                || realizacao.getFimReal().isBefore(planejada.fim())
                || minutosIntervaloExcedente(turno, realizacao) > 0
                || minutosIntervaloInsuficiente(turno, realizacao) > 0;
    }

    /** Soma apenas os segmentos trabalhados, descontando pausas reais registradas. */
    private int minutosEfetivos(RealizacaoTurno realizacao) {
        return faixasTrabalhadas(realizacao).stream()
                .mapToInt(faixa -> Math.toIntExact(
                        Duration.between(faixa.inicio(), faixa.fim()).toMinutes()))
                .sum();
    }

    /** Separa o período real em faixas de trabalho, removendo intervalos não trabalhados. */
    private List<FaixaHorario> faixasTrabalhadas(RealizacaoTurno realizacao) {
        if (realizacao.getInicioReal() == null || realizacao.getFimReal() == null) {
            return List.of();
        }
        List<IntervaloRealizacao> pausas = realizacao.getIntervalosNaoTrabalhados().stream()
                .sorted(Comparator.comparing(IntervaloRealizacao::getInicio))
                .toList();
        List<FaixaHorario> faixas = new ArrayList<>();
        LocalDateTime cursor = realizacao.getInicioReal();
        for (IntervaloRealizacao pausa : pausas) {
            if (pausa.getInicio().isAfter(cursor)) {
                faixas.add(new FaixaHorario(cursor, pausa.getInicio()));
            }
            if (pausa.getFim().isAfter(cursor)) {
                cursor = pausa.getFim();
            }
        }
        if (cursor.isBefore(realizacao.getFimReal())) {
            faixas.add(new FaixaHorario(cursor, realizacao.getFimReal()));
        }
        return faixas;
    }

    /** Conta o trecho confirmado antes da pausa quando a jornada ainda está em andamento. */
    private int minutosParciaisConfirmados(
            RealizacaoTurno realizacao, LocalDateTime inicio, LocalDateTime fim) {
        if (realizacao.getInicioReal() == null || realizacao.getInicioIntervaloReal() == null) {
            return 0;
        }
        return recortar(new FaixaHorario(
                        realizacao.getInicioReal(), realizacao.getInicioIntervaloReal()), inicio, fim)
                .map(faixa -> Math.toIntExact(
                        Duration.between(faixa.inicio(), faixa.fim()).toMinutes()))
                .orElse(0);
    }

    /** Intersecta uma faixa de horário com os limites do período, sem incluir trechos vazios. */
    private Optional<FaixaHorario> recortar(
            FaixaHorario faixa, LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime inicioRecortado = faixa.inicio().isAfter(inicio) ? faixa.inicio() : inicio;
        LocalDateTime fimRecortado = faixa.fim().isBefore(fim) ? faixa.fim() : fim;
        return fimRecortado.isAfter(inicioRecortado)
                ? Optional.of(new FaixaHorario(inicioRecortado, fimRecortado))
                : Optional.empty();
    }

    /** Recorta as faixas previstas pela jornada nos limites do mês, descontando pausas previstas. */
    private int minutosPrevistosNoMes(
            Turno turno,
            RegrasJornada.Ocorrencia ocorrencia,
            LocalDateTime inicioMes,
            LocalDateTime fimMes) {
        List<FaixaIntervalo> intervalos = turno.getIntervalos().stream()
                .map(intervalo -> {
                    long inicio = minutosDoDia(intervalo.getInicio())
                            - minutosDoDia(turno.getHorarioInicio());
                    if (inicio < 0) {
                        inicio += 24 * 60L;
                    }
                    long fim = minutosDoDia(intervalo.getFim())
                            - minutosDoDia(turno.getHorarioInicio());
                    if (fim <= inicio) {
                        fim += 24 * 60L;
                    }
                    return new FaixaIntervalo(inicio, fim);
                })
                .sorted(Comparator.comparingLong(FaixaIntervalo::inicio))
                .toList();
        List<FaixaHorario> faixas = new ArrayList<>();
        LocalDateTime cursor = ocorrencia.inicio();
        for (FaixaIntervalo intervalo : intervalos) {
            LocalDateTime inicioIntervalo = ocorrencia.inicio().plusMinutes(intervalo.inicio());
            LocalDateTime fimIntervalo = ocorrencia.inicio().plusMinutes(intervalo.fim());
            if (inicioIntervalo.isAfter(cursor)) {
                faixas.add(new FaixaHorario(cursor, inicioIntervalo));
            }
            if (fimIntervalo.isAfter(cursor)) {
                cursor = fimIntervalo;
            }
        }
        if (cursor.isBefore(ocorrencia.fim())) {
            faixas.add(new FaixaHorario(cursor, ocorrencia.fim()));
        }
        return faixas.stream()
                .map(faixa -> recortar(faixa, inicioMes, fimMes))
                .flatMap(Optional::stream)
                .mapToInt(faixa -> Math.toIntExact(
                        Duration.between(faixa.inicio(), faixa.fim()).toMinutes()))
                .sum();
    }

    /** Calcula quantos minutos de pausa registrada excedem a duração prevista. */
    /** Calcula a diferença positiva entre pausa efetiva e pausa prevista. */
    private int minutosIntervaloExcedente(Turno turno, RealizacaoTurno realizacao) {
        if (!possuiIntervaloRegistrado(realizacao)) {
            return 0;
        }
        long previsto = turno.getIntervalos().stream()
                .mapToLong(intervalo -> {
                    long inicio = minutosDoDia(intervalo.getInicio());
                    long fim = minutosDoDia(intervalo.getFim());
                    if (fim <= inicio) {
                        fim += 24 * 60L;
                    }
                    return fim - inicio;
                })
                .sum();
        long real = minutosIntervaloReal(realizacao);
        return Math.toIntExact(Math.max(0, real - previsto));
    }

    /** Calcula quantos minutos faltam para completar a pausa prevista. */
    private int minutosIntervaloInsuficiente(Turno turno, RealizacaoTurno realizacao) {
        if (!possuiIntervaloRegistrado(realizacao)) {
            return 0;
        }
        long previsto = turno.getIntervalos().stream()
                .mapToLong(intervalo -> {
                    long inicio = minutosDoDia(intervalo.getInicio());
                    long fim = minutosDoDia(intervalo.getFim());
                    if (fim <= inicio) {
                        fim += 24 * 60L;
                    }
                    return fim - inicio;
                })
                .sum();
        long real = minutosIntervaloReal(realizacao);
        return Math.toIntExact(Math.max(0, previsto - real));
    }

    private long minutosIntervaloReal(RealizacaoTurno realizacao) {
        if (realizacao.getInicioIntervaloReal() != null
                && realizacao.getFimIntervaloReal() != null) {
            return Duration.between(
                    realizacao.getInicioIntervaloReal(),
                    realizacao.getFimIntervaloReal()).toMinutes();
        }
        return realizacao.getIntervalosNaoTrabalhados().stream()
                .mapToLong(intervalo -> Duration.between(
                        intervalo.getInicio(), intervalo.getFim()).toMinutes())
                .sum();
    }

    private boolean possuiIntervaloRegistrado(RealizacaoTurno realizacao) {
        return realizacao.getInicioIntervaloReal() != null
                && realizacao.getFimIntervaloReal() != null
                || !realizacao.getIntervalosNaoTrabalhados().isEmpty();
    }

    private long minutosDoDia(LocalTime horario) {
        return horario.getHour() * 60L + horario.getMinute();
    }

    /**
     * Retorna a carga mensal da única atribuição vigente no primeiro dia do mês.
     * Sem atribuição única ou sem carga configurada, não inventa um valor padrão.
     */
    private Integer cargaMensalVigente(Long funcionarioId, LocalDate primeiroDiaMes) {
        LocalDateTime inicioDia = primeiroDiaMes.atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);
        List<AtribuicaoTurno> atribuicoes = atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(funcionarioId).stream()
                .filter(atribuicao -> atribuicao.getVigenciaInicio().isBefore(fimDia))
                .filter(atribuicao -> atribuicao.getVigenciaFim() == null
                        || atribuicao.getVigenciaFim().isAfter(inicioDia))
                .toList();
        if (atribuicoes.size() != 1) {
            return null;
        }
        return atribuicoes.get(0).getTurno().getCargaMensalPadraoMinutos();
    }

    private boolean estaNoMes(LocalDateTime data, LocalDateTime inicio, LocalDateTime fim) {
        return !data.isBefore(inicio) && data.isBefore(fim);
    }

    private ChaveOcorrencia chave(RealizacaoTurno realizacao) {
        return new ChaveOcorrencia(
                realizacao.getAtribuicao().getId(), realizacao.getInicioPlanejado());
    }

    private ResponseStatusException requisicaoInvalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public record FuncionarioIdentificacao(Long id, String nome) {
    }

    private record OcorrenciaFuncionario(
            AtribuicaoTurno atribuicao, RegrasJornada.Ocorrencia ocorrencia) {
    }

    private record ChaveOcorrencia(Long atribuicaoId, LocalDateTime inicioPlanejado) {
    }

    private record FaixaHorario(LocalDateTime inicio, LocalDateTime fim) {
    }

    private record FaixaIntervalo(long inicio, long fim) {
    }
}
