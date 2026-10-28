package com.example.shiftsync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Registra o codificador BCrypt usado para comparar e armazenar senhas com hash. */
@Configuration
public class PasswordEncoderConfig {

    /** Disponibiliza a implementação de codificação de senha por injeção de dependência. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
