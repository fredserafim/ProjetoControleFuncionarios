package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Funcionario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/funcionario")

public class FuncionarioController {

    @GetMapping
    public String consultFuncionario(){
        return "Estudantes";}

    @PostMapping
    public ResponseEntity<Funcionario> cadastrarFuncionario(@RequestBody Funcionario funcionarioRequest){
        return ResponseEntity.ok(funcionarioRequest);}
}
