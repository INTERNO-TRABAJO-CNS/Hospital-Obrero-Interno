package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeHorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * T100-24 en las citas comunes (CU-05/CU-06): adulto mayor primero en la cola, Pediatria solo menores de 10 anios y
 * ningun cruce del paciente con sus fichas internas o cirugias. Hoy: miercoles 18/11/2026 07:00; se pide el lunes
 * 23/11/2026.
 */
class ReglasEdadYCrucesTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 7, 0);
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);
    private static final long MEDICO = 1;
    private static final long JOVEN = 100;
    private static final long ADULTO_MAYOR = 101;
    private static final long NINO = 102;

    private final RelojFijo reloj = new RelojFijo(AHORA);
    private final FakeSolicitudRepository solicitudes = new FakeSolicitudRepository();
    private final FakeMedicoRepository medicos = new FakeMedicoRepository();
    private final FakeHorarioRepository horarios = new FakeHorarioRepository();
    private final FakeCitaRepository citas = new FakeCitaRepository();
    private final FakeAgendaRepository agenda = new FakeAgendaRepository(citas);
    private final Map<Long, LocalDate> nacimientos = new HashMap<>(Map.of(
            JOVEN, LocalDate.of(1995, 5, 1),
            ADULTO_MAYOR, LocalDate.of(1950, 5, 1),
            NINO, LocalDate.of(2020, 5, 1)));

    /** Ocupacion del paciente por fichas internas o cirugias: {@code [inicio, fin)} de {@link #ocupadoPaciente}. */
    private Long ocupadoPaciente;
    private LocalTime ocupadoInicio;
    private LocalTime ocupadoFin;
    private final OcupacionPaciente ocupacion = (paciente, fecha, inicio, fin) ->
            ocupadoPaciente != null && ocupadoPaciente == paciente
                    && ocupadoInicio.isBefore(fin) && ocupadoFin.isAfter(inicio);

    private AsignacionService asignacion() {
        return new AsignacionService(solicitudes, citas, new DisponibilidadService(agenda, citas, ocupacion), medicos,
                horarios, new CalendarioHabil(new FakeDiaNoHabilRepository()),
                new CitaService(citas, solicitudes, new FakeCambioCitaRepository(), new FakeNotificacionPublicador(),
                        new FakeAgendaEventos(), reloj),
                reloj, Transaccion.DIRECTA, id -> Optional.ofNullable(nacimientos.get(id)));
    }

    @Test
    @DisplayName("T100-24: con la misma prioridad, el adulto mayor (60+) va antes aunque haya llegado despues")
    void adultoMayor_primeroDentroDeSuPrioridad() {
        medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, LocalTime.of(8, 0), LocalTime.of(9, 0));
        solicitudes.adultosMayores.add(ADULTO_MAYOR);
        solicitud(10, JOVEN, TipoEspecialidad.CONSULTORIO_FAMILIAR, 1, 1);
        solicitud(11, ADULTO_MAYOR, TipoEspecialidad.CONSULTORIO_FAMILIAR, 1, 2);

        AsignacionService service = asignacion();
        service.procesarSiguiente();
        service.procesarSiguiente();

        assertEquals(LocalTime.of(8, 0), citaDe(11).hora(), "el adulto mayor recibe la primera hora");
        assertEquals(LocalTime.of(8, 30), citaDe(10).hora());
    }

    @Test
    @DisplayName("T100-24: la prioridad 1 sigue antes que la 2, aunque la de prioridad 2 sea de un adulto mayor")
    void adultoMayor_noSaltaLaPrioridad() {
        medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, LocalTime.of(8, 0), LocalTime.of(8, 30));
        solicitudes.adultosMayores.add(ADULTO_MAYOR);
        solicitud(10, ADULTO_MAYOR, TipoEspecialidad.CONSULTORIO_FAMILIAR, 2, 1);
        solicitud(11, JOVEN, TipoEspecialidad.CONSULTORIO_FAMILIAR, 1, 2);

        asignacion().procesarSiguiente();

        assertEquals(EstadoSolicitud.ASIGNADA, estado(11));
        assertEquals(EstadoSolicitud.PENDIENTE, estado(10));
    }

    @Test
    @DisplayName("T100-24: Pediatria no asigna a un paciente de 10 anios o mas: SIN_CUPO, sin crear cita")
    void pediatria_mayorDe10_sinCupo() {
        medico(TipoEspecialidad.PEDIATRIA, LocalTime.of(8, 0), LocalTime.of(10, 0));
        solicitud(10, JOVEN, TipoEspecialidad.PEDIATRIA, 1, 1);
        solicitud(11, NINO, TipoEspecialidad.PEDIATRIA, 1, 2);

        AsignacionService service = asignacion();
        assertEquals(ResultadoAsignacion.SIN_CUPO, service.procesarSiguiente());
        assertEquals(ResultadoAsignacion.ASIGNADA, service.procesarSiguiente());

        assertEquals(EstadoSolicitud.SIN_CUPO, estado(10));
        assertNull(solicitudes.porId(10).orElseThrow().citaId());
        assertEquals(NINO, citaDe(11).pacienteId());
    }

    @Test
    @DisplayName("T100-24: si el paciente tiene una ficha interna o cirugia a esa hora, se le da la siguiente franja")
    void cruceConFichaInterna_saltaEsaFranja() {
        medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, LocalTime.of(8, 0), LocalTime.of(10, 0));
        ocupadoPaciente = JOVEN;
        ocupadoInicio = LocalTime.of(7, 45);
        ocupadoFin = LocalTime.of(8, 45);
        solicitud(10, JOVEN, TipoEspecialidad.CONSULTORIO_FAMILIAR, 1, 1);

        asignacion().procesarSiguiente();

        assertEquals(LocalTime.of(9, 0), citaDe(10).hora(), "8:00 y 8:30 se cruzan con 7:45-8:45");
    }

    @Test
    void disponibilidad_pacienteOcupado() {
        ocupadoPaciente = JOVEN;
        ocupadoInicio = LocalTime.of(9, 0);
        ocupadoFin = LocalTime.of(10, 0);
        DisponibilidadService service = new DisponibilidadService(agenda, citas, ocupacion);

        assertEquals(ResultadoDisponibilidad.PACIENTE_OCUPADO,
                service.validar(new CombinacionCandidata(JOVEN, MEDICO, 1, LUNES, LocalTime.of(9, 30), 30)));
        assertEquals(ResultadoDisponibilidad.DISPONIBLE,
                service.validar(new CombinacionCandidata(JOVEN, MEDICO, 1, LUNES, LocalTime.of(10, 0), 30)),
                "terminar justo cuando empieza la otra no cruza");
    }

    private void medico(TipoEspecialidad especialidad, LocalTime inicio, LocalTime fin) {
        medicos.store(new MedicoDTO(MEDICO, "Medico", "1", especialidad, 30, 7, true));
        horarios.store(new HorarioDTO(horarios.nextIdentity(), MEDICO, 1, LUNES.getDayOfWeek(), inicio, fin, true));
    }

    private void solicitud(long id, long pacienteId, TipoEspecialidad especialidad, int prioridad, long orden) {
        solicitudes.store(new SolicitudDTO(id, pacienteId, especialidad, LUNES, prioridad, orden,
                EstadoSolicitud.PENDIENTE, null));
    }

    private EstadoSolicitud estado(long solicitudId) {
        return solicitudes.porId(solicitudId).orElseThrow().estado();
    }

    private CitaDTO citaDe(long solicitudId) {
        Long citaId = solicitudes.porId(solicitudId).orElseThrow().citaId();
        return citas.porId(citaId).orElseThrow();
    }
}
