package com.example.shiftsync.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
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

        if(caminho.equals("/longin")
                || caminho.startsWith("/swagger-ui")
                || caminho.startsWith("/webjars")
                || caminho.startsWith("/swagger-resources")
                || caminho.startsWith("/v3/api.docs")
                ){
            filterChain.doFilter(request,response);
            return;
        }
        String headerToken = request.getHeader("Authorization");

        if(headerToken!=null && headerToken.startsWith("Bearer")){

            String token = headerToken.replace("Bearer ","");

            Algorithm algorithm = Algorithm.HMAC256(secret);

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(emissor)
                    .build();

            var resultado = verifier.verify(token);

            System.out.println(resultado.getSubject());


        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido");
            return;
        }
        filterChain.doFilter(request,response);

    }
}
