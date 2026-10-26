package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.TurnoRopository;
import com.example.shiftsync.utils.RegrasJornada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/funcionario")

public class FuncionarioController {


    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private TurnoRopository turnoRepository;

    @Autowired
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @GetMapping
    public List<FuncionarioResponse> consultFuncionario(){
        return funcionarioRepository.findAll().stream()
                .map(FuncionarioResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponse> consultFuncionarioId(@PathVariable Long id) {
        return funcionarioRepository.findById(id)
                .map(funcionario -> ResponseEntity.ok(new FuncionarioResponse(funcionario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/departamento")
    public List<FuncionarioResponse> consultaFuncionarioPorDepartamento(@PathVariable Long id){
        return funcionarioRepository.findByDepartamento_Id(id).stream()
                .map(FuncionarioResponse::new)
                .toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<FuncionarioResponse> cadastrarFuncionario(@RequestBody FuncionarioRequest funcionarioRequest) {
        Funcionario funcionarioBanco = new Funcionario();

        funcionarioBanco.setNome(funcionarioRequest.getNome());
        funcionarioBanco.setCpf(funcionarioRequest.getCpf());
        funcionarioBanco.setTelefone(funcionarioRequest.getTelefone());
        funcionarioBanco.setEndereco(funcionarioRequest.getEndereco());
        funcionarioBanco.setTurno(funcionarioRequest.getTurno());
        funcionarioBanco.setDepartamento(resolverDepartamento(funcionarioRequest));
        funcionarioBanco.setFuncao(funcionarioRequest.getFuncao());
        funcionarioBanco.setDataCadastro(LocalDateTime.now());
        funcionarioBanco.setStatus("A");

        //salvando no banco
        funcionarioRepository.save(funcionarioBanco);
        if (funcionarioRequest.getTurnoId() != null) {
            Turno jornada = buscarJornadaAtiva(funcionarioRequest.getTurnoId());
            atribuirJornada(funcionarioBanco, jornada, funcionarioRequest.getTurnoVigenciaInicio());
        }

        FuncionarioResponse resposta = new FuncionarioResponse(funcionarioBanco);
        resposta.setMensagem("Cadastro com sucesso!");
        return ResponseEntity.ok(resposta);
    }

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
            funcionarioBanco.setTelefone(funcionarioRequest.getTelefone());
            funcionarioBanco.setEndereco(funcionarioRequest.getEndereco());
            funcionarioBanco.setFuncao(funcionarioRequest.getFuncao());
            funcionarioBanco.setTurno(funcionarioRequest.getTurno());
            if (funcionarioRequest.getTurnoId() != null
                    && (funcionarioBanco.getJornada() == null
                    || !funcionarioBanco.getJornada().getId().equals(funcionarioRequest.getTurnoId()))) {
                Turno jornada = buscarJornadaAtiva(funcionarioRequest.getTurnoId());
                atribuirJornada(funcionarioBanco, jornada, funcionarioRequest.getTurnoVigenciaInicio());
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

    private void atribuirJornada(
            Funcionario funcionario, Turno novaJornada, LocalDateTime vigenciaInicio) {
        LocalDateTime vigencia = vigenciaInicio == null ? LocalDateTime.now() : vigenciaInicio;
        var atribuicaoAtual = atribuicaoTurnoRepository
                .findFirstByFuncionario_IdAndVigenciaFimIsNullOrderByVigenciaInicioDesc(
                        funcionario.getId());

        if (atribuicaoAtual.isPresent()) {
            AtribuicaoTurno anterior = atribuicaoAtual.get();
            if (!vigencia.isAfter(anterior.getVigenciaInicio())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova vigência deve ser posterior ao início da jornada atual.");
            }
            RegrasJornada.validarTransicao(anterior.getTurno(), novaJornada, vigencia);
            anterior.setVigenciaFim(vigencia);
            atribuicaoTurnoRepository.save(anterior);
        }

        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(novaJornada);
        atribuicao.setVigenciaInicio(vigencia);
        atribuicaoTurnoRepository.save(atribuicao);

        funcionario.setJornada(novaJornada);
        funcionario.setJornadaVigenciaInicio(vigencia);
        funcionario.setTurno(novaJornada.getNome() == null
                ? novaJornada.getPeriodo() : novaJornada.getNome());
    }

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

    @PatchMapping("/{id}/status")// atualiza pontualmente
    public ResponseEntity<FuncionarioResponse> AtualizarStatusFuncionario(@PathVariable Long id, @RequestBody AtualizaStatusFuncionarioRequest funcionarioRequest){

        Funcionario funcionarioBanco = funcionarioRepository.findById(id).orElse(null);

        if(funcionarioBanco != null){
            funcionarioBanco.setStatus(funcionarioRequest.getStatus());
            funcionarioRepository.save(funcionarioBanco);

            return ResponseEntity.ok(new FuncionarioResponse(funcionarioBanco.getId(), "Status atualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
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
