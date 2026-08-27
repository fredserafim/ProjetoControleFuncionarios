package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/funcionario")

public class FuncionarioController {

    @GetMapping("/{id}")
    public String consultFuncionarioId(@PathVariable long id ){
        Funcionario funcionarioId = new Funcionario();
        return funcionarioId;}

    @GetMapping("/nome")
    public String consultFuncionarioNome(@PathVariable long nome ){
        Funcionario funcionarioNome = new Funcionario();
        return funcionarioNome;}

    @GetMapping("/turno")
    public String consultFuncionarioTurno(@PathVariable long turno ){
        Funcionario funcionarioTurno = new Funcionario();
        return funcionarioTurno;}

    @GetMapping("/funcao")
    public String consultFuncionarioFuncao(@PathVariable long funcao ){
        Funcionario funcionarioFuncao = new Funcionario();
        return funcionarioFuncao;}

    @GetMapping("/Departamento")
    public String consultFuncionarioFuncao(@PathVariable long departamento ){
        Departamento funcionarioDepartamento = new Departamento();
        return funcionarioDepartamento;}

    @PostMapping
    public ResponseEntity<Funcionario> cadastrarFuncionario(@RequestBody Funcionario funcionarioRequest){
        return ResponseEntity.ok(funcionarioRequest);}
}
