package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.AtencionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialDTO;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;

/**
 * HistorialRepository sobre PostgreSQL (tablas clinico.historial y clinico.receta, migracion V3).
 *
 * <p>Cada receta guarda su cita; las recetas de una misma cita forman una atencion. La fecha y hora de
 * la atencion salen de la receta; la especialidad y el medico los completa HistorialService con la cita
 * (la tabla de citas es de T100-03). El nombre del paciente sale de identidad.paciente (V2).</p>
 */
public class PgHistorialRepository implements HistorialRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgHistorialRepository.class);

    @Override
    public void crearSiNoExiste(long pacienteId) {
        String sql = "insert into clinico.historial (paciente_id) values (?) on conflict (paciente_id) do nothing";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pacienteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw error("crear el historial del paciente " + pacienteId, e);
        }
    }

    @Override
    public Optional<HistorialDTO> porPaciente(long pacienteId) {
        String sqlHistorial = "select h.paciente_id, coalesce(p.nombres || ' ' || p.apellidos, '') as nombre "
                + "from clinico.historial h left join identidad.paciente p on p.id = h.paciente_id "
                + "where h.paciente_id = ?";
        String sqlRecetas = "select id, cita_id, paciente_id, medicacion, fecha_hora, numero_ficha "
                + "from clinico.receta where paciente_id = ? order by fecha_hora, id";
        try (Connection conn = DB.getConnection()) {
            String nombre;
            try (PreparedStatement stmt = conn.prepareStatement(sqlHistorial)) {
                stmt.setLong(1, pacienteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    nombre = rs.getString("nombre");
                }
            }
            Map<Long, List<RecetaDTO>> porCita = new LinkedHashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecetas)) {
                stmt.setLong(1, pacienteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        RecetaDTO r = mapearReceta(rs);
                        porCita.computeIfAbsent(r.citaId(), k -> new ArrayList<>()).add(r);
                    }
                }
            }
            List<AtencionDTO> atenciones = new ArrayList<>();
            for (List<RecetaDTO> recetas : porCita.values()) {
                RecetaDTO primera = recetas.get(0);
                atenciones.add(new AtencionDTO(primera.fechaHora().toLocalDate(), primera.fechaHora().toLocalTime(),
                        "", "", List.copyOf(recetas)));
            }
            return Optional.of(new HistorialDTO(pacienteId, nombre.trim(), atenciones));
        } catch (SQLException e) {
            throw error("leer el historial del paciente " + pacienteId, e);
        }
    }

    /** Crea el historial si no existe (igual que el Fake) y devuelve la receta con su id. */
    @Override
    public RecetaDTO agregarReceta(RecetaDTO r) {
        String sql = "insert into clinico.receta (id, cita_id, paciente_id, medicacion, fecha_hora, numero_ficha) "
                + "values (nextval('clinico.receta_id_seq'), ?, ?, ?, ?, ?) returning id";
        crearSiNoExiste(r.pacienteId());
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, r.citaId());
            stmt.setLong(2, r.pacienteId());
            stmt.setString(3, r.medicacion());
            stmt.setTimestamp(4, Timestamp.valueOf(r.fechaHora()));
            stmt.setInt(5, r.numeroFicha());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return new RecetaDTO(rs.getLong(1), r.citaId(), r.pacienteId(), r.medicacion(), r.fechaHora(),
                        r.numeroFicha());
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "guardar la receta de la cita " + r.citaId(), e);
        }
    }

    @Override
    public void eliminarReceta(long recetaId) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("delete from clinico.receta where id = ?")) {
            stmt.setLong(1, recetaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw error("quitar la receta " + recetaId, e);
        }
    }

    private static RecetaDTO mapearReceta(ResultSet rs) throws SQLException {
        return new RecetaDTO(rs.getLong("id"), rs.getLong("cita_id"), rs.getLong("paciente_id"),
                rs.getString("medicacion"), rs.getTimestamp("fecha_hora").toLocalDateTime(), rs.getInt("numero_ficha"));
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
