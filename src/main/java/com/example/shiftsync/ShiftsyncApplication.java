package com.example.shiftsync;

import ch.qos.logback.core.encoder.JsonEscapeUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.ls.LSOutput;

/**
 * Ponto de entrada do Spring Boot.
 *
 * A anotação combina configuração, varredura de componentes e auto-configuração
 * para iniciar controllers, repositories e demais beans encontrados no pacote.
 */
@SpringBootApplication
public class ShiftsyncApplication {

    /** Inicializa o contexto Spring e inicia o servidor da aplicação. */
    public static void main(String[] args) {
        SpringApplication.run(ShiftsyncApplication.class, args);



    }


}
