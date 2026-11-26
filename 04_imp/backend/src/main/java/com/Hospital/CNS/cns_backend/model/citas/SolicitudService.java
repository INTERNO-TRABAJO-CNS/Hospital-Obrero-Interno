package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.ReglasEdad;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-04 (T100-06): el paciente pide una cita eligiendo la especialidad y el dia (RF-04, RU-01, RN-01). No elige
 * medico, hora ni consultorio: la solicitud entra a la cola como PENDIENTE para ese dia y CU-05
 * ({@link AsignacionService}) la asigna en su turno. Si ese dia no hay cupo queda EN_ESPERA para el siguiente dia
 * habil, hasta 15 dias habiles (despues: SIN_CUPO).
 *
 * <p>Reglas: dia habil, desde hoy hasta 15 dias habiles despues (la misma ventana de la asignacion); el paciente no
 * puede tener otra cita ni otra solicitud activa ese dia (RN-10) ni otra solicitud activa de la misma especialidad;
 * Pediatria solo para menores de 10 anios el dia de la cita (T100-24/T100-26).</p>
 *
 * <p>La reprogramacion (CU-10) usa las mismas reglas con prioridad 2.</p>
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
    private final EdadPaciente edades;

    /** Sin datos de edad (pruebas con Fakes): no aplica la regla de Pediatria. */
    public SolicitudService(SolicitudRepository solicitudes, CitaRepository citas, CalendarioHabil calendario,
                            Reloj reloj) {
        this(solicitudes, citas, calendario, reloj, EdadPaciente.DESCONOCIDA);
    }

    public SolicitudService(SolicitudRepository solicitudes, CitaRepository citas, CalendarioHabil calendario,
                            Reloj reloj, EdadPaciente edades) {
        this.solicitudes = Objects.requireNonNull(solicitudes, "solicitudes");
        this.citas = Objects.requireNonNull(citas, "citas");
        this.calendario = Objects.requireNonNull(calendario, "calendario");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
        this.edades = Objects.requireNonNull(edades, "edades");
    }

    /** Solicitud normal (prioridad 1) para el dia que eligio el paciente. */
    public SolicitudDTO solicitar(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        validarDatos(pacienteId, especialidad, dia);
        validarDia(dia);
        validarEdad(pacienteId, especialidad, dia);
        validarSinSolicitudActiva(pacienteId, especialidad);
        validarDisponibilidadPaciente(pacienteId, dia);
        return guardar(pacienteId, especialidad, dia, PRIORIDAD_NORMAL);
    }

    /** Solicitud que nace de cancelar o reprogramar una cita (CU-10): prioridad 2, va despues de las normales. */
    public SolicitudDTO solicitarReprogramada(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        validarDatos(pacienteId, especialidad, dia);
        validarDia(dia);
        validarEdad(pacienteId, especialidad, dia);
        validarDisponibilidadPaciente(pacienteId, dia);
        return guardar(pacienteId, especialidad, dia, PRIORIDAD_REPROGRAMADA);
    }

    /**
     * CU-10: primer dia habil desde {@code desde} (incluido) en el que el paciente puede tener la solicitud
     * reprogramada: dentro de la ventana de 15 dias habiles y sin otra cita ni solicitud activa ese dia. Vacio si
     * no hay ninguno en la ventana.
     */
    public Optional<LocalDate> primerDiaReprogramable(long pacienteId, LocalDate desde) {
        LocalDate hoy = reloj.ahora().toLocalDate();
        LocalDate limite = calendario.sumarDiasHabiles(hoy, DIAS_HABILES_MAXIMOS);
        LocalDate dia = desde.isBefore(hoy) ? hoy : desde;
        if (!calendario.esHabil(dia)) {
            dia = calendario.siguienteDiaHabil(dia);
        }
        while (!dia.isAfter(limite)) {
            if (!citas.pacienteTieneCitaEnFecha(pacienteId, dia)
                    && !solicitudes.pacienteTieneSolicitudActivaEnDia(pacienteId, dia)) {
                return Optional.of(dia);
            }
            dia = calendario.siguienteDiaHabil(dia);
        }
        return Optional.empty();
    }

    /** Dias que el paciente puede elegir en CU-04: los habiles desde hoy hasta 15 dias habiles despues. */
    public List<LocalDate> diasHabilesDisponibles() {
        LocalDate hoy = reloj.ahora().toLocalDate();
        LocalDate limite = calendario.sumarDiasHabiles(hoy, DIAS_HABILES_MAXIMOS);
        List<LocalDate> dias = new ArrayList<>();
        LocalDate dia = calendario.esHabil(hoy) ? hoy : calendario.siguienteDiaHabil(hoy);
        while (!dia.isAfter(limite)) {
            dias.add(dia);
            dia = calendario.siguienteDiaHabil(dia);
        }
        return dias;
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

    private static void validarDatos(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        if (pacienteId <= 0 || especialidad == null || dia == null) {
            throw new DominioException("DATOS_INVALIDOS", "Debe indicar la especialidad y el dia");
        }
        if (!especialidad.esBasica()) {
            throw new DominioException("ESPECIALIDAD_INVALIDA",
                    "Solo se puede pedir Consultorio Familiar, Odontologia o Pediatria");
        }
    }

    /** T100-24/T100-26: Pediatria solo atiende a menores de 10 anios (edad cumplida el dia de la cita). */
    private void validarEdad(long pacienteId, TipoEspecialidad especialidad, LocalDate dia) {
        if (especialidad != TipoEspecialidad.PEDIATRIA) {
            return;
        }
        edades.fechaNacimiento(pacienteId).ifPresent(nacimiento -> {
            if (!ReglasEdad.admitidoEnPediatria(ReglasEdad.edadEn(nacimiento, dia))) {
                throw new DominioException("EDAD_NO_PEDIATRICA",
                        "Pediatria solo atiende a menores de " + ReglasEdad.EDAD_LIMITE_PEDIATRIA + " anios");
            }
        });
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
