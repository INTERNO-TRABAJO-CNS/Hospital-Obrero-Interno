package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgAfiliadoRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgPacienteRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgUsuarioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.seguridad.BCryptCifradorClave;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoRepository;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.CifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioRepository;

/** Une el model de identidad (CU-01, CU-03) con PostgreSQL y BCrypt. */
@Configuration
public class ConfiguracionIdentidad {

    @Bean
    AfiliadoRepository afiliadoRepository() {
        return new PgAfiliadoRepository();
    }

    @Bean
    PacienteRepository pacienteRepository() {
        return new PgPacienteRepository();
    }

    @Bean
    UsuarioRepository usuarioRepository() {
        return new PgUsuarioRepository();
    }

    @Bean
    CifradorClave cifradorClave() {
        return new BCryptCifradorClave();
    }

    @Bean
    PacienteService pacienteService(PacienteRepository pacientes, AfiliadoRepository afiliados,
                                    HistorialRepository historiales, UsuarioRepository usuarios,
                                    CifradorClave cifrador, Reloj reloj) {
        return new PacienteService(pacientes, afiliados, historiales, usuarios, cifrador, reloj);
    }

    @Bean
    AuthService authService(UsuarioRepository usuarios, CifradorClave cifrador) {
        return new AuthService(usuarios, cifrador);
    }
}
