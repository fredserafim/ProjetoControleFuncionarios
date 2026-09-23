package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.*;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoRepository departamentoRepository;


    @GetMapping
    public List<Departamento> consultDepartamento(){
        return departamentoRepository.findAll();
    }

    //@GetMapping
    //public String consultDepartamentoUsuario(){return "tudo";}

    @GetMapping("/{id}")
    public String consultDepartamentoId(@PathVariable Long id) {
        Departamento departamentoId = new Departamento();
        return "departamentoId";
    }

    @GetMapping("/{id}/setor")
    public String consultDepartamentoSetor(@PathVariable Long id){
        Departamento departamentoSetor = new Departamento();
        return "departamentoSetor";
    }

    @GetMapping("/turno")
    public String consultaDepartametoPor(@PathVariable Long turno){
        Departamento departamentoTurno = new Departamento();
        return "departamentoTurno" ;
    }




    @PostMapping
    public ResponseEntity<DepartamentoResponse> cadastrarDepartamento(@RequestBody Departamento departamentoRequest){
        Departamento departamentoBanco = new Departamento();

        departamentoBanco.setSetor(departamentoRequest.getSetor());
        departamentoBanco.setStatus(departamentoRequest.getStatus());
        departamentoBanco.setDataCadastro(departamentoBanco.getDataCadastro());

        departamentoRepository.save(departamentoBanco);

        return ResponseEntity.ok(new DepartamentoResponse(departamentoBanco.getId(),
                "Cadastro com sucesso!"));
    }



    @PutMapping("/{id}")// atualiza tudo
    public ResponseEntity <DepartamentoResponse>
    atualizarDepartamento(@PathVariable Long id, @RequestBody DepartamentoRequest departamentoRequest){
        // consulta no banco
        Departamento departamentoBanco = departamentoRepository.findById(id).orElse(null);

        if(departamentoBanco != null){
            departamentoBanco.setSetor(departamentoRequest.getSetor());
            departamentoBanco.setDataAtualizacao(LocalDateTime.now());
            departamentoBanco.setStatus(departamentoRequest.getStatus());
            departamentoRepository.save(departamentoBanco);

            return ResponseEntity.ok(new DepartamentoResponse(departamentoBanco.getId(),
                    "usuarioAtualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")// atualiza pontualmente
    public ResponseEntity<DepartamentoResponse> atualizarStatusDepartamento(@PathVariable Long id, @RequestBody AtualizaStatusDepartamentoRequest departamentoRequest){

        Departamento departamentoBanco = departamentoRepository.findById(id).orElse(null);

        if(departamentoBanco != null){
            departamentoBanco.setStatus(departamentoRequest.getStatus());
            departamentoRepository.save(departamentoBanco);

            return ResponseEntity.ok(new DepartamentoResponse(departamentoBanco.getId(), "Status atualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")// deletar
    public ResponseEntity <DepartamentoResponse>
    atualizarStatusDepartamento(@PathVariable Long id){
        // consulta no banco
        Departamento departamentoBanco = departamentoRepository.findById(id).orElse(null);

        if(departamentoBanco != null){
            departamentoBanco.setStatus("D");
            departamentoRepository.save(departamentoBanco);
            //usuarioRepository.delete(usuarioBanco);

            return ResponseEntity.ok().build();

        }
        return ResponseEntity.notFound().build();
    }





}


