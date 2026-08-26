package com.example.shiftsync.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/turnos")
public class TurnoController {

    @GetMapping
    public  String ConsultTurno(){

        return " Turnos";
    }

}
