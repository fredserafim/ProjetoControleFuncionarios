package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.AtualizaStatusUsuarioRequest;
import com.example.shiftsync.DTO.UsuarioRequest;
import com.example.shiftsync.DTO.UsuarioResponse;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.entities.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/usuarios")


public class UsuarioController {

    private final PathMatcher pathMatcher;

    public UsuarioController(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @GetMapping
     public String consultusUario(){
             return " hello word ";
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
    public  Usuario consultUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = new Usuario();

        usuario.setNome("Jhan");
        usuario.setCpf("06487941999");
        usuario.setDataNascimento("05 / 01 / 1990");


        return usuario;
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
        usuarioBanco.setDataCadastro(LocalDateTime.now());
        usuarioBanco.setStatus("a");

        return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(),
                "Cadastro com sucesso!"));
    }

    @PutMapping("/{id}")
    public ResponseEntity <UsuarioResponse>
    atualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioRequest){
        // consulta no banco
        Usuario usuarioBanco = new Usuario();

        if(usuarioBanco != null){
            usuarioBanco.setNome(usuarioRequest.getNome());
            usuarioBanco.setCpf(usuarioRequest.getCpf());
            usuarioBanco.setDataNascimento(usuarioRequest.getDataNascimento());
            usuarioBanco.setDataAtualização(LocalDateTime.now());

            return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(),
                    "usuarioAtualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity <UsuarioResponse>
    atualizarStatusUsuario(@PathVariable Long id){
        // consulta no banco
        Usuario usuarioBanco = new Usuario();

        if(usuarioBanco != null){
            usuarioBanco.setStatus("D");


            return ResponseEntity.ok().build();

        }
        return ResponseEntity.notFound().build();
    }







}
