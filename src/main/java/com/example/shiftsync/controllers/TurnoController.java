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
    public Long consultTurnoId(@PathVariable Long id){
        Turno turnoid = new Turno();
        return "id";
    }

    @GetMapping("/horario")
    public Long consultTurnoHorario(@PathVariable Long horario){
        Turno turnoHorario = new Turno();
        return "turnoHorario";
    }

    @GetMapping("/periodo")
    public Long consultTurnoPeriodo(@PathVariable Long periodo){
        Turno turnoPeriodo = new Turno();
        return "turnoPeriodo";
    }

    @GetMapping("/horaExtra")
    public Turno consultTurnoHoraExtra(@PathVariable Long horaExtra){
        Turno turnoHoraExtra = new Turno();
        return  "turnoHoraExtra";
    }

    @PostMapping
    public ResponseEntity<Turno> cadastrarTurno(@RequestBody Turno turnoRequest){
        return ResponseEntity.ok(turnoRequest);}

}
