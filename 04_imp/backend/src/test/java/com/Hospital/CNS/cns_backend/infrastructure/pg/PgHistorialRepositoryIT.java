package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;

/**
 * Integracion contra PostgreSQL (no corre con {@code ./mvnw test}; ejecutar con
 * {@code ./mvnw test -Dtest=PgHistorialRepositoryIT}). Aplica V2 y V3 (idempotentes), usa un paciente propio y
 * lo borra al terminar. Si no hay base de datos, la prueba se omite.
 */
class PgHistorialRepositoryIT {

    private final PgHistorialRepository repo = new PgHistorialRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();
    private long pacienteId;

    @BeforeEach
    void preparar() throws Exception {
        try (Connection conn = DB.getConnection(); Statement st = conn.createStatement()) {
            st.execute(script("db/migration/V2__identidad_afiliados_pacientes.sql"));
            st.execute(script("db/migration/V3__historial_receta.sql"));
        } catch (IllegalStateException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        pacienteId = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(pacienteId, "AF-10020", "IT-" + pacienteId, "Prueba", "Integracion",
                LocalDate.of(1990, 1, 1), false)));
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (pacienteId == 0) {
            return;
        }
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from clinico.receta where paciente_id = ?");
            borrar(conn, "delete from clinico.historial where paciente_id = ?");
            borrar(conn, "delete from identidad.paciente where id = ?");
        }
    }

    @Test
    void IT_agregarRecetaYLeer() {
        LocalDateTime cuando = LocalDateTime.of(2026, 11, 10, 8, 40);
        RecetaDTO guardada = repo.agregarReceta(new RecetaDTO(0, 555, pacienteId, "Paracetamol 500 mg", cuando, 2));

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
                new RecetaDTO(0, 556, pacienteId, "Reposo", LocalDateTime.of(2026, 11, 10, 9, 0), 3));

        repo.eliminarReceta(guardada.recetaId());

        assertTrue(repo.porPaciente(pacienteId).orElseThrow().atenciones().isEmpty());
    }

    @Test
    void IT_crearSiNoExiste_esIdempotente() {
        repo.crearSiNoExiste(pacienteId);
        repo.crearSiNoExiste(pacienteId);

        assertTrue(repo.porPaciente(pacienteId).isPresent());
    }

    private void borrar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, pacienteId);
            st.executeUpdate();
        }
    }

    private static String script(String ruta) throws IOException {
        try (InputStream in = PgHistorialRepositoryIT.class.getClassLoader().getResourceAsStream(ruta)) {
            if (in == null) {
                throw new IOException("No se encontro " + ruta);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
