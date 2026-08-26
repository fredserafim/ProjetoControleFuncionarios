package com.example.shiftsync.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gerentes")
public class GerenteController {

    @GetMapping
    public String ConsultGerente(){
        return " gerentess";

    }
    @GetMapping("/{gerentesId}")
    public String ConsultIdGerente(@PathVariable Long gerentesId) {return " usuario id" + gerentesId;}
}
