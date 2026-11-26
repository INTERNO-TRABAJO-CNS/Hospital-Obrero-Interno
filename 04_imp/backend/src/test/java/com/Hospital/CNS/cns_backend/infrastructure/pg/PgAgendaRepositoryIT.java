package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.citas.CombinacionCandidata;
import com.Hospital.CNS.cns_backend.model.citas.DisponibilidadService;
import com.Hospital.CNS.cns_backend.model.citas.ResultadoDisponibilidad;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * Integracion contra PostgreSQL de PgAgendaRepository y DisponibilidadService (CU-06, RNF-04). No corre con
 * {@code ./mvnw test}; ejecutar con {@code ./mvnw test -Dtest=PgAgendaRepositoryIT}. Necesita las migraciones
 * V1..V9 aplicadas (las aplica Flyway al arrancar el backend). Crea sus propios datos y los borra. Si no hay base
 * de datos, la prueba se omite.
 *
 * <p>Concurrencia real: cada hilo abre su propia transaccion, intenta tomar la agenda y la MANTIENE abierta hasta que
 * el otro hilo tambien lo intento; recien entonces ambos hacen commit.</p>
 */
class PgAgendaRepositoryIT {

    private static final LocalDate DIA = LocalDate.of(2031, 3, 4);
    private static final int DURACION = 40;

    private final PgAgendaRepository agenda = new PgAgendaRepository();
    private final PgCitaRepository citas = new PgCitaRepository();
    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();

    private long medicoId;
    private int consultorioId;
    private long pacienteA;
    private long pacienteB;

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        medicoId = medicos.nextIdentity();
        medicos.store(new MedicoDTO(medicoId, "Agenda", "IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, DURACION, 7, true));
        consultorioId = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorioId, "Consultorio Agenda IT " + consultorioId, true));
        pacienteA = nuevoPaciente("AF-10019");
        pacienteB = nuevoPaciente("AF-10020");
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (medicoId == 0) {
            return; // se omitio por falta de base de datos
        }
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from citas.agenda_dia where medico_id = ?", medicoId);
            borrar(conn, "delete from citas.cita where medico_id = ?", medicoId);
            borrar(conn, "delete from identidad.paciente where id = ?", pacienteA);
            borrar(conn, "delete from identidad.paciente where id = ?", pacienteB);
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorioId);
            borrar(conn, "delete from recursos.medico where id = ?", medicoId);
        }
    }

    @Test
    void IT_dosHilosUnSoloGanador() throws Exception {
        // La agenda de ese dia ya existe (alguien la abrio antes): los dos compiten por el FOR UPDATE SKIP LOCKED.
        assertTrue(new PgTransaccion().ejecutar(() -> agenda.intentarBloquear(medicoId, DIA)));

        List<Boolean> resultados = carrera(() -> agenda.intentarBloquear(medicoId, DIA),
                () -> agenda.intentarBloquear(medicoId, DIA));

        assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count(), "solo uno toma la agenda");
    }

    @Test
    void IT_dosHilosUnSoloGanador_agendaNueva() throws Exception {
        // Nadie abrio todavia la agenda de ese dia: los dos intentan crear la fila al mismo tiempo.
        List<Boolean> resultados = carrera(() -> agenda.intentarBloquear(medicoId, DIA.plusDays(1)),
                () -> agenda.intentarBloquear(medicoId, DIA.plusDays(1)));

        assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count(), "solo uno toma la agenda");
    }

    @Test
    void IT_bloqueoSeLiberaAlTerminarLaTransaccion() {
        PgTransaccion tx = new PgTransaccion();
        assertTrue(tx.ejecutar(() -> agenda.intentarBloquear(medicoId, DIA)));

        assertTrue(tx.ejecutar(() -> agenda.intentarBloquear(medicoId, DIA)), "tras el commit otro la puede tomar");
        assertTrue(tx.ejecutar(() -> agenda.intentarBloquear(medicoId, DIA)
                && agenda.intentarBloquear(medicoId, DIA)), "la misma transaccion la puede pedir dos veces");
    }

    @Test
    void IT_otroMedicoOtroDia_noSeBloquean() throws Exception {
        List<Boolean> resultados = carrera(() -> agenda.intentarBloquear(medicoId, DIA),
                () -> agenda.intentarBloquear(medicoId, DIA.plusDays(1)));

        assertEquals(List.of(true, true), resultados);
    }

    @Test
    @DisplayName("CP-29: dos hilos piden el mismo hueco a la vez y solo uno lo confirma (PostgreSQL)")
    void IT_dosSolicitudesMismoHueco_soloUnaCitaGuardada() throws Exception {
        DisponibilidadService service = new DisponibilidadService(agenda, citas);
        LocalTime hora = LocalTime.of(9, 0);

        List<ResultadoDisponibilidad> resultados = carrera(
                () -> validarYGuardar(service, pacienteA, hora),
                () -> validarYGuardar(service, pacienteB, hora));

        assertEquals(1, resultados.stream().filter(r -> r == ResultadoDisponibilidad.DISPONIBLE).count(),
                "solo una solicitud confirma: " + resultados);
        assertEquals(1, citas.delMedicoEnFecha(medicoId, DIA).size(), "no hay doble asignacion");
    }

    @Test
    void IT_consultorioOcupado_usaLaDuracionDelMedico() {
        citas.store(cita(pacienteA, LocalTime.of(9, 0), EstadoCita.PROGRAMADA)); // 09:00 - 09:40

        assertTrue(agenda.consultorioOcupado(consultorioId, DIA, LocalTime.of(9, 30), LocalTime.of(10, 0)));
        assertTrue(agenda.consultorioOcupado(consultorioId, DIA, LocalTime.of(8, 30), LocalTime.of(9, 10)));
        assertFalse(agenda.consultorioOcupado(consultorioId, DIA, LocalTime.of(9, 40), LocalTime.of(10, 20)));
        assertFalse(agenda.consultorioOcupado(consultorioId, DIA, LocalTime.of(8, 20), LocalTime.of(9, 0)));
        assertFalse(agenda.consultorioOcupado(consultorioId, DIA.plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 40)));
    }

    @Test
    void IT_ocupacionDelMedico_sinCanceladas() {
        long cancelada = citas.nextIdentity();
        citas.store(new CitaDTO(cancelada, pacienteA, "", "", medicoId, "", TipoEspecialidad.CONSULTORIO_FAMILIAR,
                consultorioId, DIA, LocalTime.of(8, 0), 1, EstadoCita.CANCELADA));
        citas.store(cita(pacienteB, LocalTime.of(9, 0), EstadoCita.PROGRAMADA));

        List<CitaDTO> ocupacion = agenda.ocupacionDelMedico(medicoId, DIA);

        assertEquals(1, ocupacion.size());
        assertEquals(LocalTime.of(9, 0), ocupacion.get(0).hora());
        assertFalse(agenda.consultorioOcupado(consultorioId, DIA, LocalTime.of(8, 0), LocalTime.of(8, 40)),
                "una cita CANCELADA no ocupa el consultorio");
    }

    private ResultadoDisponibilidad validarYGuardar(DisponibilidadService service, long pacienteId, LocalTime hora) {
        ResultadoDisponibilidad r = service.validar(
                new CombinacionCandidata(pacienteId, medicoId, consultorioId, DIA, hora, DURACION));
        if (r == ResultadoDisponibilidad.DISPONIBLE) {
            assertTrue(citas.store(cita(pacienteId, hora, EstadoCita.PROGRAMADA)));
        }
        return r;
    }

    /**
     * Ejecuta los dos trabajos a la vez, cada uno en su propia transaccion. Ninguna transaccion termina hasta que los
     * dos trabajos respondieron (o hasta 2 s, si uno quedo esperando dentro de PostgreSQL a que el otro termine).
     */
    private static <T> List<T> carrera(Supplier<T> primero, Supplier<T> segundo) throws Exception {
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch respondieron = new CountDownLatch(2);
        CountDownLatch soltar = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<T> a = hilos.submit(() -> enTransaccionRetenida(primero, salida, respondieron, soltar));
            Future<T> b = hilos.submit(() -> enTransaccionRetenida(segundo, salida, respondieron, soltar));
            salida.countDown();
            respondieron.await(2, TimeUnit.SECONDS);
            soltar.countDown();
            return List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
        } finally {
            hilos.shutdownNow();
        }
    }

    private static <T> T enTransaccionRetenida(Supplier<T> trabajo, CountDownLatch salida, CountDownLatch respondieron,
                                               CountDownLatch soltar) throws InterruptedException {
        salida.await();
        return new PgTransaccion().ejecutar(() -> {
            T resultado = trabajo.get();
            respondieron.countDown();
            try {
                soltar.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return resultado;
        });
    }

    private long nuevoPaciente(String afiliado) {
        long id = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(id, afiliado, "IT-" + id, "Paciente", "Agenda",
                LocalDate.of(1990, 1, 1), false)));
        return id;
    }

    private CitaDTO cita(long pacienteId, LocalTime hora, EstadoCita estado) {
        return new CitaDTO(citas.nextIdentity(), pacienteId, "", "", medicoId, "", TipoEspecialidad.CONSULTORIO_FAMILIAR,
                consultorioId, DIA, hora, 1, estado);
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
