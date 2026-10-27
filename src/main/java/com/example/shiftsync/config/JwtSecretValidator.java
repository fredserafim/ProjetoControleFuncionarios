package com.example.shiftsync.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretValidator {

    @Value("${spring.secret}")
    private String secret;

    @PostConstruct
    void validarSegredo() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "Configure JWT_SECRET com pelo menos 32 caracteres.");
        }
    }
}
