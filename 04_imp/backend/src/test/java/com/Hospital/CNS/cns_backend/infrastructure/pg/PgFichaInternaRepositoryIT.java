package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.infrastructure.ws.NotificacionesWs;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import tools.jackson.databind.ObjectMapper;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * Integracion contra PostgreSQL de la migracion V7 y PgFichaInternaRepository, PgCambioFichaRepository y
 * PgNotificacionPublicador. No corre con {@code ./mvnw test}; ejecutar con
 * {@code ./mvnw test -Dtest=PgFichaInternaRepositoryIT}. Aplica las migraciones pendientes con Flyway (lo mismo
 * que hace el backend al arrancar), crea sus propios datos y los borra. Sin base de datos, se omite.
 */
class PgFichaInternaRepositoryIT {

    private static final LocalDate DIA = LocalDate.of(2031, 3, 4);

    private final PgFichaInternaRepository fichas = new PgFichaInternaRepository();
    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();

    private long derivante;
    private long especialista;
    private int consultorio;
    private long paciente;

    @BeforeAll
    static void migrar() throws Exception {
        Properties local = new Properties();
        try (InputStream in = PgFichaInternaRepositoryIT.class.getClassLoader()
                .getResourceAsStream("application-local.properties")) {
            Assumptions.assumeTrue(in != null, "Sin application-local.properties");
            local.load(in);
        }
        try {
            Flyway.configure()
                    .dataSource(local.getProperty("spring.datasource.url"), local.getProperty("spring.datasource.username"),
                            local.getProperty("spring.datasource.password"))
                    .locations("classpath:db/migration").baselineOnMigrate(true).baselineVersion("0")
                    .load().migrate();
        } catch (RuntimeException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
    }

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        derivante = medicos.nextIdentity();
        medicos.store(new MedicoDTO(derivante, "Derivante", "IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6, true));
        especialista = medicos.nextIdentity();
        medicos.store(new MedicoDTO(especialista, "Especialista", "IT", TipoEspecialidad.GASTROENTEROLOGIA, 25, 6, true));
        consultorio = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorio, "Consultorio IT " + consultorio, true));
        paciente = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(paciente, "AF-10020", "ITF-" + paciente, "Paciente", "Ficha",
                LocalDate.of(1960, 1, 1), false)));
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from notificacion.notificacion where paciente_id = ?", paciente);
            borrar(conn, "delete from fichas_internas.gastroenterologia where paciente_id = ?", paciente);
            borrar(conn, "delete from fichas_internas.gastroenterologia_pediatria where paciente_id = ?", paciente);
            borrar(conn, "delete from identidad.paciente where id = ?", paciente);
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorio);
            borrar(conn, "delete from recursos.medico where id = ?", derivante);
            borrar(conn, "delete from recursos.medico where id = ?", especialista);
        }
    }

    @Test
    void existenLas20TablasYLaVista() throws SQLException {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select count(*) from information_schema.tables "
                     + "where table_schema = 'fichas_internas' and table_type = 'BASE TABLE' "
                     + "and table_name not in ('derivacion', 'cambio')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            assertEquals(20, rs.getInt(1));
        }
        fichas.activas(TipoEspecialidad.CARDIOLOGIA, true);
    }

    @Test
    void ficha_insertarAsignarLeerYCambiarEstado() {
        long id = fichas.nextIdentity();
        fichas.insertar(ficha(id, false));

        FichaInternaDTO leida = fichas.porId(id).orElseThrow();
        assertEquals(EstadoFichaInterna.EN_ESPERA, leida.estado());
        assertEquals("Paciente Ficha", leida.nombrePaciente());
        assertEquals("Derivante IT", leida.nombreDerivante());
        assertNull(leida.medicoId());
        assertTrue(fichas.pacienteTieneFichaActiva(paciente));
        assertEquals(1, fichas.enEspera(TipoEspecialidad.GASTROENTEROLOGIA).stream()
                .filter(f -> f.fichaId() == id).count());

        fichas.guardarAsignacion(leida.asignada(especialista, "Especialista IT", consultorio, null, DIA,
                LocalTime.of(8, 25), 25));
        FichaInternaDTO asignada = fichas.porId(id).orElseThrow();
        assertEquals(EstadoFichaInterna.ASIGNADA, asignada.estado());
        assertEquals("Especialista IT", asignada.nombreMedico());
        assertEquals("Consultorio IT " + consultorio, asignada.nombreConsultorio());
        assertEquals(1, fichas.delMedicoEnFecha(especialista, DIA).size());
        assertEquals(1, fichas.asignadasEntre(TipoEspecialidad.GASTROENTEROLOGIA, DIA, DIA).stream()
                .filter(f -> f.fichaId() == id).count());
        assertTrue(fichas.medicoTieneFichaConPaciente(especialista, paciente));

        assertTrue(fichas.cambiarEstado(id, EstadoFichaInterna.ASIGNADA, EstadoFichaInterna.ATENDIDA, "Bien"));
        assertFalse(fichas.cambiarEstado(id, EstadoFichaInterna.ASIGNADA, EstadoFichaInterna.CANCELADA, null),
                "el UPDATE es atomico");
        assertEquals("Bien", fichas.porId(id).orElseThrow().observaciones());
        assertFalse(fichas.pacienteTieneFichaActiva(paciente));
    }

    @Test
    void enEspera_conFecha_laBaseLoRechaza() {
        long id = fichas.nextIdentity();
        FichaInternaDTO mala = new FichaInternaDTO(id, TipoEspecialidad.GASTROENTEROLOGIA, true, paciente, "", "",
                null, null, derivante, "", TipoEspecialidad.PEDIATRIA, null, null, null, null, DIA, null, 25,
                Gravedad.ALTA, false, false, id, "Motivo", null, EstadoFichaInterna.EN_ESPERA, LocalDateTime.now());

        assertThrows(RuntimeException.class, () -> fichas.insertar(mala));
    }

    @Test
    void candado_cambioYAviso_dentroDeUnaTransaccion() {
        long id = fichas.nextIdentity();
        new PgTransaccion().ejecutar(() -> {
            fichas.bloquearEspecialidad(TipoEspecialidad.GASTROENTEROLOGIA);
            fichas.insertar(ficha(id, true));
            new PgCambioFichaRepository().registrar("FICHA", id, "GENERADA", "IT", null);
            new PgNotificacionPublicador(new NotificacionesWs(new ObjectMapper()))
                    .publicar(NotificacionDTO.deFicha(paciente, id, "FICHA_INTERNA_EN_ESPERA",
                            "Prueba", LocalDateTime.now()));
        });

        assertTrue(fichas.porId(id).orElseThrow().pediatrica());
    }

    private FichaInternaDTO ficha(long id, boolean pediatrica) {
        return new FichaInternaDTO(id, TipoEspecialidad.GASTROENTEROLOGIA, pediatrica, paciente, "", "", null, null,
                derivante, "", TipoEspecialidad.CONSULTORIO_FAMILIAR, null, null, null, null, null, null, 25,
                Gravedad.MEDIA, false, true, id, "Dolor abdominal", null, EstadoFichaInterna.EN_ESPERA,
                LocalDateTime.of(2031, 3, 3, 9, 0));
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
