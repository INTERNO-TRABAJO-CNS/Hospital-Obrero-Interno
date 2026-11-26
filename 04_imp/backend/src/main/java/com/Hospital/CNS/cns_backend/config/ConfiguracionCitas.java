package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgAgendaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgCambioCitaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgOcupacionPaciente;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgSolicitudRepository;
import com.Hospital.CNS.cns_backend.infrastructure.planificador.ProcesadorCola;
import com.Hospital.CNS.cns_backend.model.citas.AgendaRepository;
import com.Hospital.CNS.cns_backend.model.citas.AsignacionService;
import com.Hospital.CNS.cns_backend.model.citas.CalendarioHabil;
import com.Hospital.CNS.cns_backend.model.citas.CitaService;
import com.Hospital.CNS.cns_backend.model.citas.ConsultaCitasPacienteService;
import com.Hospital.CNS.cns_backend.model.citas.EdadPaciente;
import com.Hospital.CNS.cns_backend.model.citas.OcupacionPaciente;
import com.Hospital.CNS.cns_backend.model.citas.ReprogramacionService;
import com.Hospital.CNS.cns_backend.model.compartido.CancelacionCita;
import com.Hospital.CNS.cns_backend.model.citas.DisponibilidadService;
import com.Hospital.CNS.cns_backend.model.citas.SolicitudService;
import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.operaciones.CancelacionService;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Citas (T100-02 CU-06 y T100-03 CU-05/CU-07): une su model con PostgreSQL y activa el proceso que asigna la cola
 * cada 5 segundos. AgendaEventos lo declara ConfiguracionReportes (T100-04): avisa por WebSocket.
 */
@Configuration
@EnableScheduling
public class ConfiguracionCitas {

    @Bean
    SolicitudRepository solicitudRepository() {
        return new PgSolicitudRepository();
    }

    @Bean
    CambioCitaRepository cambioCitaRepository() {
        return new PgCambioCitaRepository();
    }

    @Bean
    AgendaRepository agendaRepository(CitaRepository citas) {
        return new PgAgendaRepository(citas);
    }

    @Bean
    CalendarioHabil calendarioHabil(DiaNoHabilRepository dias) {
        return new CalendarioHabil(dias);
    }

    /** T100-24: edad del paciente (Pediatria solo menores de 10) a partir de su fecha de nacimiento. */
    @Bean
    EdadPaciente edadPaciente(PacienteRepository pacientes) {
        return pacienteId -> pacientes.porId(pacienteId).map(PacienteDTO::fechaNacimiento);
    }

    /** T100-24: cruces del paciente con sus fichas internas y cirugias. */
    @Bean
    OcupacionPaciente ocupacionPaciente() {
        return new PgOcupacionPaciente();
    }

    /** CU-04 (T100-06): el paciente registra la solicitud (especialidad y dia); la cola de abajo la asigna. */
    @Bean
    SolicitudService solicitudService(SolicitudRepository solicitudes, CitaRepository citas,
                                      CalendarioHabil calendario, Reloj reloj, EdadPaciente edades) {
        return new SolicitudService(solicitudes, citas, calendario, reloj, edades);
    }

    @Bean
    DisponibilidadService disponibilidadService(AgendaRepository agenda, CitaRepository citas,
                                                OcupacionPaciente ocupacion) {
        return new DisponibilidadService(agenda, citas, ocupacion);
    }

    @Bean
    CitaService citaService(CitaRepository citas, SolicitudRepository solicitudes, CambioCitaRepository cambios,
                            NotificacionPublicador avisos, AgendaEventos eventos, Reloj reloj,
                            Transaccion transaccion) {
        return new CitaService(citas, solicitudes, cambios, avisos, eventos, reloj, transaccion);
    }

    @Bean
    CancelacionService cancelacionService(CitaRepository citas, CambioCitaRepository cambios,
                                          NotificacionPublicador avisos, AgendaEventos eventos, Reloj reloj) {
        return new CancelacionService(citas, cambios, avisos, eventos, reloj);
    }

    /** CU-08 (T100-07): mis citas y detalle. */
    @Bean
    ConsultaCitasPacienteService consultaCitasPacienteService(CitaRepository citas) {
        return new ConsultaCitasPacienteService(citas);
    }

    /** CU-10 (T100-07): cancelar + nueva solicitud con prioridad 2, en una transaccion. */
    @Bean
    ReprogramacionService reprogramacionService(CitaRepository citas, CancelacionCita cancelacion,
                                                SolicitudService solicitudes, CalendarioHabil calendario,
                                                Reloj reloj, Transaccion transaccion) {
        return new ReprogramacionService(citas, cancelacion, solicitudes, calendario, reloj, transaccion);
    }

    @Bean
    AsignacionService asignacionService(SolicitudRepository solicitudes, CitaRepository citas,
                                        DisponibilidadService disponibilidad, MedicoRepository medicos,
                                        HorarioRepository horarios, CalendarioHabil calendario, CitaService registro,
                                        Reloj reloj, Transaccion transaccion, EdadPaciente edades) {
        return new AsignacionService(solicitudes, citas, disponibilidad, medicos, horarios, calendario, registro, reloj,
                transaccion, edades);
    }

    @Bean
    ProcesadorCola procesadorCola(AsignacionService service) {
        return new ProcesadorCola(service);
    }
}
