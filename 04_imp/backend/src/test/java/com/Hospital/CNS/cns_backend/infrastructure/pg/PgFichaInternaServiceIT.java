package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.infrastructure.ws.NotificacionesWs;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.fichasinternas.AsignadorFichaInterna;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaService;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaVistaDTO;
import com.Hospital.CNS.cns_backend.model.fichasinternas.SolicitudFichaInterna;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import tools.jackson.databind.ObjectMapper;

/**
 * CU-21/22/23 de punta a punta contra PostgreSQL: FichaInternaService con todos los Pg*Repository y la
 * transaccion real. Usa Nefrologia y una fecha lejana (lunes 03/03/2031) para no cruzarse con los datos de
 * prueba. Ejecutar con {@code ./mvnw test -Dtest=PgFichaInternaServiceIT} (necesita la migracion V7).
 */
class PgFichaInternaServiceIT {

    private static final LocalDate LUNES = LocalDate.of(2031, 3, 3);

    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();
    private final PgCitaRepository citas = new PgCitaRepository();
    private final PgHorarioRepository horarios = new PgHorarioRepository();
    private final PgFichaInternaRepository fichas = new PgFichaInternaRepository();
    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(LUNES, LocalTime.of(9, 0)));

    private long derivante;
    private long especialista;
    private int consultorio;
    private long paciente;
    private long cita;
    private FichaInternaService service;

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        derivante = medicos.nextIdentity();
        medicos.store(new MedicoDTO(derivante, "General", "IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6, true));
        especialista = medicos.nextIdentity();
        medicos.store(new MedicoDTO(especialista, "Nefro", "IT", TipoEspecialidad.NEFROLOGIA, 30, 5, true));
        consultorio = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorio, "Consultorio IT " + consultorio, true));
        for (DayOfWeek d : DayOfWeek.values()) {
            if (d.getValue() <= 5) {
                horarios.store(new HorarioDTO(horarios.nextIdentity(), especialista, consultorio, d,
                        LocalTime.of(8, 0), LocalTime.of(10, 0), true));
            }
        }
        paciente = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(paciente, "AF-10020", "ITS-" + paciente, "Paciente", "Nefro",
                LocalDate.of(1950, 6, 1), false)));
        cita = citas.nextIdentity();
        assertTrue(citas.store(new CitaDTO(cita, paciente, "", "", derivante, "", TipoEspecialidad.CONSULTORIO_FAMILIAR,
                consultorio, LUNES, LocalTime.of(8, 0), 1, EstadoCita.ASISTENCIA_CONFIRMADA)));
        AsignadorFichaInterna asignador = new AsignadorFichaInterna(fichas, medicos, horarios, consultorios, citas,
                new PgDiaNoHabilRepository(), null, reloj);
        service = new FichaInternaService(fichas, citas, medicos, pacientes, asignador, new PgCambioFichaRepository(),
                new PgNotificacionPublicador(new NotificacionesWs(new ObjectMapper())), new PgTransaccion(), reloj);
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from fichas_internas.cambio where objeto = 'FICHA' and objeto_id in "
                    + "(select id from fichas_internas.nefrologia where paciente_id = ?)", paciente);
            borrar(conn, "delete from notificacion.notificacion where paciente_id = ?", paciente);
            borrar(conn, "delete from fichas_internas.derivacion where cita_origen_id = ?", cita);
            borrar(conn, "delete from fichas_internas.nefrologia where paciente_id = ?", paciente);
            borrar(conn, "delete from citas.cita where id = ?", cita);
            borrar(conn, "delete from identidad.paciente where id = ?", paciente);
            borrar(conn, "delete from recursos.horario_medico where medico_id = ?", especialista);
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorio);
            borrar(conn, "delete from recursos.medico where id = ?", derivante);
            borrar(conn, "delete from recursos.medico where id = ?", especialista);
        }
    }

    @Test
    void generarAsignarAvisarYAtender() throws SQLException {
        SolicitudFichaInterna s = new SolicitudFichaInterna(cita, null, TipoEspecialidad.NEFROLOGIA, Gravedad.ALTA,
                false, "Creatinina alta");

        FichaInternaVistaDTO f = service.generar(s, derivante);

        assertEquals(EstadoFichaInterna.ASIGNADA, f.estado());
        assertEquals(LUNES.plusDays(1), f.fecha());
        assertEquals("08:00", f.hora());
        assertEquals("Nefro IT", f.medico());
        assertEquals(Gravedad.ALTA, f.gravedad());
        assertEquals(1, contar("select count(*) from notificacion.notificacion where ficha_id = ? "
                + "and tipo = 'FICHA_INTERNA_ASIGNADA'", f.ticket()));
        assertTrue(contar("select count(*) from fichas_internas.cambio where objeto_id = ?", f.ticket()) >= 2);

        DominioException otra = assertThrows(DominioException.class, () -> service.generar(s, derivante));
        assertEquals("CITA_YA_DERIVADA", otra.getCodigo());

        reloj.fijar(LocalDateTime.of(LUNES.plusDays(1), LocalTime.of(8, 10)));
        FichaInternaVistaDTO enAgenda = service.agendaEspecialista(especialista, LUNES.plusDays(1)).get(0);
        assertNull(enAgenda.gravedad(), "el especialista no ve la gravedad");
        assertEquals(EstadoFichaInterna.ATENDIDA,
                service.marcarAtendida(f.ticket(), especialista, "Control en un mes").estado());
        assertEquals(1, service.delPaciente(paciente, especialista).size());
    }

    private static int contar(String sql, long id) throws SQLException {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
