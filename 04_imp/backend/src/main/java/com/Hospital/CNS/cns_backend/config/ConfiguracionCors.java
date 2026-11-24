package com.Hospital.CNS.cns_backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * La app Flutter en el navegador (otro origen) puede llamar a /api/**. El origen sale de
 * {@code cns.web.origen} (por defecto http://localhost:5050, el puerto de
 * {@code flutter run -d chrome --web-port 5050}). En celular y escritorio no aplica CORS.
 * Spring Security lo usa via http.cors().
 */
@Configuration
public class ConfiguracionCors {

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${cns.web.origen:http://localhost:5050}") String origen) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origen.split("\\s*,\\s*")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
