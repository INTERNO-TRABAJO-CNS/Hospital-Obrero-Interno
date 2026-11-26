package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.Hospital.CNS.cns_backend.model.compartido.CancelacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCancelacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoCancelacion;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.operaciones.CancelacionService;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;

/**
 * CU-10 con Fakes. Hoy: miercoles 18/11/2026 07:00; la ventana de 15 dias habiles llega al miercoles 09/12/2026.
 * Reprogramar = cancelar + solicitud prioridad 2 para el siguiente dia habil despues de la cita.
 */
class ReprogramacionServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 7, 0);
    private static final LocalDate JUEVES = LocalDate.of(2026, 11, 19);
    private static final LocalDate VIERNES = LocalDate.of(2026, 11, 20);
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);
    private static final LocalDate LIMITE = LocalDate.of(2026, 12, 9);
    private static final long PACIENTE = 100;
    private static final long OTRO_PACIENTE = 101;

    private FakeCitaRepository citas;
    private FakeSolicitudRepository solicitudesRepo;
    private RelojFijo reloj;
    private CalendarioHabil calendario;
    private SolicitudService solicitudes;
    private ReprogramacionService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        solicitudesRepo = new FakeSolicitudRepository();
        reloj = new RelojFijo(AHORA);
        calendario = new CalendarioHabil(new FakeDiaNoHabilRepository());
        solicitudes = new SolicitudService(solicitudesRepo, citas, calendario, reloj);
        CancelacionCita cancelacion = new CancelacionService(citas, new FakeCambioCitaRepository(),
                new FakeNotificacionPublicador(), new FakeAgendaEventos(), reloj);
        service = new ReprogramacionService(citas, cancelacion, solicitudes, calendario, reloj);
    }

    private void cita(long citaId, long pacienteId, LocalDate dia, EstadoCita estado) {
        citas.store(com.Hospital.CNS.cns_backend.model.compartido.Citas.cita(citaId, pacienteId, citaId,
                TipoEspecialidad.ODONTOLOGIA, dia, LocalTime.of(9, 0), 1, estado));
    }

    @Test
    void reprogramar_cancelaYCreaSolicitudPrioridad2() {
        cita(1, PACIENTE, JUEVES, EstadoCita.PROGRAMADA);

        SolicitudDTO nueva = service.reprogramar(1, PACIENTE);

        assertEquals(EstadoCita.CANCELADA, citas.porId(1).orElseThrow().estado());
        assertEquals(1, citas.citas.size(), "la cita no se borra");
        assertEquals(2, nueva.prioridad());
        assertEquals(VIERNES, nueva.diaSolicitado());
        assertEquals(TipoEspecialidad.ODONTOLOGIA, nueva.especialidad());
        assertEquals(EstadoSolicitud.PENDIENTE, nueva.estado());
        assertEquals(PACIENTE, nueva.pacienteId());
    }

    @Test
    void reprogramar_citaDelViernes_pasaAlLunes() {
        cita(1, PACIENTE, VIERNES, EstadoCita.PROGRAMADA);

        assertEquals(LUNES, service.reprogramar(1, PACIENTE).diaSolicitado());
    }

    @Test
    void reprogramar_saltaElDiaEnQueYaTieneOtraCita() {
        cita(1, PACIENTE, JUEVES, EstadoCita.PROGRAMADA);
        cita(2, PACIENTE, VIERNES, EstadoCita.PROGRAMADA);

        assertEquals(LUNES, service.reprogramar(1, PACIENTE).diaSolicitado());
    }

    @Test
    void reprogramar_citaAtendida_rechaza() {
        cita(1, PACIENTE, JUEVES, EstadoCita.ATENDIDA);

        assertCodigo("ESTADO_NO_PERMITE", () -> service.reprogramar(1, PACIENTE));
        assertTrue(solicitudesRepo.porPaciente(PACIENTE).isEmpty());
    }

    @Test
    void reprogramar_citaConfirmada_rechaza() {
        cita(1, PACIENTE, JUEVES, EstadoCita.ASISTENCIA_CONFIRMADA);

        assertCodigo("ESTADO_NO_PERMITE", () -> service.reprogramar(1, PACIENTE));
        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, citas.porId(1).orElseThrow().estado());
    }

    @Test
    void reprogramar_citaAjena_rechaza() {
        cita(1, OTRO_PACIENTE, JUEVES, EstadoCita.PROGRAMADA);

        assertCodigo("NO_ES_DEL_PACIENTE", () -> service.reprogramar(1, PACIENTE));
        assertEquals(EstadoCita.PROGRAMADA, citas.porId(1).orElseThrow().estado());
    }

    @Test
    void reprogramar_citaInexistente_noEncontrada() {
        assertCodigo("NO_ENCONTRADA", () -> service.reprogramar(9, PACIENTE));
    }

    @Test
    void reprogramar_sinDiaEnLaVentana_conservaLaCita() {
        cita(1, PACIENTE, LIMITE, EstadoCita.PROGRAMADA);

        assertCodigo("SIN_DIA_DISPONIBLE", () -> service.reprogramar(1, PACIENTE));
        assertEquals(EstadoCita.PROGRAMADA, citas.porId(1).orElseThrow().estado());
        assertTrue(solicitudesRepo.porPaciente(PACIENTE).isEmpty());
    }

    @Test
    void reprogramar_siLaCancelacionFalla_noCreaSolicitud() {
        cita(1, PACIENTE, JUEVES, EstadoCita.PROGRAMADA);
        FakeCancelacionCita cancelacion = new FakeCancelacionCita();
        cancelacion.resultado = ResultadoCancelacion.ESTADO_NO_PERMITE; // otro proceso la cancelo antes
        ReprogramacionService conCarrera =
                new ReprogramacionService(citas, cancelacion, solicitudes, calendario, reloj);

        assertCodigo("ESTADO_NO_PERMITE", () -> conCarrera.reprogramar(1, PACIENTE));
        assertTrue(solicitudesRepo.porPaciente(PACIENTE).isEmpty());
    }

    private static void assertCodigo(String codigo, Executable accion) {
        DominioException e = assertThrows(DominioException.class, accion);
        assertEquals(codigo, e.getCodigo());
    }
}
