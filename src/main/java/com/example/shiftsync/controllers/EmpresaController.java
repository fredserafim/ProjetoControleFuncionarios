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

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

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

    @GetMapping
    public List<EmpresaConsultaResponse>listarTodos(){

        return empresaRepository.findAll().stream().map(EmpresaConsultaResponse::new).toList();
    }

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
