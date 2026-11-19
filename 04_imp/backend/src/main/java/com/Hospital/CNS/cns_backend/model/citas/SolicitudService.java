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
 * CU-04 (T100-06): el paciente pide una cita eligiendo solo especialidad y dia. La solicitud entra a la cola como
 * PENDIENTE; la asigna CU-05 ({@link AsignacionService}) en su turno.
 *
 * <p>Reglas: dia habil, desde hoy hasta 15 dias habiles despues (la misma ventana que usa la asignacion), y el
 * paciente no puede tener otra cita ni otra solicitud activa ese dia.</p>
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

    /** Solicitud normal (prioridad 1). */
    public SolicitudDTO solicitar(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        return registrar(pacienteId, especialidad, dia, PRIORIDAD_NORMAL);
    }

    /** Solicitud que nace de cancelar o reprogramar una cita (CU-10): prioridad 2, va despues de las normales. */
    public SolicitudDTO solicitarReprogramada(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        return registrar(pacienteId, especialidad, dia, PRIORIDAD_REPROGRAMADA);
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

    private SolicitudDTO registrar(long pacienteId, TipoEspecialidad especialidad, LocalDate dia, int prioridad) {
        validarDatos(pacienteId, especialidad, dia);
        validarDia(dia);
        validarDisponibilidadPaciente(pacienteId, dia);

        long id = solicitudes.nextIdentity();
        // ordenLlegada 0: lo pone la base (secuencia citas.solicitud_orden_seq), asi es creciente aunque dos
        // pacientes pidan a la vez. Al guardarla, se vuelve a leer para devolver el orden real.
        solicitudes.store(new SolicitudDTO(id, pacienteId, especialidad, dia, prioridad, 0,
                EstadoSolicitud.PENDIENTE, null));
        return solicitudes.porId(id).orElseThrow();
    }

    private static void validarDatos(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        if (pacienteId <= 0 || especialidad == null || dia == null) {
            throw new DominioException("DATOS_INVALIDOS", "Debe indicar la especialidad y el dia");
        }
        if (!especialidad.esBasica()) {
            throw new DominioException("ESPECIALIDAD_INVALIDA",
                    "Solo se puede pedir Consultorio Familiar, Odontologia o Pediatria");
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
