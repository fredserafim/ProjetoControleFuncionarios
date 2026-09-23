package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/funcionario")

public class FuncionarioController {


    @Autowired
    private FuncionarioRepository funcionarioRepository;


    @GetMapping
    public List<Funcionario> consultFuncionario(){
        return funcionarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public String consultFuncionarioId(@PathVariable Long id) {
        Funcionario funcionarioId = new Funcionario();
        return "funcionarioId";
    }

    @GetMapping("/nome")
    public String consultFuncionarioNome(@PathVariable Long nome) {
        Funcionario funcionarioNome = new Funcionario();
        return "funcionarioNome";
    }

    /*@GetMapping("/turno")
    public String consultFuncionarioTurno(@PathVariable Long turno) {
        Funcionario funcionarioTurno = new Funcionario();
        return "funcionarioTurno";
    }*/

    @GetMapping("/funcao")
    public String consultFuncionarioFuncao(@PathVariable Long funcao) {
        Funcionario funcionarioFuncao = new Funcionario();
        return "funcionarioFuncao";
    }

    @GetMapping("/{id}/departamento")
    public List<Funcionario> consultaFuncionarioPorDepartamento(@PathVariable Long id){
        List<Funcionario> departamentoFuncionario = List.of();
        return departamentoFuncionario;
    }

    /*@GetMapping("/Departamento")
    public String consultFuncionarioDepartamento(@PathVariable Long departamento) {
        Departamento funcionarioDepartamento = new Departamento();
        return "funcionarioDepartamento";
    }*/

    @PostMapping
    public ResponseEntity<FuncionarioResponse> cadastrarFuncionario(@RequestBody Funcionario funcionarioRequest) {
        Funcionario funcionarioBanco = new Funcionario();

        funcionarioBanco.setNome(funcionarioRequest.getNome());
        funcionarioBanco.setFuncao(funcionarioRequest.getFuncao());
        funcionarioBanco.setDataCadastro(LocalDateTime.now());
        funcionarioBanco.setStatus("A");

        //salvando no banco
        funcionarioRepository.save(funcionarioBanco);

        return ResponseEntity.ok(new FuncionarioResponse(funcionarioBanco.getId(),
                "Cadastro com sucesso!"));
    }

    @PutMapping("/{id}")// atualiza tudo
    public ResponseEntity <FuncionarioResponse>
    atualizarFuncionarioId(@PathVariable Long id, @RequestBody FuncionarioRequest funcionarioRequest){
        // consulta no banco
        Funcionario funcionarioBanco = funcionarioRepository.findById(id).orElse(null);

        if(funcionarioBanco != null){
            funcionarioBanco.setNome(funcionarioRequest.getNome());
            funcionarioBanco.setFuncao(funcionarioRequest.getFuncao());
            funcionarioBanco.setDataAtualizacao(LocalDateTime.now());
            funcionarioBanco.setStatus(funcionarioRequest.getStatus());
            funcionarioRepository.save(funcionarioBanco);

            return ResponseEntity.ok(new FuncionarioResponse(funcionarioBanco.getId(),
                    "usuarioAtualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
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
