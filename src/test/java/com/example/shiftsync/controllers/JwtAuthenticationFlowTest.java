package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.UsuarioRepository;
import com.example.shiftsync.config.JwtFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirma que um segredo curto continua permitindo autenticar e proteger rotas.
 *
 * O teste combina o controller real de login com o filtro JWT e um endpoint
 * temporário de prova, sem depender de banco de dados ou de segredo de produção.
 */
class JwtAuthenticationFlowTest {

    private static final String CPF_VALIDO = "52998224725";
    private static final String CPF_OUTRO_USUARIO = "11144477735";
    private static final String SEGREDO_CURTO_DE_TESTE = "curto";
    private static final String EMISSOR = "teste";
    private static final String SENHA_USUARIO_A = "senha-a";
    private static final String SENHA_USUARIO_B = "senha-b";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UsuarioRepository usuarioRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        Usuario usuarioA = usuario(1L, CPF_VALIDO, "Ana", SENHA_USUARIO_A);
        Usuario usuarioB = usuario(2L, CPF_OUTRO_USUARIO, "Bruno", SENHA_USUARIO_B);
        when(usuarioRepository.findByCpf(CPF_VALIDO)).thenReturn(Optional.of(usuarioA));
        when(usuarioRepository.findByCpf(CPF_OUTRO_USUARIO)).thenReturn(Optional.of(usuarioB));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioA));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioB));

        LoginController loginController = new LoginController();
        ReflectionTestUtils.setField(loginController, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(loginController, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(loginController, "secret", SEGREDO_CURTO_DE_TESTE);
        ReflectionTestUtils.setField(loginController, "emissor", EMISSOR);
        ReflectionTestUtils.setField(loginController, "tempo", 30L);

        UsuarioController usuarioController = new UsuarioController();
        ReflectionTestUtils.setField(usuarioController, "usuarioRepository", usuarioRepository);

        JwtFilter jwtFilter = new JwtFilter();
        ReflectionTestUtils.setField(jwtFilter, "secret", SEGREDO_CURTO_DE_TESTE);
        ReflectionTestUtils.setField(jwtFilter, "emissor", EMISSOR);

        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        loginController, usuarioController, new RotaProtegidaDeTeste())
                .addFilters(jwtFilter)
                .build();
    }

    @Test
    void loginComSegredoCurtoEmiteTokenAceitoPeloFiltro() throws Exception {
        String respostaLogin = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"52998224725","senha":"senha-a"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode token = objectMapper.readTree(respostaLogin).get("token");

        mockMvc.perform(get("/protegido").header(
                        "Authorization", "Bearer " + token.asText()))
                .andExpect(status().isOk());
    }

    @Test
    void rotaProtegidaContinuaRejeitandoAusenciaDeToken() throws Exception {
        mockMvc.perform(get("/protegido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosAcessamSomenteOsPropriosDadosMesmoComParametrosAdulterados() throws Exception {
        String tokenA = token(CPF_VALIDO, SENHA_USUARIO_A);
        String tokenB = token(CPF_OUTRO_USUARIO, SENHA_USUARIO_B);

        mockMvc.perform(get("/usuarios/me")
                        .param("cpf", CPF_OUTRO_USUARIO)
                        .param("id", "2")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.cpf").value(CPF_VALIDO));

        mockMvc.perform(get("/usuarios/me")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bruno"))
                .andExpect(jsonPath("$.cpf").value(CPF_OUTRO_USUARIO));

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Bruno"));

        mockMvc.perform(get("/usuarios")
                        .param("cpf", CPF_OUTRO_USUARIO)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana"));

        mockMvc.perform(get("/usuarios/2")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfilPessoalNaoPodeSerConsultadoSemToken() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    private String token(String cpf, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"%s","senha":"%s"}
                                """.formatted(cpf, senha)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private Usuario usuario(Long id, String cpf, String nome, String senha) {
        Usuario usuario = new Usuario(nome, cpf, "1990-01-01");
        usuario.setId(id);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setStatus("A");
        return usuario;
    }

    @RestController
    static class RotaProtegidaDeTeste {

        @GetMapping("/protegido")
        String protegido() {
            return "acesso autorizado";
        }
    }
}
