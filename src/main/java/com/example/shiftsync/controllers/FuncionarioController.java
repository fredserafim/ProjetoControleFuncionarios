package com.example.shiftsync.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/funcionario")

public class FuncionarioController {

    @GetMapping
    public String ConsultFuncionario(){
        return "Estudantes";
    }
}
