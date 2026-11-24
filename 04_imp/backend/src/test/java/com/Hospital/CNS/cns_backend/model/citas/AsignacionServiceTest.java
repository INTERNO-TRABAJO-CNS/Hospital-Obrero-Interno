package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeHorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * CU-05 con Fakes: un test por cada flujo del diagrama CU-05_AsignarCitaAutomaticamente ([2a], [2b], [3a], [3b])
 * mas las reglas de la cola. Hoy: miercoles 18/11/2026 07:00; el paciente pide el lunes 23/11/2026.
 * Ventana de 15 dias habiles: hasta el miercoles 09/12/2026.
 */
class AsignacionServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 7, 0);
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);
    private static final LocalDate MARTES = LUNES.plusDays(1);
    private static final LocalDate VIERNES = LocalDate.of(2026, 11, 20);
    private static final LocalDate LIMITE = LocalDate.of(2026, 12, 9);
    private static final long RAMOS = 1;
    private static final long LOPEZ = 2;
    private static final TipoEspecialidad FAMILIAR = TipoEspecialidad.CONSULTORIO_FAMILIAR;

    private final RelojFijo reloj = new RelojFijo(AHORA);
    private final FakeSolicitudRepository solicitudes = new FakeSolicitudRepository();
    private final FakeMedicoRepository medicos = new FakeMedicoRepository();
    private final FakeHorarioRepository horarios = new FakeHorarioRepository();
    private final FakeDiaNoHabilRepository diasNoHabiles = new FakeDiaNoHabilRepository();
    private final FakeNotificacionPublicador avisos = new FakeNotificacionPublicador();
    private final FakeAgendaEventos eventos = new FakeAgendaEventos();
    private final FakeCambioCitaRepository cambios = new FakeCambioCitaRepository();

    /** Rechaza una vez la cita de {@link #rechazarHora} (como si otro proceso la hubiera guardado antes). */
    private LocalTime rechazarHora;
    private final FakeCitaRepository citas = new FakeCitaRepository() {
        @Override
        public synchronized boolean store(CitaDTO cita) {
            if (cita.hora().equals(rechazarHora)) {
                rechazarHora = null;
                return false;
            }
            return super.store(cita);
        }
    };
    private final FakeAgendaRepository agenda = new FakeAgendaRepository(citas);
    private final AsignacionService service = new AsignacionService(solicitudes, citas,
            new DisponibilidadService(agenda, citas), medicos, horarios, new CalendarioHabil(diasNoHabiles),
            new CitaService(citas, solicitudes, cambios, avisos, eventos, reloj), reloj);

    // ---- Cola ----

    @Test
    @DisplayName("CP-17: tres solicitudes que llegan en orden se asignan en ese mismo orden")
    void asigna_enOrdenDeLlegada() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        // Se guardan desordenadas; el orden de llegada es 11 -> 10 -> 12.
        solicitud(10, 100, LUNES, 1, 2);
        solicitud(12, 102, LUNES, 1, 3);
        solicitud(11, 101, LUNES, 1, 1);

        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());
        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());
        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());

        assertEquals(101, citaDe(11).pacienteId());
        assertEquals(LocalTime.of(8, 0), citaDe(11).hora(), "1ra en llegar -> 1ra hora");
        assertEquals(LocalTime.of(8, 30), citaDe(10).hora(), "2da en llegar -> 2da hora");
        assertEquals(LocalTime.of(9, 0), citaDe(12).hora(), "3ra en llegar -> 3ra hora");
        assertEquals(List.of(1, 2, 3), List.of(citaDe(11).numeroFicha(), citaDe(10).numeroFicha(),
                citaDe(12).numeroFicha()), "numero de ficha en el mismo orden");
    }

    @Test
    @DisplayName("CP-18: una solicitud de prioridad 2 se atiende despues de todas las de prioridad 1")
    void prioridad2_vaDespuesDePrioridad1() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(8, 30));
        solicitud(10, 100, LUNES, 2, 1);
        solicitud(11, 101, LUNES, 1, 9);

        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());

        assertEquals(EstadoSolicitud.ASIGNADA, estado(11));
        assertEquals(EstadoSolicitud.PENDIENTE, estado(10));
    }

    @Test
    void sinSolicitudes_nadaPendiente() {
        assertEquals(ResultadoAsignacion.NADA_PENDIENTE, service.procesarSiguiente());
    }

    @Test
    void solicitudFueraDeLaVentanaDe15Dias_noSeProcesaTodavia() {
        solicitud(10, 100, LIMITE.plusDays(1), 1, 1);

        assertEquals(ResultadoAsignacion.NADA_PENDIENTE, service.procesarSiguiente());
    }

    @Test
    void procesarCola_respetaElMaximo() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0));
        solicitud(10, 100, LUNES, 1, 1);
        solicitud(11, 101, LUNES, 1, 2);
        solicitud(12, 102, LUNES, 1, 3);

        assertEquals(2, service.procesarCola(2));
        assertEquals(1, service.procesarCola(10));
        assertEquals(3, citas.citas.size());
    }

    // ---- Camino principal: medico, franja y registro ----

    @Test
    void eligeAlMedicoConMenosFichasEseDia() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0));
        medico(LOPEZ, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0));
        citaExistente(RAMOS, 200, LocalTime.of(8, 0));
        citaExistente(RAMOS, 201, LocalTime.of(8, 30));
        citaExistente(LOPEZ, 202, LocalTime.of(8, 0));
        solicitud(10, 100, LUNES, 1, 1);

        service.procesarSiguiente();

        assertEquals(LOPEZ, citaDe(10).medicoId());
        assertEquals(LocalTime.of(8, 30), citaDe(10).hora());
        assertEquals(2, citaDe(10).numeroFicha());
    }

    @Test
    void empateDeFichas_eligeElMenorMedicoId() {
        medico(LOPEZ, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0));
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0));
        solicitud(10, 100, LUNES, 1, 1);

        service.procesarSiguiente();

        assertEquals(RAMOS, citaDe(10).medicoId());
        assertEquals(1, citaDe(10).consultorio());
    }

    @Test
    void franjasConsecutivas_noSeSalenDelHorario() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(9, 15));
        citaExistente(RAMOS, 200, LocalTime.of(8, 0));
        citaExistente(RAMOS, 201, LocalTime.of(8, 30));
        solicitud(10, 100, LUNES, 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente(), "09:00-09:30 pasa de las 09:15");
    }

    @Test
    void hoy_soloFranjasQueTodaviaNoPasaron() {
        reloj.fijar(LUNES.atTime(8, 10));
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        solicitud(10, 100, LUNES, 1, 1);

        service.procesarSiguiente();

        assertEquals(LocalTime.of(8, 30), citaDe(10).hora());
    }

    @Test
    void medicoYHorarioInactivos_seIgnoran() {
        medicos.store(new MedicoDTO(RAMOS, "Carlos", "Ramos", FAMILIAR, 30, 7, false));
        horarios.store(new HorarioDTO(1, RAMOS, 1, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0), true));
        medicos.store(new MedicoDTO(LOPEZ, "Ana", "Lopez", FAMILIAR, 30, 7, true));
        horarios.store(new HorarioDTO(2, LOPEZ, 2, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0), false));
        solicitud(10, 100, LUNES, 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente());
        assertTrue(citas.citas.isEmpty());
    }

    @Test
    void asignada_registraCambioYPublicaDespues() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        solicitud(10, 100, LUNES, 1, 1);

        service.procesarSiguiente();

        CitaDTO cita = citaDe(10);
        assertEquals(EstadoCita.PROGRAMADA, cita.estado());
        assertEquals(1, cambios.deCita(cita.citaId()).size());
        assertEquals(1, avisos.publicadas.size());
        assertEquals(cita.citaId(), eventos.eventos.get(0).citaId());
    }

    // ---- [2a] NO HAY CUPOS: en espera al siguiente dia habil ----

    @Test
    @DisplayName("CP-19: sin cupo el dia pedido -> NO HAY CUPOS y queda EN_ESPERA para el siguiente dia habil")
    void sinCupoHoy_pasaAEnEsperaSiguienteHabil() {
        medico(RAMOS, VIERNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(12, 0));
        for (int i = 0; i < 7; i++) {
            citas.store(new CitaDTO(citas.nextIdentity(), 200 + i, "", "", RAMOS, "", FAMILIAR, 1, VIERNES,
                    LocalTime.of(8, 0).plusMinutes(30L * i), i + 1, EstadoCita.PROGRAMADA));
        }
        solicitud(10, 100, VIERNES, 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente());

        SolicitudDTO s = solicitudes.porId(10).orElseThrow();
        assertEquals(EstadoSolicitud.EN_ESPERA, s.estado());
        assertEquals(LUNES, s.diaSolicitado(), "viernes sin cupo -> lunes");
        assertNull(s.citaId());
        assertEquals(7, citas.citas.size());
    }

    @Test
    void enEspera_asignaElSiguienteDiaHabilQueTengaCupo() {
        medico(RAMOS, MARTES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(9, 0));
        diasNoHabiles.store(new DiaNoHabilDTO(1, LUNES, "Feriado", true));
        solicitud(10, 100, VIERNES, 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente());
        assertEquals(MARTES, solicitudes.porId(10).orElseThrow().diaSolicitado(), "salta el feriado del lunes");
        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());

        assertEquals(MARTES, citaDe(10).fecha());
    }

    @Test
    @DisplayName("CP-21: si el paciente ya tiene una cita ese dia, la solicitud no se asigna")
    void pacienteConCitaEseDia_noAsigna() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        citas.store(new CitaDTO(citas.nextIdentity(), 100, "", "", 50, "", TipoEspecialidad.PEDIATRIA, 9, LUNES,
                LocalTime.of(15, 0), 1, EstadoCita.PROGRAMADA));
        solicitud(10, 100, LUNES, 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente());

        assertEquals(1, citas.citas.size());
        assertEquals(MARTES, solicitudes.porId(10).orElseThrow().diaSolicitado());
    }

    @Test
    void diaSolicitadoYaPasado_esperaDesdeHoy() {
        solicitud(10, 100, AHORA.toLocalDate().minusDays(3), 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente());

        assertEquals(AHORA.toLocalDate(), solicitudes.porId(10).orElseThrow().diaSolicitado());
    }

    // ---- [2b] 15 dias habiles sin cupo ----

    @Test
    @DisplayName("CP-20: tras 15 dias habiles sin cupo la solicitud pasa a SIN_CUPO")
    void dias15SinCupo_sinCupo() {
        solicitud(10, 100, LUNES, 1, 1);

        service.procesarCola(100);

        SolicitudDTO s = solicitudes.porId(10).orElseThrow();
        assertEquals(EstadoSolicitud.SIN_CUPO, s.estado());
        assertEquals(LIMITE, s.diaSolicitado(), "llego hasta el ultimo dia habil de la ventana");
        assertTrue(citas.citas.isEmpty());
        assertEquals(ResultadoAsignacion.NADA_PENDIENTE, service.procesarSiguiente());
    }

    @Test
    void ultimoDiaDeLaVentanaSinCupo_sinCupoEnUnaVuelta() {
        solicitud(10, 100, LIMITE, 1, 1);

        assertEquals(ResultadoAsignacion.SIN_CUPO, service.procesarSiguiente());
    }

    // ---- [3a] La combinacion deja de estar disponible ----

    @Test
    void registroRechazadoPorLaBase_sigueConLaSiguienteFranja() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        solicitud(10, 100, LUNES, 1, 1);
        rechazarHora = LocalTime.of(8, 0);

        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());

        assertEquals(LocalTime.of(8, 30), citaDe(10).hora());
    }

    @Test
    void franjaOcupada_sigueConLaSiguiente() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        citaExistente(RAMOS, 200, LocalTime.of(8, 0));
        solicitud(10, 100, LUNES, 1, 1);

        service.procesarSiguiente();

        assertEquals(LocalTime.of(8, 30), citaDe(10).hora());
    }

    // ---- [3b] Otra solicitud simultanea tiene el cupo ----

    @Test
    void bloqueadaPorOtro_quedaEnEsperaElMismoDia() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        agenda.tomarPorOtro(RAMOS, LUNES);
        solicitud(10, 100, LUNES, 1, 1);

        assertEquals(ResultadoAsignacion.EN_ESPERA, service.procesarSiguiente());

        SolicitudDTO s = solicitudes.porId(10).orElseThrow();
        assertEquals(EstadoSolicitud.EN_ESPERA, s.estado());
        assertEquals(LUNES, s.diaSolicitado(), "no pierde su dia: se reintenta");
        assertTrue(citas.citas.isEmpty());
    }

    @Test
    void bloqueadoUnMedico_usaOtroDeLaEspecialidad() {
        medico(RAMOS, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        medico(LOPEZ, LUNES.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        agenda.tomarPorOtro(RAMOS, LUNES);
        solicitud(10, 100, LUNES, 1, 1);

        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());

        assertEquals(LOPEZ, citaDe(10).medicoId());
    }

    private void medico(long id, DayOfWeek dia, LocalTime inicio, LocalTime fin) {
        medicos.store(new MedicoDTO(id, "Medico", String.valueOf(id), FAMILIAR, 30, 7, true));
        horarios.store(new HorarioDTO(horarios.nextIdentity(), id, (int) id, dia, inicio, fin, true));
    }

    private void solicitud(long id, long pacienteId, LocalDate dia, int prioridad, long orden) {
        solicitudes.store(new SolicitudDTO(id, pacienteId, FAMILIAR, dia, prioridad, orden, EstadoSolicitud.PENDIENTE,
                null));
    }

    private void citaExistente(long medicoId, long pacienteId, LocalTime hora) {
        int ficha = citas.delMedicoEnFecha(medicoId, LUNES).size() + 1;
        citas.store(new CitaDTO(citas.nextIdentity(), pacienteId, "", "", medicoId, "", FAMILIAR, (int) medicoId,
                LUNES, hora, ficha, EstadoCita.PROGRAMADA));
    }

    private EstadoSolicitud estado(long solicitudId) {
        return solicitudes.porId(solicitudId).orElseThrow().estado();
    }

    private CitaDTO citaDe(long solicitudId) {
        Long citaId = solicitudes.porId(solicitudId).orElseThrow().citaId();
        return citas.porId(citaId).orElseThrow();
    }
}
