package com.Hospital.CNS.cns_backend.model.citas;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/**
 * CU-07 (T100-03): registra la cita confirmada.
 *
 * <p>En UNA transaccion: crea la cita PROGRAMADA con su numero de ficha, marca la solicitud ASIGNADA y escribe el
 * CambioCita. Despues del commit: la notificacion al paciente (CU-16) y el aviso por WebSocket
 * ({@link AgendaEventos}). Si la base rechaza la cita (UNIQUE medico, fecha, hora) o la solicitud ya no esta activa
 * [2a] lanza {@code ASIGNACION_INVALIDA} y no guarda nada; si faltan datos [3a], {@code ASIGNACION_INCOMPLETA}.</p>
 */
public class CitaService {

    public static final String USUARIO_SISTEMA = "SISTEMA";
    public static final String ACCION_CREADA = "CREADA";
    public static final String AVISO_CITA_ASIGNADA = "CITA_ASIGNADA";

    private static final Logger LOG = LoggerFactory.getLogger(CitaService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaRepository citas;
    private final SolicitudRepository solicitudes;
    private final CambioCitaRepository cambios;
    private final NotificacionPublicador avisos;
    private final AgendaEventos eventos;
    private final Reloj reloj;
    private final Transaccion transaccion;

    /** Contrato de T100-03: sin transaccion (pruebas con Fakes). */
    public CitaService(CitaRepository c, SolicitudRepository s, CambioCitaRepository cambios,
                       NotificacionPublicador notif, AgendaEventos eventos, Reloj reloj) {
        this(c, s, cambios, notif, eventos, reloj, Transaccion.DIRECTA);
    }

    public CitaService(CitaRepository c, SolicitudRepository s, CambioCitaRepository cambios,
                       NotificacionPublicador notif, AgendaEventos eventos, Reloj reloj, Transaccion transaccion) {
        this.citas = Objects.requireNonNull(c, "citas");
        this.solicitudes = Objects.requireNonNull(s, "solicitudes");
        this.cambios = Objects.requireNonNull(cambios, "cambios");
        this.avisos = Objects.requireNonNull(notif, "notif");
        this.eventos = Objects.requireNonNull(eventos, "eventos");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
        this.transaccion = Objects.requireNonNull(transaccion, "transaccion");
    }

    public CitaDTO registrar(Asignacion a) {
        CitaDTO cita = transaccion.ejecutar(() -> guardar(a));
        publicar(cita);
        return cita;
    }

    /**
     * Los pasos que van dentro de la transaccion (sin avisos). AsignacionService lo llama dentro de SU transaccion,
     * junto con la validacion de CU-06, y publica con {@link #publicar} despues del commit.
     */
    CitaDTO guardar(Asignacion a) {
        validarDatos(a);
        SolicitudDTO s = solicitudes.porId(a.solicitudId())
                .filter(sol -> sol.estado() == EstadoSolicitud.PENDIENTE || sol.estado() == EstadoSolicitud.EN_ESPERA)
                .orElseThrow(() -> invalida("La solicitud " + a.solicitudId() + " ya no esta pendiente"));
        if (s.pacienteId() != a.pacienteId() || s.especialidad() != a.especialidad()) {
            throw invalida("La asignacion no corresponde a la solicitud " + a.solicitudId());
        }

        long citaId = citas.nextIdentity();
        CitaDTO nueva = new CitaDTO(citaId, a.pacienteId(), "", "", a.medicoId(), "", a.especialidad(),
                a.consultorioId(), a.fecha(), a.hora(), siguienteFicha(a), EstadoCita.PROGRAMADA);
        if (!citas.store(nueva)) {
            throw invalida("El medico ya tiene una cita el " + a.fecha().format(FECHA) + " a las "
                    + a.hora().format(HORA));
        }
        solicitudes.store(new SolicitudDTO(s.solicitudId(), s.pacienteId(), s.especialidad(), s.diaSolicitado(),
                s.prioridad(), s.ordenLlegada(), EstadoSolicitud.ASIGNADA, citaId));
        cambios.registrar(new CambioCitaDTO(0, citaId, ACCION_CREADA, USUARIO_SISTEMA, reloj.ahora(),
                "Asignacion automatica de la solicitud " + s.solicitudId() + " (CU-05/CU-07)"));
        return citas.porId(citaId).orElse(nueva);
    }

    /** Despues del commit. Un aviso que falla no deshace la cita: queda en el log. */
    void publicar(CitaDTO cita) {
        try {
            avisos.publicar(new NotificacionDTO(0, cita.pacienteId(), cita.citaId(), null, null, AVISO_CITA_ASIGNADA,
                    mensaje(cita), reloj.ahora()));
        } catch (RuntimeException e) {
            LOG.error("No se pudo notificar la cita {} al paciente {}", cita.citaId(), cita.pacienteId(), e);
        }
        try {
            eventos.citaCambio(cita);
        } catch (RuntimeException e) {
            LOG.error("No se pudo avisar por WebSocket la cita {}", cita.citaId(), e);
        }
    }

    /** Correlativo por medico y dia (cuenta tambien las CANCELADAS para no repetir numeros). PENDIENTE PV-14. */
    private int siguienteFicha(Asignacion a) {
        // PENDIENTE PV-14: numero de ficha = correlativo por medico y dia.
        return citas.delMedicoEnFecha(a.medicoId(), a.fecha()).stream()
                .mapToInt(CitaDTO::numeroFicha)
                .max()
                .orElse(0) + 1;
    }

    private static void validarDatos(Asignacion a) {
        if (a == null || a.solicitudId() <= 0 || a.pacienteId() <= 0 || a.medicoId() <= 0 || a.consultorioId() <= 0
                || a.especialidad() == null || a.fecha() == null || a.hora() == null || a.duracionMin() <= 0) {
            throw new DominioException("ASIGNACION_INCOMPLETA", "Faltan datos para registrar la cita");
        }
    }

    private static String mensaje(CitaDTO c) {
        String medico = c.nombreMedico() == null || c.nombreMedico().isBlank() ? "" : " con " + c.nombreMedico();
        return "Cita asignada: ficha " + c.numeroFicha() + medico + " el " + c.fecha().format(FECHA) + " a las "
                + c.hora().format(HORA) + ", consultorio " + c.consultorio() + ".";
    }

    private static DominioException invalida(String mensaje) {
        return new DominioException("ASIGNACION_INVALIDA", mensaje);
    }
}
