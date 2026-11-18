package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** CU-07 con Fakes: un test por cada flujo del diagrama CU-07_RegistrarCitaConfirmada ([2a], [3a]). */
class CitaServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 23);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 10, 0);
    private static final long PACIENTE = 100;
    private static final long RAMOS = 1;

    private final FakeCitaRepository citas = new FakeCitaRepository();
    private final FakeSolicitudRepository solicitudes = new FakeSolicitudRepository();
    private final FakeCambioCitaRepository cambios = new FakeCambioCitaRepository();
    private final FakeNotificacionPublicador avisos = new FakeNotificacionPublicador();
    private final FakeAgendaEventos eventos = new FakeAgendaEventos();
    private final CitaService service = new CitaService(citas, solicitudes, cambios, avisos, eventos,
            new RelojFijo(AHORA));

    @BeforeEach
    void preparar() {
        solicitudes.store(solicitud(10, PACIENTE, EstadoSolicitud.PENDIENTE));
    }

    @Test
    void citaService_marcaSolicitudAsignada() {
        CitaDTO cita = service.registrar(asignacion(10, PACIENTE, LocalTime.of(8, 0)));

        SolicitudDTO s = solicitudes.porId(10).orElseThrow();
        assertEquals(EstadoSolicitud.ASIGNADA, s.estado());
        assertEquals(cita.citaId(), s.citaId());
    }

    @Test
    void registrar_creaCitaProgramadaConTodosLosDatos() {
        CitaDTO cita = service.registrar(asignacion(10, PACIENTE, LocalTime.of(8, 0)));

        assertEquals(EstadoCita.PROGRAMADA, cita.estado());
        assertEquals(PACIENTE, cita.pacienteId());
        assertEquals(RAMOS, cita.medicoId());
        assertEquals(TipoEspecialidad.CONSULTORIO_FAMILIAR, cita.especialidad());
        assertEquals(3, cita.consultorio());
        assertEquals(DIA, cita.fecha());
        assertEquals(LocalTime.of(8, 0), cita.hora());
        assertEquals(1, cita.numeroFicha());
        assertEquals(1, citas.citas.size());
    }

    @Test
    void registrar_numeroDeFichaCorrelativoPorMedicoYDia_sinRepetirCanceladas() {
        solicitudes.store(solicitud(11, 101, EstadoSolicitud.PENDIENTE));
        solicitudes.store(solicitud(12, 102, EstadoSolicitud.EN_ESPERA));
        CitaDTO primera = service.registrar(asignacion(10, PACIENTE, LocalTime.of(8, 0)));
        citas.cambiarEstado(primera.citaId(), EstadoCita.PROGRAMADA, EstadoCita.CANCELADA);

        assertEquals(2, service.registrar(asignacion(11, 101, LocalTime.of(8, 30))).numeroFicha());
        assertEquals(3, service.registrar(asignacion(12, 102, LocalTime.of(9, 0))).numeroFicha());
    }

    @Test
    void registrar_escribeCambioYPublicaNotificacionYEvento() {
        CitaDTO cita = service.registrar(asignacion(10, PACIENTE, LocalTime.of(8, 0)));

        CambioCitaDTO cambio = cambios.deCita(cita.citaId()).get(0);
        assertEquals(CitaService.ACCION_CREADA, cambio.accion());
        assertEquals(CitaService.USUARIO_SISTEMA, cambio.usuario());
        assertEquals(AHORA, cambio.fechaHora());

        NotificacionDTO aviso = avisos.publicadas.get(0);
        assertEquals(PACIENTE, aviso.pacienteId());
        assertEquals(cita.citaId(), aviso.citaId());
        assertEquals(CitaService.AVISO_CITA_ASIGNADA, aviso.tipo());
        assertTrue(aviso.mensaje().contains("ficha 1"));
        assertEquals(1, eventos.eventos.size());
    }

    // ---- [2a] La asignacion dejo de ser valida ----

    @Test
    void registrar_uniqueRechaza_asignacionInvalidaSinGuardarNada() {
        solicitudes.store(solicitud(11, 101, EstadoSolicitud.PENDIENTE));
        service.registrar(asignacion(10, PACIENTE, LocalTime.of(8, 0)));
        avisos.publicadas.clear();

        DominioException e = assertThrows(DominioException.class,
                () -> service.registrar(asignacion(11, 101, LocalTime.of(8, 0))));

        assertEquals("ASIGNACION_INVALIDA", e.getCodigo());
        assertEquals(1, citas.citas.size());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitudes.porId(11).orElseThrow().estado());
        assertTrue(avisos.publicadas.isEmpty());
        assertEquals(1, cambios.cambios.size());
    }

    @Test
    void registrar_solicitudYaAsignada_asignacionInvalida() {
        service.registrar(asignacion(10, PACIENTE, LocalTime.of(8, 0)));

        DominioException e = assertThrows(DominioException.class,
                () -> service.registrar(asignacion(10, PACIENTE, LocalTime.of(9, 0))));

        assertEquals("ASIGNACION_INVALIDA", e.getCodigo());
        assertEquals(1, citas.citas.size());
    }

    @Test
    void registrar_asignacionDeOtroPaciente_asignacionInvalida() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.registrar(asignacion(10, 999, LocalTime.of(8, 0))));

        assertEquals("ASIGNACION_INVALIDA", e.getCodigo());
        assertTrue(citas.citas.isEmpty());
    }

    // ---- [3a] Faltan datos ----

    @Test
    void registrar_datosIncompletos_noConfirma() {
        Asignacion sinHora = new Asignacion(10, PACIENTE, RAMOS, TipoEspecialidad.CONSULTORIO_FAMILIAR, 3, DIA, null, 30);

        DominioException e = assertThrows(DominioException.class, () -> service.registrar(sinHora));

        assertEquals("ASIGNACION_INCOMPLETA", e.getCodigo());
        assertTrue(citas.citas.isEmpty());
        assertNull(solicitudes.porId(10).orElseThrow().citaId());
    }

    private static Asignacion asignacion(long solicitudId, long pacienteId, LocalTime hora) {
        return new Asignacion(solicitudId, pacienteId, RAMOS, TipoEspecialidad.CONSULTORIO_FAMILIAR, 3, DIA, hora, 30);
    }

    private static SolicitudDTO solicitud(long id, long pacienteId, EstadoSolicitud estado) {
        return new SolicitudDTO(id, pacienteId, TipoEspecialidad.CONSULTORIO_FAMILIAR, DIA, 1, id, estado, null);
    }
}
