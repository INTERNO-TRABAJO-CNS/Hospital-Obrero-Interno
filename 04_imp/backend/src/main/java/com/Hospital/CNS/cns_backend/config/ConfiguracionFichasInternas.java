package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgCambioFichaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgFichaInternaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgNotificacionPublicador;
import com.Hospital.CNS.cns_backend.infrastructure.planificador.ProcesadorFichasEnEspera;
import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaRepository;
import com.Hospital.CNS.cns_backend.model.cirugias.OcupacionCirugias;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.fichasinternas.AsignadorFichaInterna;
import com.Hospital.CNS.cns_backend.model.fichasinternas.CambioFichaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaService;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Fichas internas (T100-17, 20, 21 y 22): une su model con PostgreSQL y activa el proceso de las 06:00 que
 * asigna las fichas EN_ESPERA.
 */
@Configuration
@EnableScheduling
public class ConfiguracionFichasInternas {

    @Bean
    FichaInternaRepository fichaInternaRepository() {
        return new PgFichaInternaRepository();
    }

    @Bean
    CambioFichaRepository cambioFichaRepository() {
        return new PgCambioFichaRepository();
    }

    @Bean
    NotificacionPublicador notificacionPublicador() {
        return new PgNotificacionPublicador();
    }

    @Bean
    AsignadorFichaInterna asignadorFichaInterna(FichaInternaRepository fichas, MedicoRepository medicos,
                                                HorarioRepository horarios, ConsultorioRepository consultorios,
                                                CitaRepository citas, DiaNoHabilRepository dias,
                                                AsignadorFichaInterna.OcupacionExtra extra, Reloj reloj) {
        return new AsignadorFichaInterna(fichas, medicos, horarios, consultorios, citas, dias, extra, reloj);
    }

    /** Un paciente o un especialista que esta en cirugia (T100-23) no esta libre para una ficha. */
    @Bean
    AsignadorFichaInterna.OcupacionExtra ocupacionExtra(CirugiaRepository cirugias) {
        return new OcupacionCirugias(cirugias);
    }

    @Bean
    FichaInternaService fichaInternaService(FichaInternaRepository fichas, CitaRepository citas,
                                            MedicoRepository medicos, PacienteRepository pacientes,
                                            AsignadorFichaInterna asignador, CambioFichaRepository cambios,
                                            NotificacionPublicador avisos, Transaccion transaccion, Reloj reloj) {
        return new FichaInternaService(fichas, citas, medicos, pacientes, asignador, cambios, avisos, transaccion, reloj);
    }

    @Bean
    ProcesadorFichasEnEspera procesadorFichasEnEspera(FichaInternaService service) {
        return new ProcesadorFichasEnEspera(service);
    }
}
