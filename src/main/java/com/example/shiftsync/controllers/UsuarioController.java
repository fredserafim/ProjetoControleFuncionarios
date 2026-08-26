package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")


public class UsuarioController {

    private final PathMatcher pathMatcher;

    public UsuarioController(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @GetMapping

     public String ConsultusUario(){
             return " hello word ";
    }
    @GetMapping("/Id2/{id}")
    public String ConsultUsuarioPorId2 (@PathVariable Long id){
        return"usuario por id"+id;
    }
    @GetMapping("/empresa/{empresaid}")
    public String ConsultaEmpresa(@PathVariable long empresaid){
        return"empresa " + empresaid;

    }
    @GetMapping("/{id}")
    public  Usuario ConsultUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = new Usuario();

        usuario.setNome("Jhan");
        usuario.setCpf("06487941999");
        usuario.setDataNascimento("05 / 01 / 1990");


        return usuario;
    }
    @GetMapping("/empresa/{empresaId}")
    public Usuario ConsultEmpresaId (@PathVariable Long empresaId){
        Usuario usuarioConstrutorCompleto = new Usuario("Jhan","06487941999","05/01/1190");

        return usuarioConstrutorCompleto;
    }



    @PostMapping
    public ResponseEntity<Usuario> CadastrarUsuario(@RequestBody Usuario usuarioRequest){

        return ResponseEntity.ok(usuarioRequest);
    }


}
