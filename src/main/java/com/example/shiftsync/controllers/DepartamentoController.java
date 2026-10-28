package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.repository.DepartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** Expõe operações de consulta, cadastro, edição e desativação lógica de departamentos. */
@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    /** Lista departamentos como DTOs para não serializar relações JPA diretamente. */
    @GetMapping
    public List<DepartamentoResponse> consultDepartamento(){
        return departamentoRepository.findAll().stream()
                .map(DepartamentoResponse::new)
                .toList();
    }

    /** Busca um departamento pelo id e devolve 404 quando não existe. */
    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoResponse> consultDepartamentoId(@PathVariable Long id) {
        return departamentoRepository.findById(id)
                .map(departamento -> ResponseEntity.ok(new DepartamentoResponse(departamento)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Cria um departamento; quando o status não vem no pedido, inicia como ativo. */
    @PostMapping
    public ResponseEntity<DepartamentoResponse> cadastrarDepartamento(
            @RequestBody DepartamentoRequest departamentoRequest) {
        Departamento departamentoBanco = new Departamento();
        departamentoBanco.setDepartamento(departamentoRequest.getDepartamento());
        departamentoBanco.setStatus(departamentoRequest.getStatus() == null
                ? "A" : departamentoRequest.getStatus());
        departamentoBanco.setDataCadastro(LocalDateTime.now());
        departamentoRepository.save(departamentoBanco);

        DepartamentoResponse resposta = new DepartamentoResponse(departamentoBanco);
        resposta.setMensagem("Cadastro com sucesso!");
        return ResponseEntity.ok(resposta);
    }

    /** Atualiza os dados do departamento e a data de alteração, se ele existir. */
    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoResponse> atualizarDepartamento(
            @PathVariable Long id, @RequestBody DepartamentoRequest departamentoRequest) {
        return departamentoRepository.findById(id).map(departamentoBanco -> {
            departamentoBanco.setDepartamento(departamentoRequest.getDepartamento());
            departamentoBanco.setDataAtualizacao(LocalDateTime.now());
            if (departamentoRequest.getStatus() != null) {
                departamentoBanco.setStatus(departamentoRequest.getStatus());
            }
            departamentoRepository.save(departamentoBanco);
            DepartamentoResponse resposta = new DepartamentoResponse(departamentoBanco);
            resposta.setMensagem("Departamento atualizado com sucesso!");
            return ResponseEntity.ok(resposta);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Altera somente o status solicitado, sem substituir os demais campos. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DepartamentoResponse> atualizarStatusDepartamento(
            @PathVariable Long id, @RequestBody AtualizaStatusDepartamentoRequest departamentoRequest) {
        Departamento departamentoBanco = departamentoRepository.findById(id).orElse(null);
        if (departamentoBanco != null) {
            departamentoBanco.setStatus(departamentoRequest.getStatus());
            departamentoRepository.save(departamentoBanco);
            return ResponseEntity.ok(new DepartamentoResponse(
                    departamentoBanco.getId(), "Status atualizado com sucesso!"));
        }
        return ResponseEntity.notFound().build();
    }

    /** Realiza exclusão lógica, salvando o status inativo em vez de apagar a linha. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> atualizarStatusDepartamento(@PathVariable Long id) {
        Departamento departamentoBanco = departamentoRepository.findById(id).orElse(null);
        if (departamentoBanco != null) {
            departamentoBanco.setStatus("D");
            departamentoRepository.save(departamentoBanco);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
