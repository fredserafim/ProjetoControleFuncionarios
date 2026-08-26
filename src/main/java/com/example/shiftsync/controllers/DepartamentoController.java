package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Departamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    @GetMapping
    public String consultDepartamento() {
        return " departamentos";}

    @PostMapping
    public ResponseEntity<Departamento> cadastrarDepartamento(@RequestBody Departamento departamentoRequest){
        return ResponseEntity.ok(departamentoRequest);}
}