package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.Hospital.CNS.cns_backend.controller.FiltroAutenticacion;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;

/**
 * Seguridad real (reemplaza a ConfiguracionSeguridadTemporal, decision PV-11 del 13/11/2026):
 * <ul>
 *   <li>Cada peticion a /api/** lleva {@code Authorization: Bearer <token>} (salvo login y registro del titular);
 *       lo revisa {@link FiltroAutenticacion} y cada controller pide el rol que necesita.</li>
 *   <li>Sin sesiones de servidor ni cookies: por eso CSRF no aplica y se desactiva.</li>
 *   <li>CORS: usa el CorsConfigurationSource de ConfiguracionCors (web Nuxt del medico).</li>
 * </ul>
 */
@Configuration
public class ConfiguracionSeguridad {

    @Bean
    SecurityFilterChain filtroSeguridad(HttpSecurity http, AuthService auth) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizaciones -> autorizaciones.anyRequest().permitAll())
                .addFilterBefore(new FiltroAutenticacion(auth), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
