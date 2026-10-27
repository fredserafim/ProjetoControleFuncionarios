package com.example.shiftsync.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter  extends OncePerRequestFilter {

    @Value("${spring.secret}")
    private String secret;

    @Value("${spring.emissor}")
    private String emissor;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String caminho = request.getRequestURI();

        if (request.getMethod().equals("OPTIONS")
                || caminho.equals("/login")
                || caminho.startsWith("/swagger-ui")
                || caminho.startsWith("/webjars")
                || caminho.startsWith("/swagger-resources")
                || caminho.startsWith("/v3/api-docs")){
            filterChain.doFilter(request,response);
            return;
        }
        String headerToken = request.getHeader("Authorization");

        if (headerToken == null || !headerToken.startsWith("Bearer ")
                || headerToken.substring(7).isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"mensagem\":\"Token inválido ou expirado. Faça login novamente.\"}");
            return;
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(emissor)
                    .build();
            DecodedJWT token = verifier.verify(headerToken.substring(7));
            request.setAttribute("authenticatedCpf", token.getSubject());
        } catch (JWTVerificationException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"mensagem\":\"Token inválido ou expirado. Faça login novamente.\"}");
            return;
        }

        filterChain.doFilter(request,response);

    }
}
