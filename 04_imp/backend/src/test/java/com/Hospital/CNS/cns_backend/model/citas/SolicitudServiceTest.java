package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;

/**
 * CU-04 (T100-06) con Fakes. Hoy: miercoles 18/11/2026 07:00. La ventana de 15 dias habiles llega hasta el
 * miercoles 09/12/2026 (la misma que usa AsignacionService).
 */
class SolicitudServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 7, 0);
    private static final LocalDate HOY = AHORA.toLocalDate();
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 11, 22);
    private static final LocalDate LIMITE = LocalDate.of(2026, 12, 9);
    private static final long PACIENTE = 100;
    private static final long OTRO_PACIENTE = 101;
    private static final TipoEspecialidad FAMILIAR = TipoEspecialidad.CONSULTORIO_FAMILIAR;

    private final FakeSolicitudRepository solicitudes = new FakeSolicitudRepository();
    private final FakeCitaRepository citas = new FakeCitaRepository();
    private final FakeDiaNoHabilRepository diasNoHabiles = new FakeDiaNoHabilRepository();
    private final SolicitudService service = new SolicitudService(solicitudes, citas,
            new CalendarioHabil(diasNoHabiles), new RelojFijo(AHORA));

    // ---- Camino principal ----

    @Test
    @DisplayName("CP-12: una solicitud valida (especialidad y dia habil) queda PENDIENTE en la cola con prioridad 1 "
            + "y numero de llegada creciente")
    void solicitudValida_quedaPendienteConPrioridad1YOrdenCreciente() {
        SolicitudDTO primera = service.solicitar(PACIENTE, FAMILIAR, LUNES);
        SolicitudDTO segunda = service.solicitar(OTRO_PACIENTE, TipoEspecialidad.PEDIATRIA, LUNES);

        assertEquals(EstadoSolicitud.PENDIENTE, primera.estado());
        assertEquals(1, primera.prioridad());
        assertEquals(LUNES, primera.diaSolicitado());
        assertEquals(FAMILIAR, primera.especialidad());
        assertNull(primera.citaId(), "el paciente no elige medico, hora ni consultorio: la cita la asigna CU-05");
        assertTrue(primera.ordenLlegada() > 0);
        assertTrue(segunda.ordenLlegada() > primera.ordenLlegada(), "el orden de llegada es creciente");
        assertEquals(2, solicitudes.solicitudes.size());
        assertTrue(citas.citas.isEmpty(), "solicitar no crea citas");
    }

    @Test
    void ultimoDiaDeLaVentana_seAcepta() {
        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitar(PACIENTE, FAMILIAR, LIMITE).estado());
    }

    @Test
    void hoy_seAcepta() {
        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitar(PACIENTE, FAMILIAR, HOY).estado());
    }

    // ---- Dia no habil y fuera de rango ----

    @Test
    @DisplayName("CP-13: solicitar una cita para un domingo es rechazado con DIA_NO_HABIL")
    void domingo_diaNoHabil() {
        assertCodigo("DIA_NO_HABIL", () -> service.solicitar(PACIENTE, FAMILIAR, DOMINGO));
        assertTrue(solicitudes.solicitudes.isEmpty());
    }

    @Test
    void feriado_diaNoHabil() {
        diasNoHabiles.store(new DiaNoHabilDTO(1, LUNES, "Feriado", true));

        assertCodigo("DIA_NO_HABIL", () -> service.solicitar(PACIENTE, FAMILIAR, LUNES));
    }

    @Test
    @DisplayName("CP-14: solicitar para un dia habil mas alla de los 15 dias habiles es rechazado con FUERA_DE_RANGO")
    void masAllaDe15DiasHabiles_fueraDeRango() {
        LocalDate jueves = LIMITE.plusDays(1);

        assertCodigo("FUERA_DE_RANGO", () -> service.solicitar(PACIENTE, FAMILIAR, jueves));
        assertTrue(solicitudes.solicitudes.isEmpty());
    }

    @Test
    void diaPasado_fueraDeRango() {
        assertCodigo("FUERA_DE_RANGO", () -> service.solicitar(PACIENTE, FAMILIAR, HOY.minusDays(1)));
    }

    // ---- El paciente ya tiene cita ese dia ----

    @Test
    @DisplayName("CP-15: un paciente que ya tiene cita ese dia en otra especialidad no puede solicitar otra: "
            + "YA_TIENE_CITA_ESE_DIA")
    void citaEnOtraEspecialidadEseDia_yaTieneCita() {
        cita(PACIENTE, TipoEspecialidad.PEDIATRIA, LUNES, EstadoCita.PROGRAMADA);

        assertCodigo("YA_TIENE_CITA_ESE_DIA", () -> service.solicitar(PACIENTE, FAMILIAR, LUNES));
        assertTrue(solicitudes.solicitudes.isEmpty());
    }

    @Test
    void solicitudActivaEseDia_yaTieneCita() {
        service.solicitar(PACIENTE, FAMILIAR, LUNES);

        assertCodigo("YA_TIENE_CITA_ESE_DIA",
                () -> service.solicitar(PACIENTE, TipoEspecialidad.ODONTOLOGIA, LUNES));
        assertEquals(1, solicitudes.solicitudes.size());
    }

    @Test
    void citaCanceladaEseDia_noImpide() {
        cita(PACIENTE, FAMILIAR, LUNES, EstadoCita.CANCELADA);

        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitar(PACIENTE, FAMILIAR, LUNES).estado());
    }

    // ---- Reprogramacion ----

    @Test
    @DisplayName("CP-16: una solicitud de reprogramacion entra a la cola con prioridad 2")
    void reprogramacion_prioridad2() {
        SolicitudDTO s = service.solicitarReprogramada(PACIENTE, FAMILIAR, LUNES);

        assertEquals(2, s.prioridad());
        assertEquals(EstadoSolicitud.PENDIENTE, s.estado());
    }

    // ---- Datos invalidos ----

    @Test
    void especialidadInterna_especialidadInvalida() {
        assertCodigo("ESPECIALIDAD_INVALIDA",
                () -> service.solicitar(PACIENTE, TipoEspecialidad.CARDIOLOGIA, LUNES));
    }

    @Test
    void sinEspecialidadOSinDia_datosInvalidos() {
        assertCodigo("DATOS_INVALIDOS", () -> service.solicitar(PACIENTE, null, LUNES));
        assertCodigo("DATOS_INVALIDOS", () -> service.solicitar(PACIENTE, FAMILIAR, null));
    }

    // ---- Consultas (Mis citas) ----

    @Test
    void listar_soloLasDelPaciente_masRecienteprimero() {
        SolicitudDTO lunes = service.solicitar(PACIENTE, FAMILIAR, LUNES);
        SolicitudDTO martes = service.solicitar(PACIENTE, FAMILIAR, LUNES.plusDays(1));
        service.solicitar(OTRO_PACIENTE, FAMILIAR, LUNES);

        List<Long> ids = service.listarPorPaciente(PACIENTE).stream().map(SolicitudDTO::solicitudId).toList();

        assertEquals(List.of(martes.solicitudId(), lunes.solicitudId()), ids);
    }

    @Test
    void consultar_deOtroPaciente_noEsDelPaciente() {
        SolicitudDTO s = service.solicitar(PACIENTE, FAMILIAR, LUNES);

        assertCodigo("NO_ES_DEL_PACIENTE", () -> service.consultar(OTRO_PACIENTE, s.solicitudId()));
        assertCodigo("NO_ENCONTRADA", () -> service.consultar(PACIENTE, 999));
    }

    private void cita(long pacienteId, TipoEspecialidad especialidad, LocalDate dia, EstadoCita estado) {
        citas.store(new CitaDTO(citas.nextIdentity(), pacienteId, "", "", 50, "", especialidad, 9, dia,
                LocalTime.of(15, 0), 1, estado));
    }

    private static void assertCodigo(String codigo, Executable accion) {
        assertEquals(codigo, assertThrows(DominioException.class, accion).getCodigo());
    }
}
