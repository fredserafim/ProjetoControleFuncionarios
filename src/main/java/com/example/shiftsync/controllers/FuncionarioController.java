package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.IntervaloRealizacao;
import com.example.shiftsync.entities.RealizacaoTurno;
import com.example.shiftsync.entities.StatusRealizacaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.RealizacaoTurnoRepository;
import com.example.shiftsync.repository.TurnoRopository;
import com.example.shiftsync.utils.CepUtils;
import com.example.shiftsync.utils.OnlyNumbers;
import com.example.shiftsync.utils.RegrasJornada;
import com.example.shiftsync.utils.CpfUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoints de cadastro de funcionários, atribuição histórica de turnos,
 * confirmação de jornadas e consulta de horas extras.
 *
 * A camada converte entidades em DTOs para as respostas e delega o cálculo de
 * ocorrências e transições à classe {@link RegrasJornada}.
 */
@RestController
@RequestMapping("/funcionario")
@Tag(name =" Funcionario Controller", description =  "Classe responsavel por fazer o controle do CRUD dos funcionarios")
public class FuncionarioController {


    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private TurnoRopository turnoRepository;

    @Autowired
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Autowired
    private RealizacaoTurnoRepository realizacaoTurnoRepository;

    /** Lista funcionários e converte cada entidade para o formato público da API. */
    @GetMapping
    public List<FuncionarioResponse> consultFuncionario(){
        return funcionarioRepository.findAll().stream()
                .map(FuncionarioResponse::new)
                .toList();
    }

    /** Consulta o ViaCEP pela utilitária existente sem expor falhas internas do serviço. */
    @GetMapping("/consultar-cep")
    public ResponseEntity<Map<String, String>> consultarCep(@RequestParam String cep) {
        String endereco;
        try {
            endereco = CepUtils.buscarEndereco(cep);
        } catch (IllegalArgumentException excecao) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "CEP inválido."));
        } catch (RuntimeException excecao) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "mensagem",
                    "Serviço de consulta de CEP indisponível. Tente novamente mais tarde."));
        }
        if (endereco == null || endereco.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensagem", "CEP não encontrado."));
        }
        return ResponseEntity.ok(Map.of("endereco", endereco));
    }

    /** Busca um funcionário pelo id; retorna 404 quando não existe. */
    @GetMapping("/{id}")
    @Operation(summary = "Busca Funcionario por ID", description = "Responsavel por buscar funcionario por id" +
            "cado inexistente retorna 404, recebendo Pathvariable id como parametro ")
    public ResponseEntity<FuncionarioResponse> consultFuncionarioId(@PathVariable Long id) {
        return funcionarioRepository.findById(id)
                .map(funcionario -> ResponseEntity.ok(new FuncionarioResponse(funcionario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Lista funcionários vinculados ao departamento informado. */
    @GetMapping("/{id}/departamento")
    @Operation(summary = "Lista funcionarios vinculados ao departamento informado", description = "Responsavel por buscar funcionario por id vinculado a um departamento " +
            "recebendo como parametro de entrada pathvariable id")
    public List<FuncionarioResponse> consultaFuncionarioPorDepartamento(@PathVariable Long id){
        return funcionarioRepository.findByDepartamento_Id(id).stream()
                .map(FuncionarioResponse::new)
                .toList();
    }

    /**
     * Expande as atribuições históricas em ocorrências previstas dentro do período.
     * Para cada ocorrência associa a realização já registrada, se existir.
     */
    @GetMapping("/{id}/turnos-planejados")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TurnoPlanejadoResponse>> consultarTurnosPlanejados(
            @PathVariable Long id,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim,
            HttpServletRequest request) {
        validarPeriodoConsulta(inicio, fim);
        Funcionario funcionario = funcionarioRepository.findById(id).orElse(null);
        if (funcionario == null) {
            return ResponseEntity.notFound().build();
        }
        validarAcessoAoFuncionario(funcionario, request);

        Map<ChaveOcorrencia, RealizacaoTurno> realizacoes = new HashMap<>();
        realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(id).forEach(realizacao ->
                realizacoes.put(chave(realizacao), realizacao));

        List<TurnoPlanejadoResponse> resposta = new ArrayList<>();
        for (AtribuicaoTurno atribuicao :
                atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(id)) {
            Turno turno = atribuicao.getTurno();
            if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
                continue;
            }
            RegrasJornada.ocorrencias(turno, inicio.minusDays(1), fim).stream()
                    .filter(ocorrencia -> !ocorrencia.inicio().isBefore(inicio)
                            && ocorrencia.inicio().isBefore(fim))
                    .filter(ocorrencia -> dentroDaVigencia(atribuicao, ocorrencia))
                    .forEach(ocorrencia -> {
                        ChaveOcorrencia chave = new ChaveOcorrencia(
                                atribuicao.getId(), ocorrencia.inicio());
                        resposta.add(new TurnoPlanejadoResponse(
                                atribuicao.getId(), turno, ocorrencia.inicio(), ocorrencia.fim(),
                                realizacoes.get(chave)));
                    });
        }
        resposta.sort(Comparator.comparing(TurnoPlanejadoResponse::getInicioPlanejado));
        return ResponseEntity.ok(resposta);
    }

    /**
     * Registra a confirmação administrativa de uma ocorrência planejada.
     *
     * Verifica o acesso ao funcionário, a propriedade da atribuição, a unicidade
     * da ocorrência e a coerência dos horários efetivos antes de persistir.
     */
    @PostMapping("/{id}/realizacoes-turno")
    @Transactional
    public ResponseEntity<RealizacaoTurnoResponse> registrarRealizacaoTurno(
            @PathVariable Long id,
            @RequestBody RealizacaoTurnoRequest request,
            HttpServletRequest servletRequest) {
        Funcionario funcionario = funcionarioRepository.findByIdForUpdate(id).orElse(null);
        if (funcionario == null) {
            return ResponseEntity.notFound().build();
        }
        validarAcessoAoFuncionario(funcionario, servletRequest);
        validarRequisicaoRealizacao(request);

        AtribuicaoTurno atribuicao = atribuicaoTurnoRepository
                .findById(request.getAtribuicaoTurnoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Atribuição de turno não encontrada."));
        if (!atribuicao.getFuncionario().getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A atribuição não pertence a este funcionário.");
        }
        if (realizacaoTurnoRepository.findByAtribuicao_IdAndInicioPlanejado(
                atribuicao.getId(), request.getInicioPlanejado()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Esta ocorrência já possui uma confirmação registrada.");
        }

        RegrasJornada.Ocorrencia ocorrencia =
                buscarOcorrenciaPlanejada(atribuicao, request.getInicioPlanejado());
        validarDadosReais(request);

        RealizacaoTurno realizacao = new RealizacaoTurno();
        realizacao.setAtribuicao(atribuicao);
        realizacao.setInicioPlanejado(ocorrencia.inicio());
        realizacao.setFimPlanejado(ocorrencia.fim());
        realizacao.setStatus(request.getStatus());
        realizacao.setConfirmadoEm(LocalDateTime.now());
        if (request.getStatus() == StatusRealizacaoTurno.REALIZADO) {
            realizacao.setInicioReal(request.getInicioReal());
            realizacao.setFimReal(request.getFimReal());
            realizacao.setIntervalosNaoTrabalhados(request.getIntervalosNaoTrabalhados().stream()
                    .map(intervalo -> new IntervaloRealizacao(
                            intervalo.getInicio(), intervalo.getFim()))
                    .toList());
        }
        RealizacaoTurno salva = realizacaoTurnoRepository.save(realizacao);
        return ResponseEntity.ok(new RealizacaoTurnoResponse(
                salva, request.getStatus() == StatusRealizacaoTurno.REALIZADO
                        ? minutosEfetivos(salva) : null));
    }

    /**
     * Apura a diferença entre trabalho efetivo e carga mensal contratual.
     * Também contabiliza pendências e ocorrências não realizadas conforme a regra atual.
     */
    @GetMapping("/{id}/horas-extras")
    @Transactional(readOnly = true)
    @Operation(summary = "Consulta a Hora Extra",description = " Contabiliza pendencias de horas com comparativos de horas" +
            "atribuidas, utilizando com parametro entradas PathVariable ID , RequestParam String e HTTPServeletRequest")
    public ResponseEntity<ApuracaoHorasExtrasResponse> consultarHorasExtras(
            @PathVariable Long id,
            @RequestParam String mes,
            HttpServletRequest request) {
        YearMonth mesReferencia;
        try {
            mesReferencia = YearMonth.parse(mes);
        } catch (DateTimeParseException excecao) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O mês deve estar no formato yyyy-MM.");
        }

        Funcionario funcionario = funcionarioRepository.findById(id).orElse(null);
        if (funcionario == null) {
            return ResponseEntity.notFound().build();
        }
        validarAcessoAoFuncionario(funcionario, request);

        LocalDateTime inicioMes = mesReferencia.atDay(1).atStartOfDay();
        LocalDateTime fimMes = mesReferencia.plusMonths(1).atDay(1).atStartOfDay();
        List<RealizacaoTurno> realizacoes =
                realizacaoTurnoRepository.findByAtribuicao_Funcionario_IdAndStatus(
                        id, StatusRealizacaoTurno.REALIZADO);
        List<FaixaHorario> faixas = realizacoes.stream()
                .flatMap(realizacao -> faixasTrabalhadas(realizacao).stream())
                .map(faixa -> recortar(faixa, inicioMes, fimMes))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(FaixaHorario::inicio))
                .toList();
        List<FaixaHorario> faixasUnicas = unirFaixas(faixas);
        int minutosTrabalhados = somarMinutos(faixasUnicas);

        long turnosRealizados = realizacoes.stream()
                .filter(realizacao -> !faixasTrabalhadas(realizacao).stream()
                        .map(faixa -> recortar(faixa, inicioMes, fimMes))
                        .flatMap(Optional::stream)
                        .toList().isEmpty())
                .count();

        ContagemTurnos contagem = contarTurnosPlanejados(id, mesReferencia);
        Integer cargaPadrao = cargaMensalVigente(id, mesReferencia.atDay(1));
        boolean cargaDisponivel = cargaPadrao != null;
        String pendencia = cargaDisponivel ? null
                : "Não há uma carga horária mensal padrão configurada em uma jornada "
                + "vigente no primeiro dia do mês.";
        int minutosExtras = cargaDisponivel
                ? Math.max(0, minutosTrabalhados - cargaPadrao) : 0;
        ApuracaoHorasExtrasResponse resposta = new ApuracaoHorasExtrasResponse(
                funcionario.getId(),
                funcionario.getNome(),
                mesReferencia.toString(),
                minutosTrabalhados,
                cargaPadrao,
                cargaDisponivel ? minutosExtras : null,
                Math.toIntExact(turnosRealizados),
                contagem.pendentes(),
                contagem.naoRealizados(),
                cargaDisponivel ? "REALIZADA" : "PADRAO_PENDENTE",
                pendencia);
        return ResponseEntity.ok(resposta);
    }

    /**
     * Cadastra o funcionário e, se informado, cria sua atribuição de turno com vigência.
     * A primeira gravação permite usar o funcionário como bloqueio de concorrência.
     */
    @PostMapping
    @Transactional
    @Operation(summary = "Cadastra Funcionario", description = "Responsavel por cadastrar um funcionario usando como parametros de entrada" +
            "RequestBody funcionario Request e salvando no banco informações pessoais")
    public ResponseEntity<FuncionarioResponse> cadastrarFuncionario(@RequestBody FuncionarioRequest funcionarioRequest) {
        Funcionario funcionarioBanco = new Funcionario();

        funcionarioBanco.setNome(funcionarioRequest.getNome());
        funcionarioBanco.setCpf(funcionarioRequest.getCpf());
        funcionarioBanco.setTelefone(normalizarTelefone(funcionarioRequest.getTelefone()));
        funcionarioBanco.setCep(funcionarioRequest.getCep());
        funcionarioBanco.setEndereco(funcionarioRequest.getEndereco());
        funcionarioBanco.setTurno(funcionarioRequest.getTurno());
        funcionarioBanco.setDepartamento(resolverDepartamento(funcionarioRequest));
        funcionarioBanco.setFuncao(funcionarioRequest.getFuncao());
        funcionarioBanco.setDataCadastro(LocalDateTime.now());
        funcionarioBanco.setStatus("A");

        Turno jornada = funcionarioRequest.getTurnoId() == null
                ? null : buscarJornadaAtiva(funcionarioRequest.getTurnoId());
        // Salva primeiro para que exista uma linha de funcionário a bloquear ao criar a atribuição.
        funcionarioRepository.save(funcionarioBanco);
        if (funcionarioRequest.getTurnoId() != null) {
            atribuirJornada(funcionarioBanco, jornada, funcionarioRequest.getTurnoVigenciaInicio());
        }

        FuncionarioResponse resposta = new FuncionarioResponse(funcionarioBanco);
        resposta.setMensagem("Cadastro com sucesso!");
        return ResponseEntity.ok(resposta);
    }

    /** Atualiza dados cadastrais e a vigência selecionada sem apagar o histórico. */
    @PutMapping("/{id}")// atualiza tudo
    @Transactional
    public ResponseEntity <FuncionarioResponse>
    atualizarFuncionarioId(@PathVariable Long id, @RequestBody FuncionarioRequest funcionarioRequest){
        return funcionarioRepository.findByIdForUpdate(id).map(funcionarioBanco -> {
            if (funcionarioRequest.getNome() != null) {
                funcionarioBanco.setNome(funcionarioRequest.getNome());
            }
            if (funcionarioRequest.getCpf() != null) {
                funcionarioBanco.setCpf(funcionarioRequest.getCpf());
            }
            if (funcionarioRequest.getTelefone() != null) {
                funcionarioBanco.setTelefone(normalizarTelefone(funcionarioRequest.getTelefone()));
            }
            if (funcionarioRequest.getCep() != null) {
                funcionarioBanco.setCep(funcionarioRequest.getCep());
            }
            funcionarioBanco.setEndereco(funcionarioRequest.getEndereco());
            funcionarioBanco.setFuncao(funcionarioRequest.getFuncao());
            funcionarioBanco.setTurno(funcionarioRequest.getTurno());
            if (funcionarioRequest.getTurnoId() != null) {
                atualizarAtribuicaoJornada(funcionarioBanco, funcionarioRequest);
            }
            if (funcionarioRequest.getDepartamentoId() != null
                    || funcionarioRequest.getDepartamento() != null) {
                funcionarioBanco.setDepartamento(resolverDepartamento(funcionarioRequest));
            }
            funcionarioBanco.setDataAtualizacao(LocalDateTime.now());
            Funcionario funcionarioAtualizado = funcionarioRepository.save(funcionarioBanco);
            return ResponseEntity.ok(new FuncionarioResponse(funcionarioAtualizado));
        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Normaliza o telefone com a utilitária compartilhada; valor ausente fica nulo. */
    private String normalizarTelefone(String telefone) {
        String numeros = OnlyNumbers.onlyNumbers(telefone);
        return numeros.isEmpty() ? null : numeros;
    }

    private Turno buscarJornadaAtiva(Long turnoId) {
        Turno turno = turnoRepository.findByIdForUpdate(turnoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Jornada não encontrada."));
        if ("D".equals(turno.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Não é possível atribuir uma jornada inativa.");
        }
        if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O turno legado ainda não possui uma jornada configurada.");
        }
        return turno;
    }

    /** Valida o turno selecionado e delega a atualização com proteção do histórico. */
    private void atualizarAtribuicaoJornada(
            Funcionario funcionario, FuncionarioRequest request) {
        Turno jornada = buscarJornadaAtiva(request.getTurnoId());
        atribuirJornada(funcionario, jornada, request.getTurnoVigenciaInicio());
    }

    /**
     * Atualiza diretamente a atribuição se nunca foi usada; quando já existe
     * qualquer realização, fecha o período e cria uma nova versão histórica.
     */
    private void atribuirJornada(
            Funcionario funcionario, Turno novaJornada, LocalDateTime vigenciaInicio) {
        LocalDateTime vigencia = vigenciaInicio == null
                ? LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                : vigenciaInicio.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        var atribuicaoAtual = atribuicaoTurnoRepository
                .findFirstByFuncionario_IdAndVigenciaFimIsNullOrderByVigenciaInicioDesc(
                        funcionario.getId());
        List<AtribuicaoTurno> historico = atribuicaoTurnoRepository
                .findByFuncionario_IdOrderByVigenciaInicioAsc(funcionario.getId());

        if (atribuicaoAtual.isPresent()) {
            AtribuicaoTurno atual = atribuicaoAtual.get();
            List<RealizacaoTurno> realizacoesAtuais =
                    realizacaoTurnoRepository.findByAtribuicao_Id(atual.getId());

            if (realizacoesAtuais.isEmpty()) {
                if (atual.getTurno().getId().equals(novaJornada.getId())
                        && atual.getVigenciaInicio().equals(vigencia)) {
                    atualizarJornadaCompatibilidade(funcionario, novaJornada, vigencia);
                    return;
                }
                List<AtribuicaoTurno> outrasAtribuicoes = historico.stream()
                        .filter(item -> !item.getId().equals(atual.getId()))
                        .toList();
                AtribuicaoTurno anterior = historico.stream()
                        .filter(item -> atual.getVigenciaInicio().equals(item.getVigenciaFim()))
                        .max(Comparator.comparing(AtribuicaoTurno::getVigenciaInicio))
                        .orElse(null);
                if (anterior != null && vigencia.isBefore(atual.getVigenciaInicio())) {
                    if (!vigencia.isAfter(anterior.getVigenciaInicio())) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "A vigência não pode avançar sobre o início de uma atribuição histórica.");
                    }
                    boolean registrosAfetados = realizacaoTurnoRepository
                            .findByAtribuicao_Id(anterior.getId()).stream()
                            .anyMatch(realizacao -> realizacao.getFimPlanejado().isAfter(vigencia));
                    if (registrosAfetados) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "A vigência informada afetaria registros de ponto da atribuição anterior.");
                    }
                    RegrasJornada.validarTransicao(anterior.getTurno(), novaJornada, vigencia);
                    anterior.setVigenciaFim(vigencia);
                    atribuicaoTurnoRepository.save(anterior);
                } else {
                    RegrasJornada.validarTransicao(atual.getTurno(), novaJornada, vigencia);
                }
                validarAusenciaDeSobreposicoes(
                        novaJornada, vigencia, null, outrasAtribuicoes, null, null);
                atual.setTurno(novaJornada);
                atual.setVigenciaInicio(vigencia);
                atribuicaoTurnoRepository.save(atual);
                atualizarJornadaCompatibilidade(funcionario, novaJornada, vigencia);
                return;
            }

            if (atual.getTurno().getId().equals(novaJornada.getId())
                    && atual.getVigenciaInicio().equals(vigencia)) {
                atualizarJornadaCompatibilidade(funcionario, novaJornada, vigencia);
                return;
            }
            if (!vigencia.isAfter(atual.getVigenciaInicio())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A nova vigência deve ser posterior ao início da atribuição atual "
                                + "porque já existem realizações de turno vinculadas.");
            }

            validarAusenciaDeSobreposicoes(
                    novaJornada, vigencia, null, historico, atual, vigencia);
            for (RealizacaoTurno realizacao : realizacoesAtuais) {
                if (realizacao.getFimPlanejado().isAfter(vigencia)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A nova vigência afetaria uma realização já vinculada à jornada atual.");
                }
            }
            RegrasJornada.validarTransicao(atual.getTurno(), novaJornada, vigencia);
            atual.setVigenciaFim(vigencia);
            atribuicaoTurnoRepository.save(atual);

            AtribuicaoTurno novaAtribuicao = new AtribuicaoTurno();
            novaAtribuicao.setFuncionario(funcionario);
            novaAtribuicao.setTurno(novaJornada);
            novaAtribuicao.setVigenciaInicio(vigencia);
            atribuicaoTurnoRepository.save(novaAtribuicao);
            atualizarJornadaCompatibilidade(funcionario, novaJornada, vigencia);
            return;
        }

        validarAusenciaDeSobreposicoes(
                novaJornada, vigencia, null, historico, null, null);
        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(novaJornada);
        atribuicao.setVigenciaInicio(vigencia);
        atribuicaoTurnoRepository.save(atribuicao);
        atualizarJornadaCompatibilidade(funcionario, novaJornada, vigencia);
    }

    private void atualizarJornadaCompatibilidade(
            Funcionario funcionario, Turno jornada, LocalDateTime vigencia) {
        funcionario.setJornada(jornada);
        funcionario.setJornadaVigenciaInicio(vigencia);
        funcionario.setTurno(jornada.getNome() == null
                ? jornada.getPeriodo() : jornada.getNome());
    }

    /** Impede atribuições simultâneas incompatíveis para o mesmo funcionário. */
    private void validarAusenciaDeSobreposicoes(
            Turno novaJornada,
            LocalDateTime vigenciaInicio,
            LocalDateTime vigenciaFim,
            List<AtribuicaoTurno> historico,
            AtribuicaoTurno atribuicaoEncerrada,
            LocalDateTime fimAtribuicaoEncerrada) {
        for (AtribuicaoTurno existente : historico) {
            LocalDateTime vigenciaFimExistente = existente.getVigenciaFim();
            if (atribuicaoEncerrada != null
                    && existente.getId().equals(atribuicaoEncerrada.getId())) {
                vigenciaFimExistente = fimAtribuicaoEncerrada;
            }

            RegrasJornada.encontrarSobreposicao(
                            novaJornada, vigenciaInicio, vigenciaFim,
                            existente.getTurno(), existente.getVigenciaInicio(),
                            vigenciaFimExistente)
                    .ifPresent(conflito -> {
                        String nomeConflitante = existente.getTurno().getNome() == null
                                ? existente.getTurno().getPeriodo()
                                : existente.getTurno().getNome();
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                mensagemConflito(nomeConflitante, conflito));
                    });
        }
    }

    private String mensagemConflito(
            String nomeTurno, RegrasJornada.ConflitoSobreposicao conflito) {
        return "Não foi possível atribuir este turno ao funcionário porque existe "
                + "sobreposição de horários com a jornada \""
                + (nomeTurno == null ? "sem nome" : nomeTurno)
                + "\" entre " + conflito.inicio() + " e " + conflito.fim() + ".";
    }

    /** Rejeita períodos ausentes ou invertidos antes de expandir as ocorrências planejadas. */
    private void validarPeriodoConsulta(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe um período de consulta válido.");
        }
    }

    private boolean dentroDaVigencia(
            AtribuicaoTurno atribuicao, RegrasJornada.Ocorrencia ocorrencia) {
        return !ocorrencia.inicio().isBefore(atribuicao.getVigenciaInicio())
                && (atribuicao.getVigenciaFim() == null
                || !ocorrencia.fim().isAfter(atribuicao.getVigenciaFim()));
    }

    private ChaveOcorrencia chave(RealizacaoTurno realizacao) {
        return new ChaveOcorrencia(
                realizacao.getAtribuicao().getId(), realizacao.getInicioPlanejado());
    }

    /** Valida presença e consistência básica dos campos da confirmação recebida. */
    private void validarRequisicaoRealizacao(RealizacaoTurnoRequest request) {
        if (request.getAtribuicaoTurnoId() == null
                || request.getInicioPlanejado() == null
                || request.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe a atribuição, o início planejado e o status da ocorrência.");
        }
        validarPrecisaoMinuto(request.getInicioPlanejado(), "início planejado");
    }

    /** Confirma que o horário recebido corresponde a uma ocorrência da atribuição e da vigência. */
    private RegrasJornada.Ocorrencia buscarOcorrenciaPlanejada(
            AtribuicaoTurno atribuicao, LocalDateTime inicioPlanejado) {
        Turno turno = atribuicao.getTurno();
        if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O turno não possui uma jornada configurada.");
        }
        RegrasJornada.Ocorrencia ocorrencia = RegrasJornada.ocorrencias(
                        turno, inicioPlanejado, inicioPlanejado.plusMinutes(1)).stream()
                .filter(item -> item.inicio().equals(inicioPlanejado))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O horário informado não corresponde a uma ocorrência planejada."));
        if (!dentroDaVigencia(atribuicao, ocorrencia)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A ocorrência está fora do período de vigência da atribuição.");
        }
        return ocorrencia;
    }

    /** Confere ordem, duração e alinhamento por minuto dos horários efetivos. */
    private void validarDadosReais(RealizacaoTurnoRequest request) {
        if (request.getStatus() != StatusRealizacaoTurno.REALIZADO
                && request.getStatus() != StatusRealizacaoTurno.NAO_REALIZADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma ocorrência realizada ou não realizada.");
        }
        if (request.getStatus() == StatusRealizacaoTurno.NAO_REALIZADO) {
            if (request.getInicioReal() != null || request.getFimReal() != null
                    || !request.getIntervalosNaoTrabalhados().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Uma ocorrência não realizada não pode conter horários efetivos.");
            }
            return;
        }
        if (request.getInicioReal() == null || request.getFimReal() == null
                || !request.getFimReal().isAfter(request.getInicioReal())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe horários reais válidos para uma ocorrência realizada.");
        }
        validarPrecisaoMinuto(request.getInicioReal(), "início real");
        validarPrecisaoMinuto(request.getFimReal(), "fim real");

        List<FaixaHorario> intervalos = new ArrayList<>();
        for (IntervaloRealizacaoRequest intervalo : request.getIntervalosNaoTrabalhados()) {
            if (intervalo == null || intervalo.getInicio() == null || intervalo.getFim() == null
                    || !intervalo.getFim().isAfter(intervalo.getInicio())
                    || intervalo.getInicio().isBefore(request.getInicioReal())
                    || intervalo.getFim().isAfter(request.getFimReal())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada intervalo não trabalhado deve estar dentro do período real.");
            }
            validarPrecisaoMinuto(intervalo.getInicio(), "início do intervalo");
            validarPrecisaoMinuto(intervalo.getFim(), "fim do intervalo");
            intervalos.add(new FaixaHorario(intervalo.getInicio(), intervalo.getFim()));
        }
        intervalos.sort(Comparator.comparing(FaixaHorario::inicio));
        for (int indice = 1; indice < intervalos.size(); indice++) {
            if (intervalos.get(indice).inicio()
                    .isBefore(intervalos.get(indice - 1).fim())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Os intervalos não trabalhados não podem se sobrepor.");
            }
        }
    }

    private void validarPrecisaoMinuto(LocalDateTime valor, String campo) {
        if (valor.getSecond() != 0 || valor.getNano() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O campo " + campo + " deve ter precisão de minutos.");
        }
    }

    /** Compara o CPF autenticado com o funcionário quando a chamada é feita por um titular. */
    private void validarAcessoAoFuncionario(
                Funcionario funcionario, HttpServletRequest request) {
        Object cpfAutenticado = request.getAttribute("authenticatedCpf");
        if (!(cpfAutenticado instanceof String)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "A sessão autenticada não identificou o usuário.");
        }
        if (!CpfUtils.normalizar(funcionario.getCpf())
                    .equals(CpfUtils.normalizar((String) cpfAutenticado))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Só é permitido consultar ou alterar os próprios registros de jornada.");
        }
    }

    /** Obtém a carga mensal padrão somente quando há uma atribuição vigente no primeiro dia. */
    private Integer cargaMensalVigente(Long funcionarioId, LocalDate primeiroDiaMes) {
        LocalDateTime inicioPrimeiroDia = primeiroDiaMes.atStartOfDay();
        LocalDateTime fimPrimeiroDia = inicioPrimeiroDia.plusDays(1);
        List<AtribuicaoTurno> vigentes =
                atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(
                                funcionarioId).stream()
                        .filter(atribuicao -> atribuicao.getVigenciaInicio()
                                .isBefore(fimPrimeiroDia))
                        .filter(atribuicao -> atribuicao.getVigenciaFim() == null
                                || atribuicao.getVigenciaFim().isAfter(inicioPrimeiroDia))
                        .toList();
        if (vigentes.size() != 1) {
            return null;
        }
        Integer carga = vigentes.get(0).getTurno().getCargaMensalPadraoMinutos();
        return carga == null || carga < 0 ? null : carga;
    }

    /** Conta ocorrências do mês que não possuem realização ou foram marcadas como não realizadas. */
    private ContagemTurnos contarTurnosPlanejados(Long funcionarioId, YearMonth mes) {
        LocalDateTime inicioMes = mes.atDay(1).atStartOfDay();
        LocalDateTime fimMes = mes.plusMonths(1).atDay(1).atStartOfDay();
        List<RealizacaoTurno> registros =
                realizacaoTurnoRepository.findByAtribuicao_Funcionario_Id(funcionarioId);
        Map<ChaveOcorrencia, RealizacaoTurno> porOcorrencia = new HashMap<>();
        int naoRealizados = 0;
        for (RealizacaoTurno realizacao : registros) {
            porOcorrencia.put(chave(realizacao), realizacao);
            if (realizacao.getStatus() == StatusRealizacaoTurno.NAO_REALIZADO
                    && !realizacao.getInicioPlanejado().isBefore(inicioMes)
                    && realizacao.getInicioPlanejado().isBefore(fimMes)) {
                naoRealizados++;
            }
        }

        int pendentes = 0;
        for (AtribuicaoTurno atribuicao :
                atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(
                        funcionarioId)) {
            Turno turno = atribuicao.getTurno();
            if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
                continue;
            }
            for (RegrasJornada.Ocorrencia ocorrencia :
                    RegrasJornada.ocorrencias(turno, inicioMes.minusDays(1), fimMes)) {
                if (ocorrencia.inicio().isBefore(inicioMes)
                        || !ocorrencia.inicio().isBefore(fimMes)
                        || !dentroDaVigencia(atribuicao, ocorrencia)) {
                    continue;
                }
                RealizacaoTurno confirmacao = porOcorrencia.get(
                        new ChaveOcorrencia(atribuicao.getId(), ocorrencia.inicio()));
                if (confirmacao == null) {
                    pendentes++;
                }
            }
        }
        return new ContagemTurnos(pendentes, naoRealizados);
    }

    /**
     * Separa o tempo realmente trabalhado das pausas e une faixas adjacentes.
     * É usado para evitar somar intervalos sobrepostos na apuração mensal.
     */
    private List<FaixaHorario> faixasTrabalhadas(RealizacaoTurno realizacao) {
        if (realizacao.getInicioReal() == null || realizacao.getFimReal() == null) {
            return List.of();
        }
        List<IntervaloRealizacao> intervalos = realizacao.getIntervalosNaoTrabalhados().stream()
                .sorted(Comparator.comparing(IntervaloRealizacao::getInicio))
                .toList();
        List<FaixaHorario> faixas = new ArrayList<>();
        LocalDateTime cursor = realizacao.getInicioReal();
        for (IntervaloRealizacao intervalo : intervalos) {
            if (intervalo.getInicio().isAfter(cursor)) {
                faixas.add(new FaixaHorario(cursor, intervalo.getInicio()));
            }
            if (intervalo.getFim().isAfter(cursor)) {
                cursor = intervalo.getFim();
            }
        }
        if (cursor.isBefore(realizacao.getFimReal())) {
            faixas.add(new FaixaHorario(cursor, realizacao.getFimReal()));
        }
        return faixas;
    }

    private Optional<FaixaHorario> recortar(
            FaixaHorario faixa, LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime inicioRecortado = faixa.inicio().isAfter(inicio) ? faixa.inicio() : inicio;
        LocalDateTime fimRecortado = faixa.fim().isBefore(fim) ? faixa.fim() : fim;
        return fimRecortado.isAfter(inicioRecortado)
                ? Optional.of(new FaixaHorario(inicioRecortado, fimRecortado))
                : Optional.empty();
    }

    private List<FaixaHorario> unirFaixas(List<FaixaHorario> faixas) {
        if (faixas.isEmpty()) {
            return List.of();
        }
        List<FaixaHorario> unidas = new ArrayList<>();
        FaixaHorario atual = faixas.get(0);
        for (int indice = 1; indice < faixas.size(); indice++) {
            FaixaHorario proxima = faixas.get(indice);
            if (!proxima.inicio().isAfter(atual.fim())) {
                atual = new FaixaHorario(
                        atual.inicio(),
                        proxima.fim().isAfter(atual.fim()) ? proxima.fim() : atual.fim());
            } else {
                unidas.add(atual);
                atual = proxima;
            }
        }
        unidas.add(atual);
        return unidas;
    }

    private int somarMinutos(List<FaixaHorario> faixas) {
        long minutos = faixas.stream()
                .mapToLong(faixa -> Duration.between(faixa.inicio(), faixa.fim()).toMinutes())
                .sum();
        return Math.toIntExact(minutos);
    }

    private int minutosEfetivos(RealizacaoTurno realizacao) {
        List<FaixaHorario> faixas = faixasTrabalhadas(realizacao);
        return somarMinutos(unirFaixas(faixas));
    }

    private record ChaveOcorrencia(Long atribuicaoId, LocalDateTime inicioPlanejado) {
    }

    private record FaixaHorario(LocalDateTime inicio, LocalDateTime fim) {
    }

    private record ContagemTurnos(int pendentes, int naoRealizados) {
    }

    /**
     * Resolve o departamento pelo id preferencial ou pelo nome legado.
     * Ausência ou inexistência do departamento é comunicada como erro HTTP.
     */
    private Departamento resolverDepartamento(FuncionarioRequest funcionarioRequest) {
        if (funcionarioRequest.getDepartamentoId() != null) {
            return departamentoRepository.findById(funcionarioRequest.getDepartamentoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Departamento não encontrado."));
        }

        String nomeDepartamento = funcionarioRequest.getDepartamento();
        if (nomeDepartamento == null || nomeDepartamento.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O departamento informado é inválido.");
        }

        return departamentoRepository.findByDepartamentoIgnoreCase(nomeDepartamento.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Departamento não encontrado: " + nomeDepartamento));
    }

    /** Altera somente entre os estados permitidos, mantendo os demais dados e históricos. */
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<FuncionarioResponse> atualizarStatusFuncionario(
            @PathVariable Long id,
            @RequestBody AtualizaStatusFuncionarioRequest funcionarioRequest) {
        if (funcionarioRequest == null || funcionarioRequest.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe o status A (ativo) ou D (inativo).");
        }

        String status = funcionarioRequest.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!"A".equals(status) && !"D".equals(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O status deve ser A (ativo) ou D (inativo).");
        }

        Funcionario funcionarioBanco = funcionarioRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Funcionário não encontrado."));
        funcionarioBanco.setStatus(status);
        FuncionarioResponse resposta =
                new FuncionarioResponse(funcionarioRepository.save(funcionarioBanco));
        resposta.setMensagem("Status atualizado com sucesso!");
        return ResponseEntity.ok(resposta);
    }


    @DeleteMapping("/{id}")// deletar
    public ResponseEntity <FuncionarioResponse>
    atualizarStatusFuncionarioId(@PathVariable Long id){
        // consulta no banco
        Funcionario funcionarioBanco = funcionarioRepository.findById(id).orElse(null);

        if(funcionarioBanco != null){
            funcionarioBanco.setStatus("D");
            funcionarioRepository.save(funcionarioBanco);
            //funcionarioRepository.delete(funcionarioBanco);

            return ResponseEntity.ok().build();

        }
        return ResponseEntity.notFound().build();
    }






}
