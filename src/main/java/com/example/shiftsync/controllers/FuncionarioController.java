package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.services.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private FuncionarioService funcionarioService;

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

        FuncionarioResponse resposta = new FuncionarioResponse(funcionarioBanco);
        resposta.setMensagem("Cadastro com sucesso!");
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}")// atualiza tudo
    public ResponseEntity <FuncionarioResponse>
    atualizarFuncionarioId(@PathVariable Long id, @RequestBody FuncionarioRequest funcionarioRequest){
        return funcionarioService.atualizarFuncionario(id, funcionarioRequest)
                .map(funcionarioBanco -> ResponseEntity.ok(new FuncionarioResponse(funcionarioBanco)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
                    HttpStatus.BAD_REQUEST, "Selecione ou cadastre um departamento válido.");
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
