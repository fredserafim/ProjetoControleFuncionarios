package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.TurnoRopository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/turnos")
public class TurnoController {


    @Autowired // injeção de dependencia, alocação de outros arquivos que preciso de forma automtica com consulmo de memoria somente quando necessario.
    private TurnoRopository turnoRepository;


    @GetMapping
    public List<TurnoConsultaResponse> consultTurno(){
        return turnoRepository.findAll().stream()
                .map(TurnoConsultaResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoConsultaResponse> consultTurnoId(@PathVariable Long id){
        return turnoRepository.findById(id)
                .map(turno -> ResponseEntity.ok(new TurnoConsultaResponse(turno)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> cadastrarTurno(@RequestBody Turno turnoRequest){
        Turno turnoBanco = new Turno();

        turnoBanco.setHorario(turnoRequest.getHorario());
        turnoBanco.setPeriodo(turnoRequest.getPeriodo());
        turnoBanco.setHoraExtra(turnoRequest.getHoraExtra());
        turnoBanco.setDataCadastro(LocalDateTime.now());
        turnoBanco.setStatus("A");

        //salvando no banco
        turnoRepository.save(turnoBanco);

        return ResponseEntity.ok(new TurnoResponse(turnoBanco.getId(),
                "Cadastro com sucesso!"));
    }

    @GetMapping("/listarTudo")
    public ResponseEntity<List<TurnoConsultaResponse>> listarTodos(){


        var listaCurso = turnoRepository.findAll()
                .stream()
                .map(TurnoConsultaResponse::new).toList();

            return ResponseEntity.ok(listaCurso);


    }

    @PutMapping("/{id}")// atualiza tudo
    public ResponseEntity <TurnoResponse>
    atualizarUsuario(@PathVariable Long id, @RequestBody TurnoRequest turnoRequest){
        // consulta no banco
        Turno turnoBanco = turnoRepository.findById(id).orElse(null);

        if(turnoBanco != null){
            turnoBanco.setHoraExtra(turnoRequest.getHoraExtra());
            turnoBanco.setPeriodo(turnoRequest.getPeriodo());
            turnoBanco.setHorario(turnoRequest.getHorario());
            turnoBanco.setDataAtualizacao(LocalDateTime.now());
            turnoBanco.setStatus(turnoRequest.getStatus());
            turnoRepository.save(turnoBanco);

            return ResponseEntity.ok(new TurnoResponse(turnoBanco.getId(),
                    "usuarioAtualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")// atualiza pontualmente
    public ResponseEntity<TurnoResponse> AtualizarStatusTurno(@PathVariable Long id, @RequestBody AtualizaStatusTurnoRequest turnoRequest){

        Turno turnoBanco = turnoRepository.findById(id).orElse(null);

        if(turnoBanco != null){
            turnoBanco.setStatus(turnoRequest.getStatus());
            turnoRepository.save(turnoBanco);

            return ResponseEntity.ok(new TurnoResponse(turnoBanco.getId(), "Status atualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")// deletar
    public ResponseEntity <TurnoResponse>
    atualizarStatusTurno(@PathVariable Long id){
        // consulta no banco
        Turno turnoBanco = turnoRepository.findById(id).orElse(null);

        if(turnoBanco != null){
            turnoBanco.setStatus("D");
            turnoRepository.save(turnoBanco);
            //turnoRepository.delete(turnoBanco);

            return ResponseEntity.ok().build();

        }
        return ResponseEntity.notFound().build();
    }


}
