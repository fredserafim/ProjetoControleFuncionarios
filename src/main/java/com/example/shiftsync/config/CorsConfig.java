package com.example.shiftsync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica para todas as rotas da API (/login, /funcionarios, /turnos, etc.)
                .allowedOriginPatterns("*") // Libera qualquer origem (frontend local)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD") // Métodos HTTP permitidos
                .allowedHeaders("*") // Libera todos os headers (Content-Type, Authorization, etc.)
                .allowCredentials(true); // Permite envio de credenciais (cookies/tokens se necessário)
    }
}