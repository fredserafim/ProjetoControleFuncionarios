package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.IntervaloJornada;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.TurnoRopository;
import com.example.shiftsync.utils.RegrasJornada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/turnos")
public class TurnoController {

    @Autowired
    private TurnoRopository turnoRepository;

    @Autowired
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @GetMapping
    public List<TurnoConsultaResponse> consultTurno() {
        return turnoRepository.findAll().stream()
                .map(TurnoConsultaResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoConsultaResponse> consultTurnoId(@PathVariable Long id) {
        return turnoRepository.findById(id)
                .map(turno -> ResponseEntity.ok(new TurnoConsultaResponse(turno)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TurnoResponse> cadastrarTurno(@RequestBody TurnoRequest request) {
        Turno turno = new Turno();
        aplicarDados(turno, request);
        if (RegrasJornada.possuiConfiguracaoJornada(turno)) {
            RegrasJornada.validarECalcular(turno);
        }
        turno.setDataCadastro(LocalDateTime.now());
        turno.setStatus("A");
        turnoRepository.save(turno);
        return ResponseEntity.ok(new TurnoResponse(turno.getId(), "Cadastro com sucesso!"));
    }

    @GetMapping("/listarTudo")
    public ResponseEntity<List<TurnoConsultaResponse>> listarTodos() {
        return ResponseEntity.ok(turnoRepository.findAll().stream()
                .map(TurnoConsultaResponse::new)
                .toList());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TurnoResponse> atualizarTurno(
            @PathVariable Long id, @RequestBody TurnoRequest request) {
        List<AtribuicaoTurno> atribuicoesPreliminares =
                atribuicaoTurnoRepository.findByTurno_Id(id);
        List<Long> funcionariosAfetados = atribuicoesPreliminares.stream()
                .map(atribuicao -> atribuicao.getFuncionario().getId())
                .distinct()
                .sorted()
                .toList();
        Set<Long> funcionariosBloqueados = new HashSet<>(funcionariosAfetados);
        if (!funcionariosAfetados.isEmpty()) {
            funcionarioRepository.findAllByIdForUpdate(funcionariosAfetados);
        }

        Turno turno = turnoRepository.findByIdForUpdate(id).orElse(null);
        if (turno == null) {
            return ResponseEntity.notFound().build();
        }
        List<AtribuicaoTurno> atribuicoes = atribuicaoTurnoRepository.findByTurno_Id(id);
        boolean surgiuAtribuicaoConcorrente = atribuicoes.stream()
                .map(atribuicao -> atribuicao.getFuncionario().getId())
                .anyMatch(funcionarioId -> !funcionariosBloqueados.contains(funcionarioId));
        if (surgiuAtribuicaoConcorrente) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O turno recebeu novas atribuições durante a edição. Tente novamente.");
        }

        aplicarDados(turno, request);
        if (RegrasJornada.possuiConfiguracaoJornada(turno)) {
            RegrasJornada.validarECalcular(turno);
            validarAtribuicoes(turno, atribuicoes);
        }
        if (request.getStatus() != null) {
            turno.setStatus(request.getStatus());
        }
        turno.setDataAtualizacao(LocalDateTime.now());
        turnoRepository.save(turno);
        return ResponseEntity.ok(new TurnoResponse(turno.getId(), "Turno atualizado com sucesso!"));
    }

    @GetMapping("/{id}/trabalha")
    public ResponseEntity<EstadoTurnoResponse> consultTrabalhoNaData(
            @PathVariable Long id, @RequestParam LocalDate data) {
        Turno turno = buscarJornadaConfigurada(id);
        return ResponseEntity.ok(new EstadoTurnoResponse(
                data, RegrasJornada.trabalhaNaData(turno, data)));
    }

    @GetMapping("/{id}/ocorrencias")
    public ResponseEntity<List<OcorrenciaTurnoResponse>> consultarOcorrencias(
            @PathVariable Long id,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        Turno turno = buscarJornadaConfigurada(id);
        return ResponseEntity.ok(RegrasJornada.ocorrencias(turno, inicio, fim).stream()
                .map(OcorrenciaTurnoResponse::new)
                .toList());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TurnoResponse> atualizarStatusTurno(
            @PathVariable Long id, @RequestBody AtualizaStatusTurnoRequest request) {
        Turno turno = turnoRepository.findById(id).orElse(null);
        if (turno == null) {
            return ResponseEntity.notFound().build();
        }
        turno.setStatus(request.getStatus());
        turnoRepository.save(turno);
        return ResponseEntity.ok(new TurnoResponse(turno.getId(), "Status atualizado com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TurnoResponse> desativarTurno(@PathVariable Long id) {
        Turno turno = turnoRepository.findById(id).orElse(null);
        if (turno == null) {
            return ResponseEntity.notFound().build();
        }
        turno.setStatus("D");
        turnoRepository.save(turno);
        return ResponseEntity.ok().build();
    }

    private void aplicarDados(Turno turno, TurnoRequest request) {
        if (request.getNome() != null || request.getPeriodo() != null) {
            String nome = request.getNome() == null
                    ? request.getPeriodo().trim() : request.getNome().trim();
            turno.setNome(nome);
            if (request.getPeriodo() != null || turno.getPeriodo() == null) {
                turno.setPeriodo(request.getPeriodo() == null ? nome : request.getPeriodo().trim());
            }
        }
        if (request.getHorario() != null) {
            turno.setHorario(request.getHorario());
        }
        if (request.getHoraExtra() != null) {
            turno.setHoraExtra(request.getHoraExtra());
        }
        boolean requestJornada = request.getTipoJornada() != null
                || request.getTipoEscala() != null
                || request.getHorarioInicio() != null
                || request.getHorarioFim() != null
                || request.getDataReferencia() != null
                || !request.getDiasSemana().isEmpty()
                || !request.getIntervalos().isEmpty();
        if (!requestJornada) {
            return;
        }

        turno.setTipoJornada(normalizar(request.getTipoJornada()));
        turno.setTipoEscala(normalizar(request.getTipoEscala()));
        turno.setDiasSemana(Set.copyOf(request.getDiasSemana()));
        turno.setHorarioInicio(request.getHorarioInicio());
        turno.setHorarioFim(request.getHorarioFim());
        turno.setDataReferencia(request.getDataReferencia());
        turno.setIntervalos(request.getIntervalos().stream()
                .map(intervalo -> new IntervaloJornada(intervalo.getInicio(), intervalo.getFim()))
                .toList());
        turno.setCargaDiariaContratualMinutos(request.getCargaDiariaContratualMinutos());
        turno.setCargaSemanalContratualMinutos(request.getCargaSemanalContratualMinutos());
        turno.setCargaMensalPadraoMinutos(request.getCargaMensalPadraoMinutos());
    }

    private void validarAtribuicoes(
            Turno turnoAtualizado, List<AtribuicaoTurno> atribuicoes) {
        for (AtribuicaoTurno atribuicao : atribuicoes) {
            List<AtribuicaoTurno> historico = atribuicaoTurnoRepository
                    .findByFuncionario_IdOrderByVigenciaInicioAsc(
                            atribuicao.getFuncionario().getId());
            int indice = -1;
            for (int i = 0; i < historico.size(); i++) {
                if (Objects.equals(historico.get(i).getId(), atribuicao.getId())) {
                    indice = i;
                    break;
                }
            }
            if (indice > 0) {
                RegrasJornada.validarTransicao(
                        historico.get(indice - 1).getTurno(),
                        turnoAtualizado,
                        atribuicao.getVigenciaInicio());
            }

            for (AtribuicaoTurno outraAtribuicao : historico) {
                if (Objects.equals(outraAtribuicao.getId(), atribuicao.getId())) {
                    continue;
                }
                RegrasJornada.encontrarSobreposicao(
                                turnoAtualizado,
                                atribuicao.getVigenciaInicio(),
                                atribuicao.getVigenciaFim(),
                                outraAtribuicao.getTurno(),
                                outraAtribuicao.getVigenciaInicio(),
                                outraAtribuicao.getVigenciaFim())
                        .ifPresent(conflito -> {
                            String nomeConflitante = outraAtribuicao.getTurno().getNome() == null
                                    ? outraAtribuicao.getTurno().getPeriodo()
                                    : outraAtribuicao.getTurno().getNome();
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    mensagemConflito(nomeConflitante, conflito));
                        });
            }
        }
    }

    private String mensagemConflito(
            String nomeTurno, RegrasJornada.ConflitoSobreposicao conflito) {
        return "Não foi possível atualizar o turno porque existe sobreposição de horários "
                + "com a jornada \"" + (nomeTurno == null ? "sem nome" : nomeTurno)
                + "\" entre " + conflito.inicio() + " e " + conflito.fim() + ".";
    }

    private Turno buscarJornadaConfigurada(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Jornada não encontrada."));
        if (!RegrasJornada.possuiConfiguracaoJornada(turno)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O turno legado ainda não possui uma jornada configurada.");
        }
        return turno;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim().toUpperCase(Locale.ROOT);
    }
}
