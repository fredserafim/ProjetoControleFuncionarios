package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.AtualizaStatusUsuarioRequest;
import com.example.shiftsync.DTO.UsuarioConsultaResponse;
import com.example.shiftsync.DTO.UsuarioRequest;
import com.example.shiftsync.DTO.UsuarioResponse;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.EmpresaRepository;
import com.example.shiftsync.repository.UsuarioRepository;
import com.example.shiftsync.utils.CpfUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints de consulta e manutenção de contas de usuário.
 *
 * Respostas de consulta usam DTOs que omitem senha; ao criar ou alterar senha,
 * o valor é codificado com BCrypt antes de ser persistido.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuario Controller",description = " Classe responsavel por fazer o controle do CRUD do usuario")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Mantém o formato de lista da rota existente, mas limita a consulta à conta do token.
     * O CPF enviado pelo cliente não participa da busca.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioConsultaResponse>> consultusUario(
            HttpServletRequest request) {
        Usuario usuario = buscarUsuarioAutenticado(request);
        return ResponseEntity.ok(List.of(new UsuarioConsultaResponse(usuario)));
    }

    /**
     * Retorna os dados da conta identificada pelo CPF presente no JWT validado.
     *
     * Parâmetros de CPF ou id adicionados à URL são ignorados: a identidade vem
     * exclusivamente do atributo que JwtFilter grava após validar o token.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioConsultaResponse> consultarUsuarioAutenticado(
            HttpServletRequest request) {
        return ResponseEntity.ok(new UsuarioConsultaResponse(buscarUsuarioAutenticado(request)));
    }

    /** Consulta por id somente a própria conta; ids de outros usuários recebem 403. */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar Usuario Por ID",description = "Metodo de consulta de usuario por ID que retorna somente um usuario que recebe como parametro o Path variable que recebe o longID")
    public ResponseEntity<UsuarioConsultaResponse> consultUsuarioPorId(
            @PathVariable Long id, HttpServletRequest request) {
        var usuario = usuarioRepository.findById(id).orElse(null);

        if(usuario == null){
            return  ResponseEntity.notFound().build();
        }
        String cpfAutenticado = cpfAutenticado(request);
        if (!CpfUtils.normalizar(usuario.getCpf()).equals(CpfUtils.normalizar(cpfAutenticado))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Não é permitido consultar dados de outro usuário.");
        }
        return ResponseEntity.ok(new UsuarioConsultaResponse(usuario));
    }

    /** Obtém a conta associada à identidade autenticada, sem aceitar identificadores do cliente. */
    private Usuario buscarUsuarioAutenticado(HttpServletRequest request) {
        String cpf = cpfAutenticado(request);
        return usuarioRepository.findByCpf(cpf).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
    }

    /** Lê o CPF que o JwtFilter colocou na requisição após validar a assinatura do token. */
    private String cpfAutenticado(HttpServletRequest request) {
        Object identidade = request.getAttribute("authenticatedCpf");
        if (!(identidade instanceof String cpf) || cpf.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "A sessão autenticada não identificou o usuário.");
        }
        return cpf;
    }

    /** Cria uma conta vinculada a uma empresa existente e salva a senha codificada. */
    @PostMapping
    @Operation(summary = "Posta os dados",description = "Metodo para postar o usuario , com nome, cpf. data de nascimento,senha, empresa e status" +
            " recebe usuariorequest com metodo Requestbody")
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

    /** Atualiza os dados da conta; uma senha ausente ou em branco mantém a senha atual. */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza tudo",description = "Metodo de atualização de usuario recebe como parametro path variable que recebe ID e requestbody recebe Usuariorequest")// atualiza tudo
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

    /** Altera apenas o status da conta, sem reescrever os demais campos. */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza somente status",description = "Metodo de atualização de status do usuario e recebe como parametro o Path variable que recebe ID e request body que recebe AtualizaStatusUsuarioRequest ")// atualiza pontualmente
    public ResponseEntity<UsuarioResponse> AtualizarStatus(@PathVariable Long id, @RequestBody AtualizaStatusUsuarioRequest usuarioRequest){

        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);

        if(usuarioBanco != null){
            usuarioBanco.setStatus(usuarioRequest.getStatus());
            usuarioRepository.save(usuarioBanco);

            return ResponseEntity.ok(new UsuarioResponse(usuarioBanco.getId(), "Status atualizado com sucesso!"));

        }
        return ResponseEntity.notFound().build();
    }


    /** Desativa logicamente a conta; não remove o registro do banco. */
    @DeleteMapping("/{id}")
    @Operation(summary = "deleta ( muda status para D )",description = "Metodo deleta  usuario por ID , recebe como parametro o Path variable")// deletar
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
