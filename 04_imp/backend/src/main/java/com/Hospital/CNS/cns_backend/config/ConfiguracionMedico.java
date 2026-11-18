package com.Hospital.CNS.cns_backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgHistorialRepository;
import com.Hospital.CNS.cns_backend.model.atencion.AtencionService;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FinalizacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.historial.HistorialService;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.operaciones.AgendaMedicaService;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoActualService;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Une el model del medico (agenda CU-13, historial CU-14, atencion CU-15) con su base de datos.
 * El model no conoce Spring: los Service se crean aqui. El Reloj (hora del hospital) sale de
 * ConfiguracionBaseDatos.
 */
@Configuration
public class ConfiguracionMedico {

    @Bean
    HistorialRepository historialRepository() {
        return new PgHistorialRepository();
    }

    @Bean
    MedicoActualService medicoActualService(MedicoRepository medicos) {
        return new MedicoActualService(medicos);
    }

    @Bean
    AgendaMedicaService agendaMedicaService(CitaRepository citas, Reloj reloj) {
        return new AgendaMedicaService(citas, reloj);
    }

    @Bean
    HistorialService historialService(HistorialRepository historiales, CitaRepository citas, Reloj reloj,
                                      PacienteRepository pacientes) {
        return new HistorialService(historiales, citas, reloj, pacientes);
    }

    @Bean
    AtencionService atencionService(CitaRepository citas, HistorialRepository historiales,
                                    FinalizacionCita finalizacion, Reloj reloj, Transaccion transaccion,
                                    PacienteRepository pacientes, ConsultorioRepository consultorios) {
        return new AtencionService(citas, historiales, finalizacion, reloj, transaccion, pacientes, consultorios);
    }

    /**
     * TEMPORAL: lo reemplaza la FinalizacionCita de T100-10 (CU-12). Mientras no exista, comprueba que la
     * cita sea del medico y guarda la hora de fin con el UPDATE atomico ASISTENCIA_CONFIRMADA -> ATENDIDA.
     */
    @Bean
    @ConditionalOnMissingBean(FinalizacionCita.class)
    FinalizacionCita finalizacionCitaTemporal(CitaRepository citas, Reloj reloj) {
        return (citaId, medicoId) -> {
            CitaDTO cita = citas.porId(citaId).orElse(null);
            if (cita == null) {
                return ResultadoFinalizacion.NO_ENCONTRADA;
            }
            if (cita.medicoId() != medicoId) {
                return ResultadoFinalizacion.NO_ES_DE_SU_AGENDA;
            }
            return citas.registrarFin(citaId, reloj.ahora())
                    ? ResultadoFinalizacion.FINALIZADA
                    : ResultadoFinalizacion.ESTADO_NO_PERMITE;
        };
    }
}
