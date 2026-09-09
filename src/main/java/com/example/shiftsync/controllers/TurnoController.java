package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Turno;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/turnos")
public class TurnoController {

    @GetMapping
    public  String consultTurnoUsuario() {
        return " Turnos";
    }

    @GetMapping("/{id}")
    public String consultTurnoId(@PathVariable Long id){
        Turno turnoid = new Turno();
        return "id";
    }

    @GetMapping("/horario")
    public String consultTurnoHorario(@PathVariable Long horario){
        Turno turnoHorario = new Turno();
        return "turnoHorario";
    }

    @GetMapping("/periodo")
    public String consultTurnoPeriodo(@PathVariable Long periodo){
        Turno turnoPeriodo = new Turno();
        return "turnoPeriodo";
    }

    @GetMapping("/horaExtra")
    public String consultTurnoHoraExtra(@PathVariable Long horaExtra){
        Turno turnoHoraExtra = new Turno();
        return  "turnoHoraExtra";
    }

    @PostMapping
    public ResponseEntity<Turno> cadastrarTurno(@RequestBody Turno turnoRequest){
        return ResponseEntity.ok(turnoRequest);}

}
