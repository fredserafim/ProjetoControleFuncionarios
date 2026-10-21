package com.example.shiftsync.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.shiftsync.DTO.LoginRequest;
import com.example.shiftsync.DTO.LoginResponse;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${spring.secret}")
    private String secret;

    @Value("${spring.emissor}")
    private String emissor;

    @Value("${spring.tempo}")
    private Long tempo;

    @PostMapping
    public ResponseEntity<LoginResponse>logar(@RequestBody LoginRequest loginRequest){

        //Usuario usuarioBanco = usuarioRepository.findAll().stream().filter()


        if(usuarioRepository.existsUsuarioByCpfAndSenha(loginRequest.getLogin(), loginRequest.getSenha())){

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setMensagem("Bem vindo");
            // JWT metodo

            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token = JWT.create()
                            .withIssuer(emissor)
                                    . withSubject(loginRequest.getLogin())
                                            .withExpiresAt(gerarDataExpiracao())
                                                    .sign((algorithm));

            loginResponse.setToken(token);

            return ResponseEntity.ok(loginResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    private Instant gerarDataExpiracao() {

        LocalDateTime dataAtual = LocalDateTime.now();

        LocalDateTime dataFuturoSoma = dataAtual.plusMinutes(tempo);

        return dataFuturoSoma.toInstant(ZoneOffset.of("-03:00"));

    }

}
