package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-04 (T100-06): el paciente pide una cita eligiendo SOLO la especialidad. No elige dia, medico, hora ni
 * consultorio: la solicitud entra a la cola como PENDIENTE desde el primer dia habil (hoy si es habil) y CU-05
 * ({@link AsignacionService}) la asigna en su turno al primer cupo libre, avanzando dia habil por dia habil hasta
 * 15 dias habiles (despues: SIN_CUPO).
 *
 * <p>Regla: el paciente no puede tener otra solicitud activa (PENDIENTE o EN_ESPERA) de la misma especialidad.
 * Si ya tiene cita en un dia, la asignacion salta ese dia.</p>
 *
 * <p>La reprogramacion (CU-10) si parte de un dia concreto: dia habil dentro de la ventana y sin otra cita ni
 * solicitud activa ese dia.</p>
 */
public class SolicitudService {

    /** Mismo limite que {@code AsignacionService}: despues de 15 dias habiles la solicitud pasa a SIN_CUPO. */
    static final int DIAS_HABILES_MAXIMOS = 15;

    static final int PRIORIDAD_NORMAL = 1;
    static final int PRIORIDAD_REPROGRAMADA = 2;

    private final SolicitudRepository solicitudes;
    private final CitaRepository citas;
    private final CalendarioHabil calendario;
    private final Reloj reloj;

    public SolicitudService(SolicitudRepository solicitudes, CitaRepository citas, CalendarioHabil calendario,
                            Reloj reloj) {
        this.solicitudes = Objects.requireNonNull(solicitudes, "solicitudes");
        this.citas = Objects.requireNonNull(citas, "citas");
        this.calendario = Objects.requireNonNull(calendario, "calendario");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
    }

    /**
     * Solicitud normal (prioridad 1). El dia NO lo elige el paciente: la busqueda de cupo empieza el primer dia habil
     * desde hoy.
     */
    public SolicitudDTO solicitar(long pacienteId, TipoEspecialidad especialidad) {
        validarDatos(pacienteId, especialidad);
        validarSinSolicitudActiva(pacienteId, especialidad);
        LocalDate hoy = reloj.ahora().toLocalDate();
        LocalDate desde = calendario.esHabil(hoy) ? hoy : calendario.siguienteDiaHabil(hoy);
        return guardar(pacienteId, especialidad, desde, PRIORIDAD_NORMAL);
    }

    /** Solicitud que nace de cancelar o reprogramar una cita (CU-10): prioridad 2, va despues de las normales. */
    public SolicitudDTO solicitarReprogramada(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        validarDatos(pacienteId, especialidad);
        if (dia == null) {
            throw new DominioException("DATOS_INVALIDOS", "Debe indicar el dia de la reprogramacion");
        }
        validarDia(dia);
        validarDisponibilidadPaciente(pacienteId, dia);
        return guardar(pacienteId, especialidad, dia, PRIORIDAD_REPROGRAMADA);
    }

    public List<SolicitudDTO> listarPorPaciente(long pacienteId) {
        if (pacienteId <= 0) {
            throw new DominioException("DATOS_INVALIDOS", "El identificador del paciente no es valido");
        }
        return solicitudes.porPaciente(pacienteId);
    }

    public SolicitudDTO consultar(long pacienteId, long solicitudId) {
        if (pacienteId <= 0 || solicitudId <= 0) {
            throw new DominioException("DATOS_INVALIDOS", "Los identificadores deben ser validos");
        }
        SolicitudDTO solicitud = solicitudes.porId(solicitudId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No se encontro la solicitud"));
        if (solicitud.pacienteId() != pacienteId) {
            throw new DominioException("NO_ES_DEL_PACIENTE", "No tiene permiso para consultar esta solicitud");
        }
        return solicitud;
    }

    private SolicitudDTO guardar(long pacienteId, TipoEspecialidad especialidad, LocalDate dia, int prioridad) {
        long id = solicitudes.nextIdentity();
        // ordenLlegada 0: lo pone la base (secuencia citas.solicitud_orden_seq), asi es creciente aunque dos
        // pacientes pidan a la vez. Al guardarla, se vuelve a leer para devolver el orden real.
        solicitudes.store(new SolicitudDTO(id, pacienteId, especialidad, dia, prioridad, 0,
                EstadoSolicitud.PENDIENTE, null));
        return solicitudes.porId(id).orElseThrow();
    }

    private static void validarDatos(long pacienteId, TipoEspecialidad especialidad) {
        if (pacienteId <= 0 || especialidad == null) {
            throw new DominioException("DATOS_INVALIDOS", "Debe indicar la especialidad");
        }
        if (!especialidad.esBasica()) {
            throw new DominioException("ESPECIALIDAD_INVALIDA",
                    "Solo se puede pedir Consultorio Familiar, Odontologia o Pediatria");
        }
    }

    private void validarSinSolicitudActiva(long pacienteId, TipoEspecialidad especialidad) {
        boolean yaTiene = solicitudes.porPaciente(pacienteId).stream()
                .anyMatch(s -> s.especialidad() == especialidad
                        && (s.estado() == EstadoSolicitud.PENDIENTE || s.estado() == EstadoSolicitud.EN_ESPERA));
        if (yaTiene) {
            throw new DominioException("YA_TIENE_SOLICITUD_ACTIVA",
                    "Ya tiene una solicitud en curso para esta especialidad; el sistema le avisara cuando haya cupo");
        }
    }

    private void validarDia(LocalDate dia) {
        if (!calendario.esHabil(dia)) {
            throw new DominioException("DIA_NO_HABIL", "El dia seleccionado no es habil");
        }
        LocalDate hoy = reloj.ahora().toLocalDate();
        LocalDate limite = calendario.sumarDiasHabiles(hoy, DIAS_HABILES_MAXIMOS);
        if (dia.isBefore(hoy) || dia.isAfter(limite)) {
            throw new DominioException("FUERA_DE_RANGO",
                    "La fecha debe estar dentro de los proximos " + DIAS_HABILES_MAXIMOS + " dias habiles");
        }
    }

    private void validarDisponibilidadPaciente(long pacienteId, LocalDate dia) {
        if (citas.pacienteTieneCitaEnFecha(pacienteId, dia)
                || solicitudes.pacienteTieneSolicitudActivaEnDia(pacienteId, dia)) {
            throw new DominioException("YA_TIENE_CITA_ESE_DIA",
                    "Ya tiene una cita o una solicitud para ese dia");
        }
    }
}
