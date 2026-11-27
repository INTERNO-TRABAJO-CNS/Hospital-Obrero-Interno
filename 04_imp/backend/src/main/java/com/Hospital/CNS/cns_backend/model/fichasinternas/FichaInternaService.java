package com.Hospital.CNS.cns_backend.model.fichasinternas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.ComparadorPrioridad;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.ReglasEdad;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * CU-21 (generar ficha interna), CU-23 (agenda del especialista) y CU-25 (cola de prioridad), T100-20/21/22.
 *
 * <p>El {@code medicoId} sale siempre del usuario autenticado (lo pone el controller). Cada rechazo es una
 * {@link DominioException} con su codigo: MEDICO_NO_DERIVA, ESPECIALIDAD_NO_INTERNA, MOTIVO_INVALIDO,
 * TERAPIA_SOLO_URGENCIA, CITA_NO_VALIDA, CITA_YA_DERIVADA, EDAD_NO_PEDIATRICA, PACIENTE_CON_FICHA_ACTIVA.</p>
 *
 * <p>Ficha + derivacion + auditoria + avisos al paciente se guardan en UNA transaccion.</p>
 */
public class FichaInternaService {

    public static final int MAX_MOTIVO = 500;
    public static final int MAX_OBSERVACIONES = 2000;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FichaInternaRepository fichas;
    private final CitaRepository citas;
    private final MedicoRepository medicos;
    private final PacienteRepository pacientes;
    private final AsignadorFichaInterna asignador;
    private final CambioFichaRepository cambios;
    private final NotificacionPublicador avisos;
    private final Transaccion transaccion;
    private final Reloj reloj;

    public FichaInternaService(FichaInternaRepository fichas, CitaRepository citas, MedicoRepository medicos,
                               PacienteRepository pacientes, AsignadorFichaInterna asignador,
                               CambioFichaRepository cambios, NotificacionPublicador avisos, Transaccion transaccion,
                               Reloj reloj) {
        this.fichas = fichas;
        this.citas = citas;
        this.medicos = medicos;
        this.pacientes = pacientes;
        this.asignador = asignador;
        this.cambios = cambios;
        this.avisos = avisos;
        this.transaccion = transaccion;
        this.reloj = reloj;
    }

    /** CU-21: genera la ficha y la asigna por prioridad. Devuelve la ficha como la ve quien derivo. */
    public FichaInternaVistaDTO generar(SolicitudFichaInterna s, long medicoId) {
        MedicoDTO medico = medicoActivo(medicoId);
        if (!medico.especialidad().puedeDerivar()) {
            throw new DominioException("MEDICO_NO_DERIVA", "Solo Medico General y Pediatria pueden derivar");
        }
        if (s.especialidad() == null || !s.especialidad().esInterna()) {
            throw new DominioException("ESPECIALIDAD_NO_INTERNA", "Elija una especialidad interna");
        }
        if (s.gravedad() == null) {
            throw new DominioException("DATOS_INVALIDOS", "Elija la gravedad");
        }
        String motivo = s.motivo() == null ? "" : s.motivo().trim();
        if (motivo.isEmpty() || motivo.length() > MAX_MOTIVO) {
            throw new DominioException("MOTIVO_INVALIDO", "El motivo debe tener de 1 a " + MAX_MOTIVO + " caracteres");
        }
        if (s.especialidad().soloUrgencia() && !s.urgencia()) {
            throw new DominioException("TERAPIA_SOLO_URGENCIA", "Terapia Intensiva solo se asigna con Urgencia");
        }
        LocalDateTime ahora = reloj.ahora();
        LocalDate hoy = ahora.toLocalDate();
        long pacienteId;
        if (s.citaOrigenId() != null) {
            CitaDTO cita = citas.porId(s.citaOrigenId()).orElse(null);
            boolean valida = cita != null && cita.medicoId() == medicoId && cita.fecha().equals(hoy)
                    && (cita.estado() == EstadoCita.ASISTENCIA_CONFIRMADA || cita.estado() == EstadoCita.ATENDIDA);
            if (!valida) {
                throw new DominioException("CITA_NO_VALIDA", "La cita debe ser tuya, de hoy y estar confirmada o atendida");
            }
            fichas.deCita(cita.citaId()).ifPresent(previa -> {
                throw new DominioException("CITA_YA_DERIVADA", "Esta cita ya tiene una ficha interna (Ticket N° "
                        + previa.fichaId() + "). Solo se permite una derivacion por cita");
            });
            pacienteId = cita.pacienteId();
        } else {
            if (!s.urgencia()) {
                throw new DominioException("CITA_NO_VALIDA", "Sin urgencia la ficha se genera desde una cita de hoy");
            }
            if (s.pacienteId() == null) {
                throw new DominioException("DATOS_INVALIDOS", "Busque al paciente por su N° de afiliado");
            }
            pacienteId = s.pacienteId();
        }
        PacienteDTO paciente = pacientes.porId(pacienteId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No existe ese paciente"));
        int edad = ReglasEdad.edadEn(paciente.fechaNacimiento(), hoy);
        boolean pediatrica = medico.especialidad() == TipoEspecialidad.PEDIATRIA;
        if (pediatrica && !ReglasEdad.admitidoEnPediatria(edad)) {
            throw new DominioException("EDAD_NO_PEDIATRICA", "Pediatria solo atiende a menores de 10 anios");
        }

        FichaInternaDTO resultado = transaccion.ejecutar(() -> {
            fichas.bloquearEspecialidad(s.especialidad());
            if (fichas.pacienteTieneFichaActiva(paciente.pacienteId())) {
                throw new DominioException("PACIENTE_CON_FICHA_ACTIVA", "El paciente ya tiene una ficha interna activa");
            }
            long id = fichas.nextIdentity();
            FichaInternaDTO nueva = new FichaInternaDTO(id, s.especialidad(), pediatrica, paciente.pacienteId(),
                    paciente.nombreCompleto(), paciente.numeroAfiliado(), paciente.fechaNacimiento(), s.citaOrigenId(),
                    medicoId, nombre(medico), medico.especialidad(), null, null, null, null, null, null,
                    s.especialidad().duracionMinMinutos(), s.gravedad(), s.urgencia(), ReglasEdad.esAdultoMayor(edad),
                    id, motivo, null, EstadoFichaInterna.EN_ESPERA, ahora);
            fichas.insertar(nueva);
            if (s.citaOrigenId() != null && !fichas.registrarDerivacion(s.citaOrigenId(), id)) {
                throw new DominioException("CITA_YA_DERIVADA", "Esta cita ya tiene una ficha interna");
            }
            cambios.registrar("FICHA", id, "GENERADA", "Medico " + medicoId, null);
            AsignadorFichaInterna.Resultado r = asignador.asignar(nueva);
            avisar(r.ficha(), ahora);
            cambios.registrar("FICHA", id, r.ficha().estado().name(), "Sistema", null);
            if (r.desplazada() != null) {
                cambios.registrar("FICHA", r.desplazada().fichaId(), "DESPLAZADA", "Sistema",
                        "Por la ficha N° " + id + " de mayor prioridad");
                avisarDesplazada(r.desplazada(), ahora);
            }
            return r.ficha();
        });
        return vista(fichas.porId(resultado.fichaId()).orElse(resultado), true);
    }

    /** El proceso de las 06:00: fichas EN_ESPERA buscan hueco sin desplazar. Devuelve cuantas se asignaron. */
    public int procesarEnEspera() {
        LocalDateTime ahora = reloj.ahora();
        if (!asignador.esHabil(ahora.toLocalDate())) {
            return 0;
        }
        return transaccion.ejecutar(() -> {
            List<FichaInternaDTO> asignadas = asignador.procesarEnEspera();
            for (FichaInternaDTO f : asignadas) {
                cambios.registrar("FICHA", f.fichaId(), "ASIGNADA", "Sistema", "Proceso de fichas en espera");
                avisar(f, ahora);
            }
            return asignadas.size();
        });
    }

    /** Ficha derivada desde esa cita (para la agenda del medico), o null. */
    public FichaInternaVistaDTO deCita(long citaId, long medicoId) {
        FichaInternaDTO f = fichas.deCita(citaId).orElse(null);
        if (f == null) {
            return null;
        }
        return vista(f, puedeVerGravedad(medicoId));
    }

    /** Detalle de una ficha: la ve quien derivo, el especialista asignado o el administrador. */
    public FichaInternaVistaDTO porId(long fichaId, Long medicoId) {
        FichaInternaDTO f = fichas.porId(fichaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No existe esa ficha interna"));
        if (medicoId != null) {
            boolean suya = f.medicoDerivanteId() == medicoId || Long.valueOf(medicoId).equals(f.medicoId())
                    || medicoActivo(medicoId).especialidad() == f.especialidad();
            if (!suya) {
                throw new DominioException("ACCESO_DENEGADO", "Esta ficha interna no es tuya");
            }
        }
        return vista(f, medicoId != null && puedeVerGravedad(medicoId));
    }

    /** CU-25: cola de prioridad (solo lectura) para el medico general o el pediatra. */
    public List<FichaInternaVistaDTO> cola(TipoEspecialidad especialidad, boolean pediatrica, long medicoId) {
        if (!medicoActivo(medicoId).especialidad().puedeDerivar()) {
            throw new DominioException("MEDICO_NO_DERIVA", "La cola de prioridad es del medico que deriva");
        }
        if (especialidad == null || !especialidad.esInterna()) {
            throw new DominioException("ESPECIALIDAD_NO_INTERNA", "Elija una especialidad interna");
        }
        List<FichaInternaDTO> lista = new ArrayList<>(fichas.activas(especialidad, pediatrica));
        lista.sort(Comparator.comparing(FichaInternaDTO::prioridad, ComparadorPrioridad.INSTANCIA));
        return lista.stream().map(f -> vista(f, true)).toList();
    }

    /** Fichas de un paciente para su historial: solo si el medico lo tiene en su agenda o en una ficha. */
    public List<FichaInternaVistaDTO> delPaciente(long pacienteId, long medicoId) {
        boolean suyo = citas.medicoTienePacienteEnAgenda(medicoId, pacienteId)
                || fichas.medicoTieneFichaConPaciente(medicoId, pacienteId);
        if (!suyo) {
            throw new DominioException("PACIENTE_FUERA_DE_AGENDA",
                    "Solo puede ver las fichas de pacientes de su agenda");
        }
        boolean gravedad = puedeVerGravedad(medicoId);
        return fichas.delPaciente(pacienteId).stream()
                .sorted(Comparator.comparing(FichaInternaDTO::creadaEn).reversed())
                .map(f -> vista(f, gravedad)).toList();
    }

    /** Urgencia sin cita: titular y beneficiarios de ese N° de afiliado. */
    public List<PacienteParaFichaDTO> buscarPacientes(String numeroAfiliado, long medicoId) {
        if (!medicoActivo(medicoId).especialidad().puedeDerivar()) {
            throw new DominioException("MEDICO_NO_DERIVA", "Solo Medico General y Pediatria pueden derivar");
        }
        if (numeroAfiliado == null || numeroAfiliado.isBlank()) {
            return List.of();
        }
        LocalDate hoy = reloj.ahora().toLocalDate();
        return pacientes.deLaAfiliacion(numeroAfiliado.trim()).stream()
                .map(p -> new PacienteParaFichaDTO(p.pacienteId(), p.nombreCompleto(), p.numeroAfiliado(),
                        ReglasEdad.edadEn(p.fechaNacimiento(), hoy), p.titular()))
                .toList();
    }

    /** CU-23: fichas del especialista ese dia (sin la gravedad), ordenadas por hora. */
    public List<FichaInternaVistaDTO> agendaEspecialista(long medicoId, LocalDate fechaONull) {
        MedicoDTO medico = medicoActivo(medicoId);
        if (!medico.especialidad().esInterna()) {
            throw new DominioException("ACCESO_DENEGADO", "Solo un especialista tiene agenda de fichas internas");
        }
        LocalDate fecha = fechaONull != null ? fechaONull : reloj.ahora().toLocalDate();
        return fichas.delMedicoEnFecha(medicoId, fecha).stream()
                .sorted(Comparator.comparing(FichaInternaDTO::hora))
                .map(f -> vista(f, false)).toList();
    }

    /** CU-23: el especialista marca la ficha ATENDIDA (con observaciones opcionales). */
    public FichaInternaVistaDTO marcarAtendida(long fichaId, long medicoId, String observaciones) {
        String texto = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
        if (texto != null && texto.length() > MAX_OBSERVACIONES) {
            throw new DominioException("DATOS_INVALIDOS", "Las observaciones pasan de " + MAX_OBSERVACIONES
                    + " caracteres");
        }
        FichaInternaDTO f = delEspecialista(fichaId, medicoId);
        transaccion.ejecutar(() -> {
            if (!fichas.cambiarEstado(fichaId, EstadoFichaInterna.ASIGNADA, EstadoFichaInterna.ATENDIDA, texto)) {
                throw new DominioException("ESTADO_NO_PERMITE", "Solo se atiende una ficha ASIGNADA");
            }
            cambios.registrar("FICHA", fichaId, "ATENDIDA", "Medico " + medicoId, null);
        });
        return vista(fichas.porId(fichaId).orElse(f), false);
    }

    /** CU-23: "No se presento" (despues de la hora de la ficha). El horario no se reasigna. */
    public FichaInternaVistaDTO noSePresento(long fichaId, long medicoId) {
        FichaInternaDTO f = delEspecialista(fichaId, medicoId);
        if (f.inicio() != null && f.inicio().isAfter(reloj.ahora())) {
            throw new DominioException("TODAVIA_NO_ES_LA_HORA",
                    "«No se presento» se habilita despues de las " + f.hora().format(HORA));
        }
        transaccion.ejecutar(() -> {
            if (!fichas.cambiarEstado(fichaId, EstadoFichaInterna.ASIGNADA, EstadoFichaInterna.CANCELADA,
                    f.observaciones())) {
                throw new DominioException("ESTADO_NO_PERMITE", "Solo se cancela una ficha ASIGNADA");
            }
            cambios.registrar("FICHA", fichaId, "NO_SE_PRESENTO", "Medico " + medicoId, null);
        });
        return vista(fichas.porId(fichaId).orElse(f), false);
    }

    private FichaInternaDTO delEspecialista(long fichaId, long medicoId) {
        FichaInternaDTO f = fichas.porId(fichaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No existe esa ficha interna"));
        if (!Long.valueOf(medicoId).equals(f.medicoId())) {
            throw new DominioException("NO_ES_DE_SU_AGENDA", "Esta ficha interna no esta en su agenda");
        }
        return f;
    }

    private void avisar(FichaInternaDTO f, LocalDateTime ahora) {
        if (f.estado() == EstadoFichaInterna.ASIGNADA) {
            avisos.publicar(NotificacionDTO.deFicha(f.pacienteId(), f.fichaId(), "FICHA_INTERNA_ASIGNADA",
                    "Ficha interna N° " + f.fichaId() + " de " + nombreEspecialidad(f.especialidad()) + ": "
                            + f.fecha().format(FECHA) + " a las " + f.hora().format(HORA) + " con " + f.nombreMedico()
                            + lugar(f) + ".", ahora));
        } else if (f.estado() == EstadoFichaInterna.EN_ESPERA) {
            avisos.publicar(NotificacionDTO.deFicha(f.pacienteId(), f.fichaId(), "FICHA_INTERNA_EN_ESPERA",
                    "Ficha interna N° " + f.fichaId() + " de " + nombreEspecialidad(f.especialidad())
                            + ": en espera. Le avisaremos cuando tenga fecha y hora.", ahora));
        }
    }

    private void avisarDesplazada(FichaInternaDTO f, LocalDateTime ahora) {
        String detalle = f.estado() == EstadoFichaInterna.ASIGNADA
                ? "nuevo horario " + f.fecha().format(FECHA) + " a las " + f.hora().format(HORA) + " con "
                        + f.nombreMedico() + lugar(f) + "."
                : "quedo en espera; le avisaremos cuando tenga nueva fecha.";
        avisos.publicar(NotificacionDTO.deFicha(f.pacienteId(), f.fichaId(), "FICHA_INTERNA_REPROGRAMADA",
                "Su ficha interna N° " + f.fichaId() + " de " + nombreEspecialidad(f.especialidad())
                        + " cambio por un caso de mayor prioridad: " + detalle + " Pida que le reimpriman el ticket.",
                ahora));
    }

    private static String lugar(FichaInternaDTO f) {
        return f.nombreConsultorio() == null ? "" : " (" + f.nombreConsultorio() + ")";
    }

    private boolean puedeVerGravedad(long medicoId) {
        return medicos.porId(medicoId).map(m -> m.especialidad().puedeDerivar()).orElse(false);
    }

    private MedicoDTO medicoActivo(long medicoId) {
        MedicoDTO m = medicos.porId(medicoId)
                .orElseThrow(() -> new DominioException("ACCESO_DENEGADO", "No existe un medico con ese id"));
        if (!m.activo()) {
            throw new DominioException("ACCESO_DENEGADO", "El medico esta dado de baja");
        }
        return m;
    }

    /** La ficha como la ve la app; sin la gravedad si {@code conGravedad} es false. */
    FichaInternaVistaDTO vista(FichaInternaDTO f, boolean conGravedad) {
        LocalDate hoy = reloj.ahora().toLocalDate();
        return new FichaInternaVistaDTO(f.fichaId(), f.citaOrigenId(), f.pacienteId(), f.nombrePaciente(),
                f.numeroAfiliado(), ReglasEdad.edadEn(f.fechaNacimiento(), hoy), f.especialidad(),
                f.pediatrica() ? "PEDIATRIA" : "GENERAL", f.urgencia(), conGravedad ? f.gravedad() : null, f.motivo(),
                f.estado(), f.fecha(), f.hora() == null ? null : f.hora().format(HORA), f.duracionMin(), f.nombreMedico(),
                f.nombreConsultorio(), f.nombreDerivante(), f.especialidadDerivante(), f.creadaEn(), f.observaciones());
    }

    private static String nombre(MedicoDTO m) {
        return (m.nombres() + " " + m.apellidos()).trim();
    }

    private static String nombreEspecialidad(TipoEspecialidad e) {
        String texto = e.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
