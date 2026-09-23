package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.EmpresaRequest;
import com.example.shiftsync.DTO.EmpresaResponse;
import com.example.shiftsync.entities.Empresa;
import com.example.shiftsync.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @PostMapping("/criar")
    public ResponseEntity<EmpresaResponse> casastrarEmpresa(@RequestBody EmpresaRequest empresaRequest){

        Empresa empresaBanco = new Empresa();


        empresaBanco.setCnpj(empresaRequest.getCnpj());
        empresaBanco.setGetRazaoSocial(empresaRequest.getRazaoSocial());
        empresaBanco.setIncricaoEstadual(empresaRequest.getIncricaoEstadual());
        empresaBanco.setNomeFantasia(empresaRequest.getNomeFantasia());

        empresaRepository.save(empresaBanco);

        EmpresaResponse empresaResponse = new EmpresaResponse();

        empresaResponse.setId(empresaBanco.getId());
        empresaResponse.setMensagem("Cadastro da empresa realizado com sucesso!");

        return ResponseEntity.ok(empresaResponse);
    }
}
