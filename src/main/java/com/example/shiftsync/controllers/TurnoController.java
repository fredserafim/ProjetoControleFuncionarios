package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.TurnoRopository;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/turnos")
public class TurnoController {


    @Autowired
    private TurnoRopository turnoRepository;


    @GetMapping
    public List<Turno> consultTurno(){
        return turnoRepository.findAll();
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
