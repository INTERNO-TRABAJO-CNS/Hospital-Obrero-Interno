package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaDTO;
import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaDTOs;
import com.Hospital.CNS.cns_backend.model.cirugias.ProgramacionCirugiaService;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoDTO;

/**
 * CU-24 contra PostgreSQL: migracion V8 (EXCLUDE de quirofano, cirujano y paciente), PgQuirofanoRepository,
 * PgCirugiaRepository y ProgramacionCirugiaService de punta a punta. Usa Dermatologia y el lunes 03/03/2031.
 * Ejecutar con {@code ./mvnw test -Dtest=PgCirugiaIT}.
 */
class PgCirugiaIT {

    private static final LocalDate LUNES = LocalDate.of(2031, 3, 3);

    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();
    private final PgFichaInternaRepository fichas = new PgFichaInternaRepository();
    private final PgQuirofanoRepository quirofanos = new PgQuirofanoRepository();
    private final PgCirugiaRepository cirugias = new PgCirugiaRepository();
    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(LUNES, LocalTime.of(9, 0)));

    private long derivante;
    private long cirujano;
    private long paciente;
    private long otroPaciente;
    private int quirofano;
    private long ficha;

    @BeforeAll
    static void migrar() throws Exception {
        Properties local = new Properties();
        try (InputStream in = PgCirugiaIT.class.getClassLoader().getResourceAsStream("application-local.properties")) {
            Assumptions.assumeTrue(in != null, "Sin application-local.properties");
            local.load(in);
        }
        try {
            Flyway.configure()
                    .dataSource(local.getProperty("spring.datasource.url"), local.getProperty("spring.datasource.username"),
                            local.getProperty("spring.datasource.password"))
                    .locations("classpath:db/migration").baselineOnMigrate(true).baselineVersion("0").load().migrate();
        } catch (RuntimeException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
    }

    @BeforeEach
    void preparar() {
        derivante = medicos.nextIdentity();
        medicos.store(new MedicoDTO(derivante, "General", "ITC", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6, true));
        cirujano = medicos.nextIdentity();
        medicos.store(new MedicoDTO(cirujano, "Derma", "ITC", TipoEspecialidad.DERMATOLOGIA, 20, 6, true));
        paciente = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(paciente, "AF-10020", "ITC-" + paciente, "Paciente", "Cirugia",
                LocalDate.of(1955, 1, 1), false)));
        otroPaciente = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(otroPaciente, "AF-10020", "ITC-" + otroPaciente, "Otro", "Cirugia",
                LocalDate.of(1990, 1, 1), false)));
        quirofano = quirofanos.nextIdentity();
        assertTrue(quirofanos.store(new QuirofanoDTO(quirofano, "IT-" + quirofano, TipoEspecialidad.DERMATOLOGIA,
                LocalTime.of(7, 0), LocalTime.of(19, 0), 30, true)));
        ficha = fichas.nextIdentity();
        fichas.insertar(new FichaInternaDTO(ficha, TipoEspecialidad.DERMATOLOGIA, false, paciente, "", "", null, null,
                derivante, "", TipoEspecialidad.CONSULTORIO_FAMILIAR, cirujano, null, null, null, LUNES,
                LocalTime.of(8, 0), 20, Gravedad.ALTA, false, true, ficha, "Lesion", null, EstadoFichaInterna.ATENDIDA,
                LocalDateTime.of(LUNES, LocalTime.of(7, 0))));
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from notificacion.notificacion where paciente_id in (?, ?)", paciente, otroPaciente);
            borrar(conn, "delete from quirofano.cirugia where quirofano_id = ?", quirofano);
            borrar(conn, "delete from quirofano.quirofano where id = ?", quirofano);
            borrar(conn, "delete from fichas_internas.dermatologia where id = ?", ficha);
            borrar(conn, "delete from identidad.paciente where id in (?, ?)", paciente, otroPaciente);
            borrar(conn, "delete from recursos.medico where id in (?, ?)", derivante, cirujano);
        }
    }

    @Test
    void laBaseImpideChoquesDeQuirofanoConSuLimpieza_yLasCanceladasNoOcupan() {
        LocalDateTime nueve = LocalDateTime.of(LUNES.plusDays(1), LocalTime.of(9, 0));
        long primera = cirugias.nextIdentity();
        assertTrue(cirugias.insertar(cirugia(primera, paciente, cirujano, nueve, 60)));

        long otraId = cirugias.nextIdentity();
        assertFalse(cirugias.insertar(cirugia(otraId, otroPaciente, derivante, nueve.plusMinutes(80), 30)),
                "10:20 cae dentro de la limpieza (10:00 a 10:30)");

        assertTrue(cirugias.cambiarEstado(primera, EstadoCirugia.PROGRAMADA, EstadoCirugia.CANCELADA));
        assertTrue(cirugias.insertar(cirugia(otraId, otroPaciente, derivante, nueve.plusMinutes(80), 30)),
                "una CANCELADA no ocupa el quirofano");
        CirugiaDTO leida = cirugias.porId(otraId).orElseThrow();
        assertEquals("IT-" + quirofano, leida.nombreQuirofano());
        assertEquals(nueve.plusMinutes(140), leida.finConLimpieza());
    }

    @Test
    void proponerConfirmarYChoqueAlConfirmarDosVeces() {
        ProgramacionCirugiaService service = new ProgramacionCirugiaService(cirugias, fichas, soloElDeLaPrueba(),
                medicos, new PgCitaRepository(), new PgDiaNoHabilRepository(), new PgCambioFichaRepository(),
                new PgNotificacionPublicador(), new PgTransaccion(), reloj);
        CirugiaDTOs.Solicitud s = new CirugiaDTOs.Solicitud(ficha, 45, Gravedad.ALTA, false);

        CirugiaDTOs.Propuesta p = service.proponer(s, cirujano);
        CirugiaDTOs.Confirmacion c = new CirugiaDTOs.Confirmacion(ficha, 45, Gravedad.ALTA, false, p.quirofanoId(),
                p.fecha(), LocalTime.parse(p.inicio()), null, null, null);
        CirugiaDTOs.Vista guardada = service.confirmar(c, cirujano);

        assertEquals(LUNES.plusDays(1), guardada.fecha());
        assertEquals("07:00", guardada.inicio());
        DominioException choque = assertThrows(DominioException.class, () -> service.confirmar(c, cirujano));
        assertEquals("CHOQUE_DE_HORARIO", choque.getCodigo());
    }

    /** Las pruebas solo usan su quirofano (en la base puede haber otros de Dermatologia). */
    private com.Hospital.CNS.cns_backend.model.recursos.QuirofanoRepository soloElDeLaPrueba() {
        return new com.Hospital.CNS.cns_backend.model.recursos.QuirofanoRepository() {
            @Override
            public int nextIdentity() {
                return quirofanos.nextIdentity();
            }

            @Override
            public boolean store(QuirofanoDTO q) {
                return quirofanos.store(q);
            }

            @Override
            public java.util.Optional<QuirofanoDTO> porId(int id) {
                return quirofanos.porId(id);
            }

            @Override
            public java.util.List<QuirofanoDTO> listar() {
                return quirofanos.porId(quirofano).stream().toList();
            }

            @Override
            public java.util.List<QuirofanoDTO> activosDe(TipoEspecialidad e) {
                return quirofanos.porId(quirofano).stream().toList();
            }
        };
    }

    private CirugiaDTO cirugia(long id, long pacienteId, long cirujanoId, LocalDateTime inicio, int duracion) {
        return new CirugiaDTO(id, ficha, pacienteId, "", "", null, cirujanoId, "", quirofano, "",
                TipoEspecialidad.DERMATOLOGIA, inicio, duracion, 30, Gravedad.MEDIA, false, false, id,
                EstadoCirugia.PROGRAMADA, LocalDateTime.of(LUNES, LocalTime.of(8, 0)));
    }

    private static void borrar(Connection conn, String sql, long... ids) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                stmt.setLong(i + 1, ids[i]);
            }
            stmt.executeUpdate();
        }
    }
}
