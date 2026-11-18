package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * CU-05 (T100-03): asigna automaticamente la siguiente solicitud de la cola.
 *
 * <ul>
 *   <li>Cola: prioridad 1 antes que 2; dentro de cada una, orden de llegada ({@code siguientesParaProcesar}).</li>
 *   <li>Cada llamada prueba SOLO el {@code diaSolicitado}. Medicos activos de la especialidad, primero el de menos
 *       fichas ese dia (empate: menor medicoId, {@code PENDIENTE PV-13}); por cada uno, sus franjas consecutivas de
 *       {@code duracionCitaMin} dentro de su horario; cada franja se valida con CU-06.</li>
 *   <li>[2a] Sin cupo ese dia (o el paciente ya tiene cita ese dia): EN_ESPERA con el siguiente dia habil.
 *       [2b] Si ese dia pasa de hoy + 15 dias habiles (la ventana de CU-04): SIN_CUPO.</li>
 *   <li>[3a] La franja dejo de estar libre o la base rechaza la cita (ASIGNACION_INVALIDA): sigue con la siguiente.
 *       [3b] Otra solicitud simultanea tiene la agenda (BLOQUEADA_POR_OTRO) y no hubo otra opcion: queda EN_ESPERA en el
 *       MISMO dia y se reintenta en la siguiente vuelta.</li>
 * </ul>
 *
 * <p>Todo ocurre en UNA transaccion: tomar la solicitud (en PostgreSQL con {@code FOR UPDATE SKIP LOCKED}, asi dos
 * procesos no toman la misma), validar (bloquea la agenda del medico) y registrar (CU-07). La notificacion y el
 * WebSocket se publican despues del commit.</p>
 */
public class AsignacionService {

    public static final int DIAS_HABILES_MAXIMOS = 15;

    private static final Logger LOG = LoggerFactory.getLogger(AsignacionService.class);

    private final SolicitudRepository solicitudes;
    private final CitaRepository citas;
    private final DisponibilidadService disponibilidad;
    private final MedicoRepository medicos;
    private final HorarioRepository horarios;
    private final CalendarioHabil calendario;
    private final CitaService registro;
    private final Reloj reloj;
    private final Transaccion transaccion;

    /** Resultado de buscar en un dia: la cita creada, o si alguna franja estaba tomada por otro proceso. */
    private record Busqueda(CitaDTO cita, boolean hubOtroProceso) {
    }

    /** Lo que se decide dentro de la transaccion; la cita se publica despues del commit. */
    private record Paso(ResultadoAsignacion resultado, CitaDTO cita) {
    }

    /** Contrato de T100-03: sin transaccion (pruebas con Fakes). */
    public AsignacionService(SolicitudRepository s, CitaRepository c, DisponibilidadService d,
                             MedicoRepository m, HorarioRepository h, CalendarioHabil cal, CitaService registro,
                             Reloj reloj) {
        this(s, c, d, m, h, cal, registro, reloj, Transaccion.DIRECTA);
    }

    public AsignacionService(SolicitudRepository s, CitaRepository c, DisponibilidadService d,
                             MedicoRepository m, HorarioRepository h, CalendarioHabil cal, CitaService registro,
                             Reloj reloj, Transaccion transaccion) {
        this.solicitudes = Objects.requireNonNull(s, "solicitudes");
        this.citas = Objects.requireNonNull(c, "citas");
        this.disponibilidad = Objects.requireNonNull(d, "disponibilidad");
        this.medicos = Objects.requireNonNull(m, "medicos");
        this.horarios = Objects.requireNonNull(h, "horarios");
        this.calendario = Objects.requireNonNull(cal, "calendario");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
        this.transaccion = Objects.requireNonNull(transaccion, "transaccion");
    }

    public ResultadoAsignacion procesarSiguiente() {
        LocalDateTime ahora = reloj.ahora();
        LocalDate limite = calendario.sumarDiasHabiles(ahora.toLocalDate(), DIAS_HABILES_MAXIMOS);
        Paso paso = transaccion.ejecutar(() -> {
            List<SolicitudDTO> cola = solicitudes.siguientesParaProcesar(limite, 1);
            if (cola.isEmpty()) {
                return new Paso(ResultadoAsignacion.NADA_PENDIENTE, null);
            }
            return procesar(cola.get(0), ahora, limite);
        });
        if (paso.cita() != null) {
            registro.publicar(paso.cita());
        }
        return paso.resultado();
    }

    /** Procesa hasta {@code max} solicitudes (o hasta que no quede ninguna). Devuelve cuantas proceso. */
    public int procesarCola(int max) {
        int procesadas = 0;
        while (procesadas < max && procesarSiguiente() != ResultadoAsignacion.NADA_PENDIENTE) {
            procesadas++;
        }
        return procesadas;
    }

    private Paso procesar(SolicitudDTO s, LocalDateTime ahora, LocalDate limite) {
        LocalDate hoy = ahora.toLocalDate();
        LocalDate dia = s.diaSolicitado();
        boolean diaUtil = !dia.isBefore(hoy) && calendario.esHabil(dia)
                && !citas.pacienteTieneCitaEnFecha(s.pacienteId(), dia);
        if (diaUtil) {
            Busqueda b = buscarEnDia(s, dia, ahora);
            if (b.cita() != null) {
                LOG.info("Solicitud {} asignada: cita {}", s.solicitudId(), b.cita().citaId());
                return new Paso(ResultadoAsignacion.ASIGNADA, b.cita());
            }
            if (b.hubOtroProceso()) {
                guardarEstado(s, EstadoSolicitud.EN_ESPERA, dia);
                return new Paso(ResultadoAsignacion.EN_ESPERA, null);
            }
        }
        LocalDate siguiente = dia.isBefore(hoy) ? primerHabilDesde(hoy) : calendario.siguienteDiaHabil(dia);
        if (siguiente.isAfter(limite)) {
            LOG.info("Solicitud {} SIN_CUPO: {} dias habiles sin cupo", s.solicitudId(), DIAS_HABILES_MAXIMOS);
            guardarEstado(s, EstadoSolicitud.SIN_CUPO, dia);
            return new Paso(ResultadoAsignacion.SIN_CUPO, null);
        }
        LOG.info("Solicitud {}: NO HAY CUPOS el {}, en espera para el {}", s.solicitudId(), dia, siguiente);
        guardarEstado(s, EstadoSolicitud.EN_ESPERA, siguiente);
        return new Paso(ResultadoAsignacion.EN_ESPERA, null);
    }

    private Busqueda buscarEnDia(SolicitudDTO s, LocalDate dia, LocalDateTime ahora) {
        boolean hubOtroProceso = false;
        for (MedicoDTO medico : medicosPorMenosFichas(s, dia)) {
            for (HorarioDTO horario : horariosDelDia(medico, dia)) {
                for (LocalTime hora : franjas(horario, medico.duracionCitaMin(), dia, ahora)) {
                    Asignacion a = new Asignacion(s.solicitudId(), s.pacienteId(), medico.medicoId(), s.especialidad(),
                            horario.consultorioId(), dia, hora, medico.duracionCitaMin());
                    ResultadoDisponibilidad r = disponibilidad.validar(a.combinacion());
                    if (r == ResultadoDisponibilidad.DISPONIBLE) {
                        Optional<CitaDTO> cita = registrar(a);
                        if (cita.isPresent()) {
                            return new Busqueda(cita.get(), false);
                        }
                        continue; // [3a] la base la rechazo: siguiente franja
                    }
                    if (r == ResultadoDisponibilidad.PACIENTE_YA_TIENE_CITA) {
                        return new Busqueda(null, false); // ese dia ya no sirve para este paciente
                    }
                    if (r == ResultadoDisponibilidad.MEDICO_COMPLETO) {
                        break;
                    }
                    if (r == ResultadoDisponibilidad.BLOQUEADA_POR_OTRO) {
                        hubOtroProceso = true;
                    }
                }
            }
        }
        return new Busqueda(null, hubOtroProceso);
    }

    private Optional<CitaDTO> registrar(Asignacion a) {
        try {
            return Optional.of(registro.guardar(a));
        } catch (DominioException e) {
            if (!"ASIGNACION_INVALIDA".equals(e.getCodigo())) {
                throw e;
            }
            LOG.info("Combinacion descartada para la solicitud {}: {}", a.solicitudId(), e.getMessage());
            return Optional.empty();
        }
    }

    /** Activos de la especialidad; primero el de menos fichas ese dia, empate: menor medicoId. */
    private List<MedicoDTO> medicosPorMenosFichas(SolicitudDTO s, LocalDate dia) {
        List<MedicoDTO> activos = medicos.listar(s.especialidad()).stream().filter(MedicoDTO::activo).toList();
        Map<Long, Integer> fichas = activos.stream()
                .collect(Collectors.toMap(MedicoDTO::medicoId, m -> citas.contarDelMedicoEnFecha(m.medicoId(), dia),
                        (x, y) -> x));
        // PENDIENTE PV-13: criterio de desempate entre medicos con el mismo numero de fichas.
        return activos.stream()
                .sorted(Comparator.comparing((Function<MedicoDTO, Integer>) m -> fichas.get(m.medicoId()))
                        .thenComparingLong(MedicoDTO::medicoId))
                .toList();
    }

    private List<HorarioDTO> horariosDelDia(MedicoDTO medico, LocalDate dia) {
        return horarios.delMedico(medico.medicoId()).stream()
                .filter(h -> h.activo() && h.diaSemana() == dia.getDayOfWeek())
                .sorted(Comparator.comparing(HorarioDTO::horaInicio))
                .toList();
    }

    /** Franjas consecutivas [inicio, inicio + duracion) que terminan dentro del horario; hoy, solo las que no pasaron. */
    private static List<LocalTime> franjas(HorarioDTO h, int duracion, LocalDate dia, LocalDateTime ahora) {
        List<LocalTime> lista = new ArrayList<>();
        if (duracion <= 0) {
            return lista;
        }
        boolean esHoy = dia.equals(ahora.toLocalDate());
        LocalTime inicio = h.horaInicio();
        while (!inicio.plusMinutes(duracion).isAfter(h.horaFin()) && inicio.plusMinutes(duracion).isAfter(inicio)) {
            if (!esHoy || inicio.isAfter(ahora.toLocalTime())) {
                lista.add(inicio);
            }
            inicio = inicio.plusMinutes(duracion);
        }
        return lista;
    }

    private LocalDate primerHabilDesde(LocalDate fecha) {
        return calendario.esHabil(fecha) ? fecha : calendario.siguienteDiaHabil(fecha);
    }

    private void guardarEstado(SolicitudDTO s, EstadoSolicitud estado, LocalDate dia) {
        solicitudes.store(new SolicitudDTO(s.solicitudId(), s.pacienteId(), s.especialidad(), dia, s.prioridad(),
                s.ordenLlegada(), estado, null));
    }
}
