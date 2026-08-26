package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Turno;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/turnos")
public class TurnoController {

    @GetMapping
    public  String consultTurno(){
        return " Turnos";}

    @PostMapping
    public ResponseEntity<Turno> cadastrarTurno(@RequestBody Turno turnoRequest){
        return ResponseEntity.ok(turnoRequest);}

}
