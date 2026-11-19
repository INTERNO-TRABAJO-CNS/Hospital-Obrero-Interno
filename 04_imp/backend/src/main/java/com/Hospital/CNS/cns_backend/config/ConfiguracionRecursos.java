package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgCitaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgConsultorioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgHorarioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgMedicoRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgSolicitudRepository;

import com.Hospital.CNS.cns_backend.model.citas.CalendarioHabil;
import com.Hospital.CNS.cns_backend.model.citas.CalendarioHabilImpl;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.citas.SolicitudService;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

import com.Hospital.CNS.cns_backend.model.recursos.CatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Configuración de los repositorios y servicios de recursos.
 */
@Configuration
public class ConfiguracionRecursos {

    @Bean
    MedicoRepository medicoRepository() {
        return new PgMedicoRepository();
    }

    @Bean
    ConsultorioRepository consultorioRepository() {
        return new PgConsultorioRepository();
    }

    @Bean
    HorarioRepository horarioRepository() {
        return new PgHorarioRepository();
    }

    @Bean
    DiaNoHabilRepository diaNoHabilRepository() {
        return new PgDiaNoHabilRepository();
    }

    @Bean
    CitaRepository citaRepository() {
        return new PgCitaRepository();
    }

    @Bean
    SolicitudRepository solicitudRepository() {
        return new PgSolicitudRepository();
    }

    /**
     * Calendario que valida fines de semana y feriados registrados.
     */
    @Bean
    CalendarioHabil calendarioHabil(DiaNoHabilRepository diasNoHabiles) {
        return new CalendarioHabilImpl(diasNoHabiles);
    }

    @Bean
    SolicitudService solicitudService(
            SolicitudRepository solicitudes,
            CitaRepository citas,
            CalendarioHabil calendario,
            Reloj reloj) {

        return new SolicitudService(solicitudes, citas, calendario, reloj);
    }

    @Bean
    CatalogoService catalogoService(
            MedicoRepository medicos,
            ConsultorioRepository consultorios,
            HorarioRepository horarios,
            DiaNoHabilRepository dias,
            CitaRepository citas,
            Reloj reloj,
            Transaccion transaccion) {

        return new CatalogoService(
                medicos,
                consultorios,
                horarios,
                dias,
                citas,
                reloj,
                transaccion
        );
    }
}