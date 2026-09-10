package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.AtualizaStatusUsuarioRequest;
import com.example.shiftsync.DTO.UsuarioRequest;
import com.example.shiftsync.DTO.UsuarioResponse;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/usuarios")


public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;


    @GetMapping
     public List<Usuario> consultusUario(){
             return usuarioRepository.findAll();
    }

    @GetMapping("/Id2/{id}")
    public String consultUsuarioPorId2 (@PathVariable Long id){
        return"usuario por id"+id;
    }

    @GetMapping("/empresa/{empresaid}")
    public String consultaEmpresa(@PathVariable Long empresaid){
        return"empresa " + empresaid;

    }

    @GetMapping("/{id}")
    public  ResponseEntity<Usuario> consultUsuarioPorId(@PathVariable Long id) {
        var usuario = usuarioRepository.findById(id).orElse(null);

        if(usuario == null){
            return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/empresa/{empresaId}")
    public Usuario consultEmpresaId (@PathVariable Long empresaId){
        Usuario usuarioConstrutorCompleto = new Usuario("Jhan","06487941999","05/01/1190");

        return usuarioConstrutorCompleto;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrarUsuario(@RequestBody UsuarioRequest usuarioRequest){
        Usuario usuarioBanco = new Usuario();

        usuarioBanco.setNome(usuarioRequest.getNome());
        usuarioBanco.setCpf(usuarioRequest.getCpf());
        usuarioBanco.setDataNascimento(usuarioRequest.getDataNascimento());
        usuarioBanco.setSenha(usuarioRequest.getSenha());
        usuarioBanco.setDataCadastro(LocalDateTime.now());
        usuarioBanco.setStatus("A");

        //salvando no banco
        usuarioRepository.save(usuarioBanco);

        return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(),
                "Cadastro com sucesso!"));
    }

    @PutMapping("/{id}")
    public ResponseEntity <UsuarioResponse>
    atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequest usuarioRequest){
        // consulta no banco
        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);

        if(usuarioBanco != null){
            usuarioBanco.setNome(usuarioRequest.getNome());
            usuarioBanco.setCpf(usuarioRequest.getCpf());
            usuarioBanco.setDataNascimento(usuarioRequest.getDataNascimento());
            usuarioBanco.setDataAtualizacao(LocalDateTime.now());
            usuarioBanco.setSenha(usuarioRequest.getSenha());
            usuarioRepository.save(usuarioBanco);

            return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(),
                    "usuarioAtualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioResponse> AtualizarStatus(@PathVariable Long id, @RequestBody AtualizaStatusUsuarioRequest usuarioRequest){

        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);

        if(usuarioBanco != null){
            usuarioBanco.setStatus(usuarioRequest.getStatus());
            usuarioRepository.save(usuarioBanco);

            return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(), "Status atualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity <UsuarioResponse>
    atualizarStatusUsuario(@PathVariable Long id){
        // consulta no banco
        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);

        if(usuarioBanco != null){
            usuarioBanco.setStatus("D");
            usuarioRepository.save(usuarioBanco);
            //usuarioRepository.delete(usuarioBanco);

            return ResponseEntity.ok().build();

        }
        return ResponseEntity.notFound().build();
    }







}
