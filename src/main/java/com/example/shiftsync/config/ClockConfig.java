package com.example.shiftsync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Fornece um Clock compartilhado para que a aplicação use o fuso de São Paulo.
 *
 * Injetar Clock também permite que testes forneçam um relógio controlado.
 */
@Configuration
public class ClockConfig {

    /** Cria o relógio do sistema no fuso horário usado pelas jornadas da aplicação. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
