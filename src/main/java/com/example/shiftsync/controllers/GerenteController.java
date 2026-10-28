package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Gerente;
import com.example.shiftsync.repository.GerenteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Disponibiliza consultas de leitura para os cadastros de gerente existentes. */
@RestController
@RequestMapping("/gerentes")
public class GerenteController {


    @Autowired
    private GerenteRepository gerenteRepository;


    /** Retorna todos os gerentes usando o modelo da entidade como resposta. */
    @GetMapping
    public List<Gerente> consultGerentes(){
        return gerenteRepository.findAll();
    }


    /** Busca um gerente por id e usa 404 quando o cadastro não existe. */
    @GetMapping("/{gerentesId}")
    public ResponseEntity<Gerente> consultIdGerentes(@PathVariable Long gerentesId) {
        return gerenteRepository.findById(gerentesId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
