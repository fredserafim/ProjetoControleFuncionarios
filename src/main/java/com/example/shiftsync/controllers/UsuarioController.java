package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.AtualizaStatusUsuarioRequest;
import com.example.shiftsync.DTO.UsuarioConsultaResponse;
import com.example.shiftsync.DTO.UsuarioRequest;
import com.example.shiftsync.DTO.UsuarioResponse;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.EmpresaRepository;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/usuarios")


public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping
     public List<UsuarioConsultaResponse> consultusUario(){
             return usuarioRepository.findAll()
                     .stream()
                     .map(UsuarioConsultaResponse::new).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioConsultaResponse> consultUsuarioPorId(@PathVariable Long id) {
        var usuario = usuarioRepository.findById(id).orElse(null);

        if(usuario == null){
            return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new UsuarioConsultaResponse(usuario));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrarUsuario(@RequestBody UsuarioRequest usuarioRequest){

        if (usuarioRequest.getEmpresa_id() == null
                || usuarioRequest.getSenha() == null
                || usuarioRequest.getSenha().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var empresaBanco = empresaRepository.findById(usuarioRequest.getEmpresa_id()).orElse(null);

        if(empresaBanco ==  null){
            return ResponseEntity.notFound().build();
        }
        Usuario usuarioBanco = new Usuario();
        usuarioBanco.setNome(usuarioRequest.getNome());
        usuarioBanco.setCpf(usuarioRequest.getCpf());
        usuarioBanco.setDataNascimento(usuarioRequest.getDataNascimento());
        usuarioBanco.setSenha(passwordEncoder.encode(usuarioRequest.getSenha()));
        usuarioBanco.setDataCadastro(LocalDateTime.now());
        usuarioBanco.setEmpresa(empresaBanco);
        usuarioBanco.setStatus("A");

        //salvando no banco
        usuarioRepository.save(usuarioBanco);

        return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(),
                "Cadastro com sucesso!"));
    }

    @PutMapping("/{id}")// atualiza tudo
    public ResponseEntity <UsuarioResponse>
    atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequest usuarioRequest){
        if (usuarioRequest.getEmpresa_id() == null) {
            return ResponseEntity.badRequest().build();
        }
        // consulta no banco
        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);

        if(usuarioBanco != null){


            var empresaBanco = empresaRepository.findById(usuarioRequest.getEmpresa_id()).orElse(null);
            if(empresaBanco == null){
                return ResponseEntity.notFound().build();
            }

            usuarioBanco.setEmpresa(empresaBanco);
            usuarioBanco.setNome(usuarioRequest.getNome());
            usuarioBanco.setCpf(usuarioRequest.getCpf());
            usuarioBanco.setDataNascimento(usuarioRequest.getDataNascimento());
            usuarioBanco.setDataAtualizacao(LocalDateTime.now());
            if (usuarioRequest.getSenha() != null && !usuarioRequest.getSenha().isBlank()) {
                usuarioBanco.setSenha(passwordEncoder.encode(usuarioRequest.getSenha()));
            }
            usuarioRepository.save(usuarioBanco);

            return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(),
                    "usuarioAtualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")// atualiza pontualmente
    public ResponseEntity<UsuarioResponse> AtualizarStatus(@PathVariable Long id, @RequestBody AtualizaStatusUsuarioRequest usuarioRequest){

        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);

        if(usuarioBanco != null){
            usuarioBanco.setStatus(usuarioRequest.getStatus());
            usuarioRepository.save(usuarioBanco);

            return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(), "Status atualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")// deletar
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
