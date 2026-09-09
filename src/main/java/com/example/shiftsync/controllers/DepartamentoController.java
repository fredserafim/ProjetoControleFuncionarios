package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Departamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    @GetMapping
    public String consultDepartamentoUsuario(){return "tudo";}

    @GetMapping("/{id}")
    public String consultDepartamentoId(@PathVariable Long id) {
        Departamento departamentoId = new Departamento();
        return "departamentoId";
    }

    @GetMapping("/setor")
    public String consultDepartamentoSetor(@PathVariable Long setor){
        Departamento departamentoSetor = new Departamento();
        return "departamentoSetor";
    }

    @GetMapping("/turno")
    public String consultDepartamentoTurno(@PathVariable Long turno){
        Departamento departamentoTurno = new Departamento();
        return "departamentoTurno";
    }

    @GetMapping("/funcionario")
    public String consultDepartamentoFuncionario(@PathVariable Long funcionario){
        Departamento departamentoFuncionario = new Departamento();
        return "departamentoFuncionario";
    }


    @PostMapping
    public ResponseEntity<Departamento> cadastrarDepartamento(@RequestBody Departamento departamentoRequest){
        return ResponseEntity.ok(departamentoRequest);}
}