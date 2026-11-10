package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORAL: lo reemplaza Spring Security de S3-08.
 * Deja pasar todas las peticiones (sin login ni CSRF) para poder probar con Postman
 * usando los headers X-Usuario-Id, X-Rol y X-Persona-Id. Borrar al poner la seguridad real.
 * CORS: usa el CorsConfigurationSource de ConfiguracionCors (web Nuxt del medico).
 */
@Configuration
public class ConfiguracionSeguridadTemporal {

    @Bean
    SecurityFilterChain filtroTemporal(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(autorizaciones -> autorizaciones.anyRequest().permitAll());
        return http.build();
    }
}
