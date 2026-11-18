package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * Integracion contra PostgreSQL (no corre con {@code ./mvnw test}; ejecutar con
 * {@code ./mvnw test -Dtest=PgHistorialRepositoryIT}). Necesita las migraciones V1..V6 (las aplica Flyway al
 * arrancar el backend). Desde V5 cada receta debe apuntar a una cita real: la prueba crea medico, consultorio,
 * paciente y citas propios y los borra al terminar. Si no hay base de datos, la prueba se omite.
 */
class PgHistorialRepositoryIT {

    private final PgHistorialRepository repo = new PgHistorialRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();
    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgCitaRepository citas = new PgCitaRepository();

    private long pacienteId;
    private long medicoId;
    private int consultorioId;
    private long citaA;
    private long citaB;

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        pacienteId = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(pacienteId, "AF-10020", "IT-" + pacienteId, "Prueba", "Integracion",
                LocalDate.of(1990, 1, 1), false)));
        medicoId = medicos.nextIdentity();
        medicos.store(new MedicoDTO(medicoId, "Medico", "IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6, true));
        consultorioId = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorioId, "Consultorio IT " + consultorioId, true));
        citaA = nuevaCita(LocalTime.of(8, 40), 2);
        citaB = nuevaCita(LocalTime.of(9, 20), 3);
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (pacienteId == 0) {
            return;
        }
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from clinico.receta where paciente_id = ?", pacienteId);
            borrar(conn, "delete from clinico.historial where paciente_id = ?", pacienteId);
            borrar(conn, "delete from citas.cita where paciente_id = ?", pacienteId);
            borrar(conn, "delete from identidad.paciente where id = ?", pacienteId);
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorioId);
            borrar(conn, "delete from recursos.medico where id = ?", medicoId);
        }
    }

    @Test
    void IT_agregarRecetaYLeer() {
        LocalDateTime cuando = LocalDateTime.of(2026, 11, 10, 8, 40);
        RecetaDTO guardada = repo.agregarReceta(new RecetaDTO(0, citaA, pacienteId, "Paracetamol 500 mg", cuando, 2));

        HistorialDTO h = repo.porPaciente(pacienteId).orElseThrow();

        assertTrue(guardada.recetaId() > 0);
        assertEquals("Prueba Integracion", h.nombrePaciente());
        assertEquals(1, h.atenciones().size());
        assertEquals(cuando.toLocalDate(), h.atenciones().get(0).fecha());
        assertEquals("Paracetamol 500 mg", h.atenciones().get(0).recetas().get(0).medicacion());
    }

    @Test
    void IT_eliminarReceta_compensacion() {
        RecetaDTO guardada = repo.agregarReceta(
                new RecetaDTO(0, citaB, pacienteId, "Reposo", LocalDateTime.of(2026, 11, 10, 9, 0), 3));

        repo.eliminarReceta(guardada.recetaId());

        assertTrue(repo.porPaciente(pacienteId).orElseThrow().atenciones().isEmpty());
    }

    @Test
    void IT_recetaDeUnaCitaInexistente_laRechazaLaFK() {
        DominioException e = assertThrows(DominioException.class, () -> repo.agregarReceta(
                new RecetaDTO(0, Long.MAX_VALUE, pacienteId, "Reposo", LocalDateTime.of(2026, 11, 10, 9, 0), 3)));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void IT_crearSiNoExiste_esIdempotente() {
        repo.crearSiNoExiste(pacienteId);
        repo.crearSiNoExiste(pacienteId);

        assertTrue(repo.porPaciente(pacienteId).isPresent());
    }

    private long nuevaCita(LocalTime hora, int ficha) {
        long id = citas.nextIdentity();
        assertTrue(citas.store(new CitaDTO(id, pacienteId, "", "", medicoId, "", TipoEspecialidad.CONSULTORIO_FAMILIAR,
                consultorioId, LocalDate.of(2026, 11, 10), hora, ficha, EstadoCita.ASISTENCIA_CONFIRMADA)));
        return id;
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, id);
            st.executeUpdate();
        }
    }
}
