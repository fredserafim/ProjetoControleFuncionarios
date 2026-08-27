package com.example.shiftsync.controllers;

import com.example.shiftsync.DTO.LoginRequest;
import com.example.shiftsync.DTO.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/login")
public class LoginController {

    @PostMapping
    public ResponseEntity<LoginResponse>logar(@RequestBody LoginRequest loginRequest){

        if(loginRequest.getLogin().equals("string")
                && loginRequest.getSenha().equals("string")){

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setMensagem("Bem vindo");

            return ResponseEntity.ok(loginResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
