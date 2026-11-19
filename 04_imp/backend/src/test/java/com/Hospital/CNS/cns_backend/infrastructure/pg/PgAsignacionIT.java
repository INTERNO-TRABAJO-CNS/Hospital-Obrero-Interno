package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.citas.AsignacionService;
import com.Hospital.CNS.cns_backend.model.citas.CalendarioHabil;
import com.Hospital.CNS.cns_backend.model.citas.CitaService;
import com.Hospital.CNS.cns_backend.model.citas.DisponibilidadService;
import com.Hospital.CNS.cns_backend.model.citas.ResultadoAsignacion;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Integracion contra PostgreSQL de CU-05/CU-07 (T100-03): PgSolicitudRepository, PgCambioCitaRepository y
 * AsignacionService con transaccion real. No corre con {@code ./mvnw test}; ejecutar con
 * {@code ./mvnw test -Dtest=PgAsignacionIT}. Necesita las migraciones V1..V10 aplicadas (las aplica Flyway al
 * arrancar el backend). Crea sus propios datos y los borra. Si no hay base de datos, la prueba se omite.
 *
 * <p>Usa fechas de 2020 (hoy = lunes 02/03/2020) para que la cola no tome solicitudes reales, y solo ve al medico de
 * la prueba.</p>
 */
class PgAsignacionIT {

    private static final LocalDate DIA = LocalDate.of(2020, 3, 2);
    private static final LocalDateTime AHORA = DIA.atTime(6, 0);
    private static final TipoEspecialidad FAMILIAR = TipoEspecialidad.CONSULTORIO_FAMILIAR;
    private static final int SOLICITUDES = 10;

    private final PgCitaRepository citas = new PgCitaRepository();
    private final PgSolicitudRepository solicitudes = new PgSolicitudRepository();
    private final PgCambioCitaRepository cambios = new PgCambioCitaRepository();
    private final PgMedicoRepository medicosPg = new PgMedicoRepository();
    private final PgHorarioRepository horarios = new PgHorarioRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();

    private long medicoId;
    private int consultorioId;
    private long horarioId;
    private final List<Long> pacienteIds = new ArrayList<>();
    private final List<Long> solicitudIds = new ArrayList<>();

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        medicoId = medicosPg.nextIdentity();
        medicosPg.store(new MedicoDTO(medicoId, "Cola", "IT", FAMILIAR, 30, 7, true));
        consultorioId = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorioId, "Consultorio Cola IT " + consultorioId, true));
        // Solo los lunes de 08:00 a 09:30: 3 huecos de 30 minutos por dia.
        horarioId = horarios.nextIdentity();
        horarios.store(new HorarioDTO(horarioId, medicoId, consultorioId, DIA.getDayOfWeek(), LocalTime.of(8, 0),
                LocalTime.of(9, 30), true));
        for (int i = 0; i < SOLICITUDES; i++) {
            long id = pacientes.nextIdentity();
            assertTrue(pacientes.store(new PacienteDTO(id, "AF-10019", "IT-" + id, "Paciente", "Cola " + i,
                    LocalDate.of(1990, 1, 1), false)));
            pacienteIds.add(id);
        }
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (medicoId == 0) {
            return; // se omitio por falta de base de datos
        }
        try (Connection conn = DB.getConnection()) {
            for (long paciente : pacienteIds) {
                borrar(conn, "delete from citas.solicitud where paciente_id = ?", paciente);
            }
            borrar(conn, "delete from citas.cambio_cita where cita_id in "
                    + "(select id from citas.cita where medico_id = ?)", medicoId);
            borrar(conn, "delete from citas.agenda_dia where medico_id = ?", medicoId);
            borrar(conn, "delete from citas.cita where medico_id = ?", medicoId);
            borrar(conn, "delete from recursos.horario_medico where id = ?", horarioId);
            for (long paciente : pacienteIds) {
                borrar(conn, "delete from identidad.paciente where id = ?", paciente);
            }
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorioId);
            borrar(conn, "delete from recursos.medico where id = ?", medicoId);
        }
    }

    @Test
    @DisplayName("CP-23: 10 solicitudes simultaneas para 3 huecos crean exactamente 3 citas, sin duplicados (PostgreSQL)")
    void IT_concurrencia_10solicitudes_3huecos() throws Exception {
        for (int i = 0; i < SOLICITUDES; i++) {
            nuevaSolicitud(pacienteIds.get(i), DIA, EstadoSolicitud.PENDIENTE);
        }
        AsignacionService service = servicio();

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(SOLICITUDES);
        try {
            List<Future<Integer>> trabajos = new ArrayList<>();
            for (int i = 0; i < SOLICITUDES; i++) {
                trabajos.add(hilos.submit(() -> {
                    salida.await();
                    int vueltas = 0;
                    while (vueltas < 500 && service.procesarSiguiente() != ResultadoAsignacion.NADA_PENDIENTE) {
                        vueltas++;
                    }
                    return vueltas;
                }));
            }
            salida.countDown();
            for (Future<Integer> t : trabajos) {
                t.get(60, TimeUnit.SECONDS);
            }
        } finally {
            hilos.shutdownNow();
        }
        service.procesarCola(1000); // por si algun hilo termino mientras otro tenia la ultima solicitud

        List<CitaDTO> delDia = vivas(DIA);
        assertEquals(3, delDia.size(), "exactamente 3 citas en los 3 huecos del dia pedido");
        assertEquals(3, delDia.stream().map(CitaDTO::hora).distinct().count(), "sin horas duplicadas");
        assertEquals(List.of(1, 2, 3), delDia.stream().map(CitaDTO::numeroFicha).sorted().toList());

        // Las otras 7 esperaron a los lunes siguientes (3 + 3 + 1), nunca mas de 3 por dia.
        Map<LocalDate, Long> porDia = todasLasCitas().stream()
                .collect(Collectors.groupingBy(CitaDTO::fecha, Collectors.counting()));
        assertEquals(Map.of(DIA, 3L, DIA.plusWeeks(1), 3L, DIA.plusWeeks(2), 3L, DIA.plusWeeks(3), 1L), porDia);

        List<SolicitudDTO> finales = solicitudIds.stream().map(id -> solicitudes.porId(id).orElseThrow()).toList();
        assertTrue(finales.stream().allMatch(s -> s.estado() == EstadoSolicitud.ASIGNADA), finales.toString());
        assertEquals(SOLICITUDES, new HashSet<>(finales.stream().map(SolicitudDTO::citaId).toList()).size(),
                "cada solicitud tiene su propia cita");
        for (SolicitudDTO s : finales) {
            CitaDTO cita = citas.porId(s.citaId()).orElseThrow();
            assertEquals(s.pacienteId(), cita.pacienteId());
            assertEquals(1, cambios.deCita(cita.citaId()).size(), "un CambioCita por cita");
        }
    }

    @Test
    void solicitud_guardarLeerYCola() {
        long primera = nuevaSolicitud(pacienteIds.get(0), DIA, EstadoSolicitud.PENDIENTE);
        long segunda = nuevaSolicitud(pacienteIds.get(1), DIA, EstadoSolicitud.EN_ESPERA);
        SolicitudDTO leida = solicitudes.porId(primera).orElseThrow();
        assertEquals(EstadoSolicitud.PENDIENTE, leida.estado());
        assertEquals(DIA, leida.diaSolicitado());
        assertTrue(leida.ordenLlegada() > 0, "la base pone el orden de llegada");
        assertTrue(solicitudes.pacienteTieneSolicitudActivaEnDia(pacienteIds.get(0), DIA));
        assertFalse(solicitudes.pacienteTieneSolicitudActivaEnDia(pacienteIds.get(0), DIA.plusDays(1)));

        List<Long> cola = solicitudes.siguientesParaProcesar(DIA, 50).stream()
                .map(SolicitudDTO::solicitudId).filter(solicitudIds::contains).toList();
        assertEquals(List.of(primera, segunda), cola);
    }

    @Test
    void cola_dosTransacciones_noTomanLaMismaSolicitud() throws Exception {
        nuevaSolicitud(pacienteIds.get(0), DIA, EstadoSolicitud.PENDIENTE);
        nuevaSolicitud(pacienteIds.get(1), DIA, EstadoSolicitud.PENDIENTE);
        CountDownLatch tomaron = new CountDownLatch(2);
        CountDownLatch soltar = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<Optional<Long>>> f = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                f.add(hilos.submit(() -> new PgTransaccion().ejecutar(() -> {
                    Optional<Long> id = solicitudes.siguientesParaProcesar(DIA, 1).stream()
                            .map(SolicitudDTO::solicitudId).findFirst();
                    tomaron.countDown();
                    esperar(soltar);
                    return id;
                })));
            }
            assertTrue(tomaron.await(10, TimeUnit.SECONDS));
            soltar.countDown();
            Optional<Long> a = f.get(0).get(10, TimeUnit.SECONDS);
            Optional<Long> b = f.get(1).get(10, TimeUnit.SECONDS);
            assertTrue(a.isPresent() && b.isPresent());
            assertNotEquals(a.get(), b.get(), "SKIP LOCKED: cada proceso toma una distinta");
        } finally {
            hilos.shutdownNow();
        }
    }

    @Test
    void citaDuplicadaDentroDeTransaccion_devuelveFalseYLaTransaccionSigue() {
        CitaDTO primera = cita(pacienteIds.get(0), LocalTime.of(8, 0));
        assertTrue(citas.store(primera));

        boolean guardoLaSegunda = new PgTransaccion().ejecutar(() -> {
            assertFalse(citas.store(cita(pacienteIds.get(1), LocalTime.of(8, 0))), "UNIQUE (medico, fecha, hora)");
            return citas.store(cita(pacienteIds.get(1), LocalTime.of(8, 30)));
        });

        assertTrue(guardoLaSegunda, "el savepoint deja seguir usando la transaccion");
        assertEquals(2, vivas(DIA).size());
    }

    @Test
    void cambioCita_registrarYBuscar() {
        CitaDTO c = cita(pacienteIds.get(0), LocalTime.of(8, 0));
        assertTrue(citas.store(c));
        LocalDateTime cuando = LocalDateTime.of(2020, 3, 2, 7, 15);
        cambios.registrar(new CambioCitaDTO(0, c.citaId(), "CREADA", "SISTEMA", cuando, "prueba"));

        List<CambioCitaDTO> deCita = cambios.deCita(c.citaId());
        assertEquals(1, deCita.size());
        assertEquals(cuando, deCita.get(0).fechaHora());
        assertEquals(1, cambios.buscar(c.citaId(), "SISTEMA", DIA, DIA).size());
        assertEquals(0, cambios.buscar(c.citaId(), null, DIA.plusDays(1), null).size());
    }

    private AsignacionService servicio() {
        RelojFijo reloj = new RelojFijo(AHORA);
        PgTransaccion tx = new PgTransaccion();
        PgAgendaRepository agenda = new PgAgendaRepository(citas);
        CitaService registro = new CitaService(citas, solicitudes, cambios, aviso -> { }, cita -> { }, reloj, tx);
        return new AsignacionService(solicitudes, citas, new DisponibilidadService(agenda, citas), soloElMedicoDePrueba(),
                horarios, new CalendarioHabil(new PgDiaNoHabilRepository()), registro, reloj, tx);
    }

    /** La base puede tener medicos reales de la especialidad: la prueba solo usa el suyo. */
    private MedicoRepository soloElMedicoDePrueba() {
        return new MedicoRepository() {
            @Override
            public long nextIdentity() {
                return medicosPg.nextIdentity();
            }

            @Override
            public void store(MedicoDTO medico) {
                medicosPg.store(medico);
            }

            @Override
            public Optional<MedicoDTO> porId(long id) {
                return medicosPg.porId(id);
            }

            @Override
            public List<MedicoDTO> listar(TipoEspecialidad filtro) {
                return medicosPg.listar(filtro).stream().filter(m -> m.medicoId() == medicoId).toList();
            }
        };
    }

    private long nuevaSolicitud(long pacienteId, LocalDate dia, EstadoSolicitud estado) {
        long id = solicitudes.nextIdentity();
        solicitudes.store(new SolicitudDTO(id, pacienteId, FAMILIAR, dia, 1, 0, estado, null));
        solicitudIds.add(id);
        return id;
    }

    private CitaDTO cita(long pacienteId, LocalTime hora) {
        return new CitaDTO(citas.nextIdentity(), pacienteId, "", "", medicoId, "", FAMILIAR, consultorioId, DIA, hora, 1,
                EstadoCita.PROGRAMADA);
    }

    private List<CitaDTO> vivas(LocalDate dia) {
        return citas.delMedicoEnFecha(medicoId, dia).stream().filter(c -> c.estado() != EstadoCita.CANCELADA).toList();
    }

    private List<CitaDTO> todasLasCitas() {
        List<CitaDTO> todas = new ArrayList<>();
        for (int semana = 0; semana < 5; semana++) {
            todas.addAll(vivas(DIA.plusWeeks(semana)));
        }
        return todas;
    }

    private static void esperar(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
