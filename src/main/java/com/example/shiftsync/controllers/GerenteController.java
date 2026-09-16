package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Gerente;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.GerenteRepository;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gerentes")
public class GerenteController {


    @Autowired
    private GerenteRepository gerenteRepository;


    @GetMapping
    public List<Gerente> consultGerentes(){
        return gerenteRepository.findAll();
    }


    @GetMapping("/{gerentesId}")
    public String consultIdGerentes(@PathVariable Long gerentesId) {return " usuario id" + gerentesId;}
}
