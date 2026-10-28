package com.example.shiftsync.config;

/*import org.springframework.context.annotation.Configuration;
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
 */


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Define as origens locais autorizadas a acessar a API pelo navegador.
 *
 * O filtro registra a configuração para todas as rotas, métodos e cabeçalhos
 * explicitamente listados; isso não substitui autenticação JWT.
 */
@Configuration
public class CorsConfig {

    /** Constrói e registra o filtro CORS com as origens e métodos permitidos. */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH",
                "DELETE", "OPTIONS", "HEAD"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
