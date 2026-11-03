package com.example.shiftsync.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Mantém erros HTTP da API em formato previsível sem expor detalhes internos. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> tratarStatus(ResponseStatusException exception) {
        String message = exception.getReason() == null
                ? exception.getStatusCode().toString()
                : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ApiError(exception.getStatusCode().value(), message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> tratarRotaInexistente(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Rota não encontrada."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarRequisicaoMalformada(
            HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Dados inválidos ou em formato incorreto."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarErroInterno(Exception exception) {
        LOGGER.error("Erro não tratado durante o processamento da requisição.", exception);
        return ResponseEntity.internalServerError()
                .body(new ApiError(500, "O servidor encontrou um erro. Tente novamente mais tarde."));
    }

    public record ApiError(int status, String message) {
    }
}
