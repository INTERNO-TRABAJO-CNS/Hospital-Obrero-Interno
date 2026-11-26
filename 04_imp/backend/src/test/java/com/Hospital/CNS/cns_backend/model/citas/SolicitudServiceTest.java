package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
 * CU-04 (T100-06) con Fakes: el paciente elige especialidad y dia. Hoy: miercoles 18/11/2026 07:00. La ventana de 15 dias habiles llega hasta el
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

    // ---- Camino principal: el paciente elige especialidad y dia (RF-04, RU-01, RN-01) ----

    @Test
    @DisplayName("CP-12: una solicitud valida (especialidad y dia) queda PENDIENTE con prioridad 1 para ese dia y "
            + "numero de llegada creciente; el paciente no elige medico, hora ni consultorio")
    void solicitudValida_quedaPendienteParaElDiaElegido() {
        SolicitudDTO primera = service.solicitar(PACIENTE, FAMILIAR, LUNES);
        SolicitudDTO segunda = service.solicitar(OTRO_PACIENTE, TipoEspecialidad.PEDIATRIA, HOY);

        assertEquals(EstadoSolicitud.PENDIENTE, primera.estado());
        assertEquals(1, primera.prioridad());
        assertEquals(LUNES, primera.diaSolicitado(), "la busqueda de cupo empieza el dia que eligio el paciente");
        assertEquals(FAMILIAR, primera.especialidad());
        assertNull(primera.citaId(), "el paciente no elige medico, hora ni consultorio: la cita la asigna CU-05");
        assertTrue(primera.ordenLlegada() > 0);
        assertTrue(segunda.ordenLlegada() > primera.ordenLlegada(), "el orden de llegada es creciente");
        assertEquals(2, solicitudes.solicitudes.size());
        assertTrue(citas.citas.isEmpty(), "solicitar no crea citas");
    }

    @Test
    @DisplayName("CP-13: pedir para un domingo o un feriado es DIA_NO_HABIL")
    void diaNoHabil_rechazado() {
        diasNoHabiles.store(new DiaNoHabilDTO(1, LUNES, "Feriado", true));

        assertCodigo("DIA_NO_HABIL", () -> service.solicitar(PACIENTE, FAMILIAR, DOMINGO));
        assertCodigo("DIA_NO_HABIL", () -> service.solicitar(PACIENTE, FAMILIAR, LUNES));
        assertTrue(solicitudes.solicitudes.isEmpty());
    }

    @Test
    @DisplayName("CP-14: pedir mas alla de los 15 dias habiles o un dia pasado es FUERA_DE_RANGO")
    void fueraDeVentana_fueraDeRango() {
        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitar(PACIENTE, FAMILIAR, LIMITE).estado());
        assertCodigo("FUERA_DE_RANGO", () -> service.solicitar(OTRO_PACIENTE, FAMILIAR, LIMITE.plusDays(1)));
        assertCodigo("FUERA_DE_RANGO", () -> service.solicitar(OTRO_PACIENTE, FAMILIAR, HOY.minusDays(1)));
    }

    @Test
    @DisplayName("RN-10: con una cita ese dia (aunque sea de otra especialidad) es YA_TIENE_CITA_ESE_DIA")
    void citaEseDia_yaTieneCita() {
        cita(PACIENTE, TipoEspecialidad.ODONTOLOGIA, LUNES, EstadoCita.PROGRAMADA);

        assertCodigo("YA_TIENE_CITA_ESE_DIA", () -> service.solicitar(PACIENTE, FAMILIAR, LUNES));
    }

    @Test
    void citaCanceladaEseDia_noImpide() {
        cita(PACIENTE, FAMILIAR, LUNES, EstadoCita.CANCELADA);

        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitar(PACIENTE, FAMILIAR, LUNES).estado());
    }

    @Test
    void diasHabilesDisponibles_desdeHoyHastaElLimiteSinFinesDeSemanaNiFeriados() {
        diasNoHabiles.store(new DiaNoHabilDTO(1, LUNES, "Feriado", true));

        List<LocalDate> dias = service.diasHabilesDisponibles();

        assertEquals(HOY, dias.get(0));
        assertEquals(LIMITE.plusDays(1), dias.get(dias.size() - 1),
                "con un feriado en medio, los 15 dias habiles terminan un dia despues");
        assertEquals(16, dias.size(), "hoy + 15 dias habiles");
        assertTrue(dias.stream().noneMatch(d -> d.equals(DOMINGO) || d.equals(LUNES)));
        assertTrue(dias.stream().noneMatch(d -> d.getDayOfWeek().getValue() >= 6), "sin sabados ni domingos");
    }

    // ---- Pediatria solo menores de 10 anios (T100-24 / T100-26) ----

    @Test
    @DisplayName("T100-26: un paciente de 10 anios o mas no puede pedir Pediatria (EDAD_NO_PEDIATRICA)")
    void pediatria_mayorDe10_rechazada() {
        SolicitudService conEdades = conNacimientos(Map.of(PACIENTE, HOY.minusYears(30),
                OTRO_PACIENTE, HOY.minusYears(4)));

        assertCodigo("EDAD_NO_PEDIATRICA",
                () -> conEdades.solicitar(PACIENTE, TipoEspecialidad.PEDIATRIA, LUNES));
        assertEquals(EstadoSolicitud.PENDIENTE,
                conEdades.solicitar(OTRO_PACIENTE, TipoEspecialidad.PEDIATRIA, LUNES).estado());
        assertEquals(EstadoSolicitud.PENDIENTE,
                conEdades.solicitar(PACIENTE, FAMILIAR, HOY).estado(), "la regla solo aplica a Pediatria");
    }

    @Test
    @DisplayName("T100-26: la edad se mide el dia de la cita: si cumple 10 antes de ese dia, ya no es pediatrico")
    void pediatria_cumple10AntesDelDia_rechazada() {
        SolicitudService conEdades = conNacimientos(Map.of(PACIENTE, LUNES.minusYears(10)));

        assertEquals(EstadoSolicitud.PENDIENTE,
                conEdades.solicitar(PACIENTE, TipoEspecialidad.PEDIATRIA, HOY).estado(), "hoy todavia tiene 9");
        assertCodigo("EDAD_NO_PEDIATRICA",
                () -> conEdades.solicitarReprogramada(PACIENTE, TipoEspecialidad.PEDIATRIA, LUNES));
    }

    // ---- Solicitud duplicada ----

    @Test
    @DisplayName("CP-15: un paciente con una solicitud en curso de la misma especialidad no puede pedir otra: "
            + "YA_TIENE_SOLICITUD_ACTIVA")
    void solicitudActivaMismaEspecialidad_rechazada() {
        service.solicitar(PACIENTE, FAMILIAR, HOY);

        assertCodigo("YA_TIENE_SOLICITUD_ACTIVA", () -> service.solicitar(PACIENTE, FAMILIAR, LUNES));
        assertEquals(1, solicitudes.solicitudes.size());
    }

    @Test
    void solicitudEnEsperaMismaEspecialidad_rechazada() {
        SolicitudDTO s = service.solicitar(PACIENTE, FAMILIAR, HOY);
        solicitudes.store(conEstado(s, EstadoSolicitud.EN_ESPERA));

        assertCodigo("YA_TIENE_SOLICITUD_ACTIVA", () -> service.solicitar(PACIENTE, FAMILIAR, LUNES));
    }

    @Test
    void otraEspecialidadOtroDia_seAcepta() {
        service.solicitar(PACIENTE, FAMILIAR, HOY);

        assertEquals(EstadoSolicitud.PENDIENTE,
                service.solicitar(PACIENTE, TipoEspecialidad.ODONTOLOGIA, LUNES).estado());
    }

    @Test
    @DisplayName("RN-10: otra especialidad el MISMO dia de una solicitud activa es YA_TIENE_CITA_ESE_DIA")
    void otraEspecialidadMismoDia_rechazada() {
        service.solicitar(PACIENTE, FAMILIAR, LUNES);

        assertCodigo("YA_TIENE_CITA_ESE_DIA",
                () -> service.solicitar(PACIENTE, TipoEspecialidad.ODONTOLOGIA, LUNES));
    }

    @Test
    void solicitudAnteriorAsignadaOSinCupo_noImpide() {
        SolicitudDTO s = service.solicitar(PACIENTE, FAMILIAR, HOY);
        solicitudes.store(conEstado(s, EstadoSolicitud.SIN_CUPO));

        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitar(PACIENTE, FAMILIAR, LUNES).estado());
    }

    // ---- Reprogramacion (CU-10): parte de un dia concreto ----

    @Test
    @DisplayName("CP-16: una solicitud de reprogramacion entra a la cola con prioridad 2")
    void reprogramacion_prioridad2() {
        SolicitudDTO s = service.solicitarReprogramada(PACIENTE, FAMILIAR, LUNES);

        assertEquals(2, s.prioridad());
        assertEquals(LUNES, s.diaSolicitado());
        assertEquals(EstadoSolicitud.PENDIENTE, s.estado());
    }

    @Test
    @DisplayName("CP-13: reprogramar para un domingo es rechazado con DIA_NO_HABIL")
    void reprogramacionDomingo_diaNoHabil() {
        assertCodigo("DIA_NO_HABIL", () -> service.solicitarReprogramada(PACIENTE, FAMILIAR, DOMINGO));
        assertTrue(solicitudes.solicitudes.isEmpty());
    }

    @Test
    @DisplayName("CP-14: reprogramar mas alla de los 15 dias habiles o a un dia pasado es FUERA_DE_RANGO")
    void reprogramacionFueraDeVentana_fueraDeRango() {
        assertEquals(EstadoSolicitud.PENDIENTE, service.solicitarReprogramada(PACIENTE, FAMILIAR, LIMITE).estado());
        assertCodigo("FUERA_DE_RANGO",
                () -> service.solicitarReprogramada(OTRO_PACIENTE, FAMILIAR, LIMITE.plusDays(1)));
        assertCodigo("FUERA_DE_RANGO",
                () -> service.solicitarReprogramada(OTRO_PACIENTE, FAMILIAR, HOY.minusDays(1)));
    }

    @Test
    void reprogramacionConCitaEseDia_yaTieneCita() {
        cita(PACIENTE, TipoEspecialidad.PEDIATRIA, LUNES, EstadoCita.PROGRAMADA);

        assertCodigo("YA_TIENE_CITA_ESE_DIA", () -> service.solicitarReprogramada(PACIENTE, FAMILIAR, LUNES));
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
        assertCodigo("DATOS_INVALIDOS", () -> service.solicitarReprogramada(PACIENTE, FAMILIAR, null));
    }

    // ---- Consultas (Mis citas) ----

    @Test
    void listar_soloLasDelPaciente_masRecienteprimero() {
        SolicitudDTO familiar = service.solicitar(PACIENTE, FAMILIAR, HOY);
        SolicitudDTO odonto = service.solicitar(PACIENTE, TipoEspecialidad.ODONTOLOGIA, LUNES);
        service.solicitar(OTRO_PACIENTE, FAMILIAR, HOY);

        List<Long> ids = service.listarPorPaciente(PACIENTE).stream().map(SolicitudDTO::solicitudId).toList();

        assertEquals(List.of(odonto.solicitudId(), familiar.solicitudId()), ids);
    }

    @Test
    void consultar_deOtroPaciente_noEsDelPaciente() {
        SolicitudDTO s = service.solicitar(PACIENTE, FAMILIAR, HOY);

        assertCodigo("NO_ES_DEL_PACIENTE", () -> service.consultar(OTRO_PACIENTE, s.solicitudId()));
        assertCodigo("NO_ENCONTRADA", () -> service.consultar(PACIENTE, 999));
    }

    private SolicitudService conNacimientos(Map<Long, LocalDate> nacimientos) {
        return new SolicitudService(solicitudes, citas, new CalendarioHabil(diasNoHabiles), new RelojFijo(AHORA),
                id -> Optional.ofNullable(nacimientos.get(id)));
    }

    private static SolicitudDTO conEstado(SolicitudDTO s, EstadoSolicitud estado) {
        return new SolicitudDTO(s.solicitudId(), s.pacienteId(), s.especialidad(), s.diaSolicitado(), s.prioridad(),
                s.ordenLlegada(), estado, s.citaId());
    }

    private void cita(long pacienteId, TipoEspecialidad especialidad, LocalDate dia, EstadoCita estado) {
        citas.store(new CitaDTO(citas.nextIdentity(), pacienteId, "", "", 50, "", especialidad, 9, dia,
                LocalTime.of(15, 0), 1, estado));
    }

    private static void assertCodigo(String codigo, Executable accion) {
        assertEquals(codigo, assertThrows(DominioException.class, accion).getCodigo());
    }
}
