package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.EmpresaConsultaResponse;
import com.example.shiftsync.DTO.EmpresaRequest;
import com.example.shiftsync.DTO.EmpresaResponse;
import com.example.shiftsync.DTO.UsuarioConsultaResponse;
import com.example.shiftsync.entities.Empresa;
import com.example.shiftsync.entities.Usuario;
import com.example.shiftsync.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints para cadastrar empresas, listá-las e consultar os usuários vinculados. */
@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    /** Copia os dados recebidos para a entidade e persiste uma empresa nova. */
    @PostMapping("/criar")
    public ResponseEntity<EmpresaResponse> casastrarEmpresa(@RequestBody EmpresaRequest empresaRequest){

        Empresa empresaBanco = new Empresa();


        empresaBanco.setCnpj(empresaRequest.getCnpj());
        empresaBanco.setRazaoSocial(empresaRequest.getRazaoSocial());
        empresaBanco.setIncricaoEstadual(empresaRequest.getInscricaoEstadual());
        empresaBanco.setNomeFantasia(empresaRequest.getNomeFantasia());

        empresaRepository.save(empresaBanco);

        EmpresaResponse empresaResponse = new EmpresaResponse();

        empresaResponse.setId(empresaBanco.getId());
        empresaResponse.setMensagem("Cadastro da empresa realizado com sucesso!");

        return ResponseEntity.ok(empresaResponse);
    }

    /** Lista empresas convertendo cada entidade para a projeção de consulta. */
    @GetMapping
    public List<EmpresaConsultaResponse>listarTodos(){

        return empresaRepository.findAll().stream().map(EmpresaConsultaResponse::new).toList();
    }

    /** Localiza a empresa por CNPJ e retorna usuários sem expor suas senhas. */
    @GetMapping("/cnpj/{cnpj}/usuarios")
    public ResponseEntity<List<UsuarioConsultaResponse>> buscarUsuariosPorCnpjEmpresa(@PathVariable String cnpj) {

        var empresaBanco = empresaRepository.getEmpresaByCnpj(cnpj).orElse(null);
        if (empresaBanco == null)
            return ResponseEntity.notFound().build();

        var usuarioEmpresaBanco = empresaBanco.getUsuarios()
                .stream()
                .map(UsuarioConsultaResponse::new)
                .toList();
         return ResponseEntity.ok(usuarioEmpresaBanco);
    }
}
