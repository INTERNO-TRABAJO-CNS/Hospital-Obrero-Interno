package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.historial.HistorialCambiosService;
import com.Hospital.CNS.cns_backend.model.operaciones.ActualizacionEstadoService;
import com.Hospital.CNS.cns_backend.model.operaciones.FichasDelDiaService;

/**
 * Operaciones del dia: fichas de hoy y TV (CU-09), confirmar asistencia y fin de atencion (CU-12) e historial de
 * cambios (CU-18).
 */
@Configuration
public class ConfiguracionOperaciones {

    @Bean
    FichasDelDiaService fichasDelDiaService(CitaRepository citasRepository, Reloj reloj) {
        return new FichasDelDiaService(citasRepository, reloj);
    }

    /** Es tambien la {@code FinalizacionCita} que usa AtencionService (CU-15). */
    @Bean
    ActualizacionEstadoService actualizacionEstadoService(CitaRepository citas, CambioCitaRepository cambios,
                                                          AgendaEventos eventos, Reloj reloj) {
        return new ActualizacionEstadoService(citas, cambios, eventos, reloj);
    }

    @Bean
    HistorialCambiosService historialCambiosService(CambioCitaRepository cambios, CitaRepository citas) {
        return new HistorialCambiosService(cambios, citas);
    }
}
