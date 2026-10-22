package com.example.shiftsync.services;

import com.example.shiftsync.DTO.FuncionarioRequest;
import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final DepartamentoRepository departamentoRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              DepartamentoRepository departamentoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.departamentoRepository = departamentoRepository;
    }

    @Transactional
    public Optional<Funcionario> atualizarFuncionario(Long id, FuncionarioRequest request) {
        return funcionarioRepository.findById(id).map(funcionario -> {
            funcionario.setTelefone(request.getTelefone());
            funcionario.setEndereco(request.getEndereco());
            funcionario.setFuncao(request.getFuncao());
            funcionario.setTurno(request.getTurno());
            if (request.getDepartamentoId() != null || request.getDepartamento() != null) {
                funcionario.setDepartamento(resolverDepartamento(request));
            }
            funcionario.setDataAtualizacao(LocalDateTime.now());
            return funcionarioRepository.save(funcionario);
        });
    }

    private Departamento resolverDepartamento(FuncionarioRequest request) {
        if (request.getDepartamentoId() != null) {
            return departamentoRepository.findById(request.getDepartamentoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Departamento não encontrado."));
        }

        String nomeDepartamento = request.getDepartamento();
        if (nomeDepartamento == null || nomeDepartamento.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O departamento informado é inválido.");
        }

        return departamentoRepository.findByDepartamentoIgnoreCase(nomeDepartamento.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Departamento não encontrado: " + nomeDepartamento));
    }
}
