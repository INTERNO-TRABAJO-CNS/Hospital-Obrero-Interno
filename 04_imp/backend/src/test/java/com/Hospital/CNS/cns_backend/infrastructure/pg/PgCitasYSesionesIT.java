package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.SesionGuardada;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * Integracion contra PostgreSQL de PgCitaRepository, PgSesionRepository, PgUsuarioRepository y PgTransaccion
 * (no corre con {@code ./mvnw test}; ejecutar con {@code ./mvnw test -Dtest=PgCitasYSesionesIT}). Necesita las
 * migraciones V1..V6 aplicadas (las aplica Flyway al arrancar el backend). Crea sus propios datos y los borra.
 * Si no hay base de datos, la prueba se omite.
 */
class PgCitasYSesionesIT {

    private static final LocalDate DIA = LocalDate.of(2031, 3, 3);

    private final PgCitaRepository citas = new PgCitaRepository();
    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();
    private final PgUsuarioRepository usuarios = new PgUsuarioRepository();
    private final PgSesionRepository sesiones = new PgSesionRepository();

    private long medicoId;
    private int consultorioId;
    private long pacienteId;
    private long usuarioId;

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        medicoId = medicos.nextIdentity();
        medicos.store(new MedicoDTO(medicoId, "Prueba", "IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6, true));
        consultorioId = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorioId, "Consultorio IT " + consultorioId, true));
        pacienteId = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(pacienteId, "AF-10020", "IT-" + pacienteId, "Paciente", "Prueba",
                LocalDate.of(1990, 1, 1), false)));
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from identidad.sesion where usuario_id = ?", usuarioId);
            borrar(conn, "delete from identidad.usuario where id = ?", usuarioId);
            borrar(conn, "delete from citas.cita where medico_id = ?", medicoId);
            borrar(conn, "delete from identidad.paciente where id = ?", pacienteId);
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorioId);
            borrar(conn, "delete from recursos.medico where id = ?", medicoId);
        }
    }

    @Test
    void cita_guardarLeerContarYCambiarEstado() {
        long id = citas.nextIdentity();
        assertTrue(citas.store(cita(id, LocalTime.of(8, 0), EstadoCita.PROGRAMADA)));

        CitaDTO leida = citas.porId(id).orElseThrow();
        assertEquals("Paciente Prueba", leida.nombrePaciente());
        assertEquals("AF-10020", leida.numeroAfiliado());
        assertEquals("Prueba IT", leida.nombreMedico());
        assertEquals(1, citas.delMedicoEnFecha(medicoId, DIA).size());
        assertEquals(1, citas.contarProgramadasDelMedico(medicoId));
        assertEquals(1, citas.contarProgramadasEnConsultorio(consultorioId));
        assertTrue(citas.medicoTienePacienteEnAgenda(medicoId, pacienteId));
        assertTrue(citas.pacienteTieneCitaEnFecha(pacienteId, DIA));
        assertEquals(1, citas.programadasHasta(DIA.atTime(8, 0)).stream().filter(c -> c.citaId() == id).count());

        assertFalse(citas.registrarFin(id, LocalDateTime.now()), "sin asistencia confirmada no se finaliza");
        assertTrue(citas.cambiarEstado(id, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA));
        assertFalse(citas.cambiarEstado(id, EstadoCita.PROGRAMADA, EstadoCita.CANCELADA), "el UPDATE es atomico");
        assertTrue(citas.registrarFin(id, LocalDateTime.now()));
        assertEquals(EstadoCita.ATENDIDA, citas.porId(id).orElseThrow().estado());
    }

    @Test
    void cita_mismoMedicoMismaHora_rechaza_salvoCancelada() {
        long primera = citas.nextIdentity();
        assertTrue(citas.store(cita(primera, LocalTime.of(9, 0), EstadoCita.PROGRAMADA)));

        assertFalse(citas.store(cita(citas.nextIdentity(), LocalTime.of(9, 0), EstadoCita.PROGRAMADA)));

        assertTrue(citas.cambiarEstado(primera, EstadoCita.PROGRAMADA, EstadoCita.CANCELADA));
        assertTrue(citas.store(cita(citas.nextIdentity(), LocalTime.of(9, 0), EstadoCita.PROGRAMADA)));
    }

    @Test
    void transaccion_siFallaUnPaso_noQuedaNinguno() {
        long id = citas.nextIdentity();

        assertThrows(IllegalStateException.class, () -> new PgTransaccion().ejecutar(() -> {
            citas.store(cita(id, LocalTime.of(10, 0), EstadoCita.PROGRAMADA));
            throw new IllegalStateException("falla a proposito");
        }));

        assertTrue(citas.porId(id).isEmpty());
    }

    @Test
    void usuarioDeMedicoDadoDeBaja_noEstaActivo_ySesionSeCierra() {
        usuarioId = usuarios.nextIdentity();
        assertTrue(usuarios.store(new UsuarioDTO(usuarioId, "it-medico-" + usuarioId, "x", RolUsuario.MEDICO, medicoId,
                "Prueba IT", true)));
        assertTrue(usuarios.porId(usuarioId).orElseThrow().activo());

        medicos.store(new MedicoDTO(medicoId, "Prueba", "IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6, false));
        assertFalse(usuarios.porId(usuarioId).orElseThrow().activo());

        LocalDateTime ahora = LocalDateTime.of(2031, 3, 3, 8, 0);
        String hash = "a".repeat(64);
        sesiones.guardar(new SesionGuardada(hash, usuarioId, ahora, ahora.plusHours(8), null));
        assertFalse(sesiones.porTokenHash(hash).orElseThrow().cerrada());
        sesiones.cerrarTodasDelUsuario(usuarioId, ahora.plusHours(1));
        assertTrue(sesiones.porTokenHash(hash).orElseThrow().cerrada());
    }

    private CitaDTO cita(long id, LocalTime hora, EstadoCita estado) {
        return new CitaDTO(id, pacienteId, "", "", medicoId, "", TipoEspecialidad.CONSULTORIO_FAMILIAR, consultorioId,
                DIA, hora, 1, estado);
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
