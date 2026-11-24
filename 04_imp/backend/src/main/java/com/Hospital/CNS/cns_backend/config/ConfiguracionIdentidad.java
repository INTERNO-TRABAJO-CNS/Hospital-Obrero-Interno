package com.Hospital.CNS.cns_backend.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgAfiliadoRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgPacienteRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgSesionRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgUsuarioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.seguridad.BCryptCifradorClave;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoRepository;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.CifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.LimitadorIntentos;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;
import com.Hospital.CNS.cns_backend.model.identidad.SesionRepository;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioRepository;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioService;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/** Une el model de identidad (CU-01, CU-03, cuentas del personal) con PostgreSQL y BCrypt. */
@Configuration
public class ConfiguracionIdentidad {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracionIdentidad.class);

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
    SesionRepository sesionRepository() {
        return new PgSesionRepository();
    }

    @Bean
    CifradorClave cifradorClave() {
        return new BCryptCifradorClave();
    }

    @Bean
    LimitadorIntentos limitadorIntentos(Reloj reloj,
                                        @Value("${cns.intentos.maximo:5}") int maximo,
                                        @Value("${cns.intentos.ventana-minutos:15}") long ventana,
                                        @Value("${cns.intentos.bloqueo-minutos:15}") long bloqueo) {
        return new LimitadorIntentos(maximo, Duration.ofMinutes(ventana), Duration.ofMinutes(bloqueo), reloj);
    }

    @Bean
    PacienteService pacienteService(PacienteRepository pacientes, AfiliadoRepository afiliados,
                                    HistorialRepository historiales, UsuarioRepository usuarios,
                                    CifradorClave cifrador, Reloj reloj, Transaccion transaccion,
                                    LimitadorIntentos limitador) {
        return new PacienteService(pacientes, afiliados, historiales, usuarios, cifrador, reloj, transaccion, limitador);
    }

    @Bean
    AuthService authService(UsuarioRepository usuarios, CifradorClave cifrador, SesionRepository sesiones,
                            PacienteRepository pacientes, LimitadorIntentos limitador, Reloj reloj,
                            @Value("${cns.sesion.horas:8}") long horas) {
        return new AuthService(usuarios, cifrador, sesiones, pacientes, limitador, reloj, Duration.ofHours(horas));
    }

    @Bean
    UsuarioService usuarioService(UsuarioRepository usuarios, SesionRepository sesiones, CifradorClave cifrador,
                                  MedicoRepository medicos, Reloj reloj) {
        return new UsuarioService(usuarios, sesiones, cifrador, medicos, reloj);
    }

    /**
     * Al arrancar: si no hay ningun ADMINISTRADOR activo y CNS_ADMIN_CLAVE tiene valor, crea el primero.
     * La clave nunca va en el repositorio.
     */
    @Bean
    ApplicationRunner administradorInicial(UsuarioService usuarios,
                                           @Value("${cns.admin.usuario:admin}") String usuario,
                                           @Value("${cns.admin.clave:}") String clave,
                                           @Value("${cns.admin.nombre:Administrador del sistema}") String nombre) {
        return argumentos -> {
            try {
                if (usuarios.crearAdministradorInicial(usuario, clave, nombre)) {
                    LOG.info("Se creo el administrador inicial '{}'", usuario);
                } else if (clave == null || clave.isBlank()) {
                    LOG.debug("CNS_ADMIN_CLAVE vacia: no se crea administrador inicial");
                }
            } catch (DominioException e) {
                LOG.warn("No se pudo crear el administrador inicial '{}': {}", usuario, e.getMessage());
            }
        };
    }
}
