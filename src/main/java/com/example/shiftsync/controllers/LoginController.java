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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


/**
 * Autentica o usuário pelo CPF e emite um token JWT assinado.
 *
 * Senhas BCrypt são verificadas pelo PasswordEncoder. Para compatibilidade com
 * registros antigos em texto simples, o código compara em tempo constante e,
 * quando correto, migra a senha para BCrypt no próximo login.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.secret}")
    private String secret;

    @Value("${spring.emissor}")
    private String emissor;

    @Value("${spring.tempo}")
    private Long tempo;

    /**
     * Valida credenciais e status da conta, retornando 401 quando não são aceitos.
     * Em caso de sucesso, o JWT recebe emissor, CPF como assunto e expiração configurada.
     */
    @PostMapping
    public ResponseEntity<LoginResponse>logar(@RequestBody LoginRequest loginRequest){
        if (loginRequest.getLogin() == null || loginRequest.getSenha() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuarioBanco = usuarioRepository.findByCpf(loginRequest.getLogin()).orElse(null);
        if (usuarioBanco != null
                && !"D".equalsIgnoreCase(usuarioBanco.getStatus())
                && senhaCorreta(usuarioBanco, loginRequest.getSenha())) {

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

    /**
     * Compara a senha informada com hash BCrypt ou com o formato legado.
     * Se a senha legada for válida, grava imediatamente sua versão BCrypt.
     */
    private boolean senhaCorreta(Usuario usuario, String senhaInformada) {
        if (senhaInformada == null || usuario.getSenha() == null) {
            return false;
        }

        String senhaArmazenada = usuario.getSenha();
        if (senhaArmazenada.startsWith("$2a$")
                || senhaArmazenada.startsWith("$2b$")
                || senhaArmazenada.startsWith("$2y$")) {
            return passwordEncoder.matches(senhaInformada, senhaArmazenada);
        }

        boolean senhaLegadaValida = MessageDigest.isEqual(
                senhaInformada.getBytes(StandardCharsets.UTF_8),
                senhaArmazenada.getBytes(StandardCharsets.UTF_8));
        if (senhaLegadaValida) {
            usuario.setSenha(passwordEncoder.encode(senhaInformada));
            usuarioRepository.save(usuario);
        }
        return senhaLegadaValida;
    }

    /** Calcula o instante de expiração do token usando o prazo configurado em minutos. */
    private Instant gerarDataExpiracao() {

        LocalDateTime dataAtual = LocalDateTime.now();

        LocalDateTime dataFuturoSoma = dataAtual.plusMinutes(tempo);

        return dataFuturoSoma.toInstant(ZoneOffset.of("-03:00"));

    }

}
