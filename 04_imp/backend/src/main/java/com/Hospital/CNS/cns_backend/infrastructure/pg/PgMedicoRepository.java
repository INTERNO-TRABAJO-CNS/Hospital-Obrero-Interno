package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/** MedicoRepository sobre PostgreSQL (tabla recursos.medico). Es lo unico que conoce ese SQL. */
public class PgMedicoRepository implements MedicoRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgMedicoRepository.class);

    private static final String COLUMNAS =
            "id, nombres, apellidos, especialidad, duracion_cita_min, cupo_maximo_dia, activo";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('recursos.medico_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw error("generar el id del medico", e);
        }
    }

    @Override
    public void store(MedicoDTO m) {
        String sql = "insert into recursos.medico (" + COLUMNAS + ") values (?, ?, ?, ?, ?, ?, ?) "
                + "on conflict (id) do update set nombres = excluded.nombres, apellidos = excluded.apellidos, "
                + "especialidad = excluded.especialidad, duracion_cita_min = excluded.duracion_cita_min, "
                + "cupo_maximo_dia = excluded.cupo_maximo_dia, activo = excluded.activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, m.medicoId());
            stmt.setString(2, m.nombres());
            stmt.setString(3, m.apellidos());
            stmt.setString(4, m.especialidad().name());
            stmt.setInt(5, m.duracionCitaMin());
            stmt.setInt(6, m.cupoMaximoDia());
            stmt.setBoolean(7, m.activo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "guardar el medico " + m.medicoId(), e);
        }
    }

    @Override
    public Optional<MedicoDTO> porId(long medicoId) {
        String sql = "select " + COLUMNAS + " from recursos.medico where id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, medicoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar el medico " + medicoId, e);
        }
    }

    @Override
    public List<MedicoDTO> listar(TipoEspecialidad filtro) {
        String sql = "select " + COLUMNAS + " from recursos.medico"
                + (filtro == null ? "" : " where especialidad = ?") + " order by id";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtro != null) {
                stmt.setString(1, filtro.name());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<MedicoDTO> medicos = new ArrayList<>();
                while (rs.next()) {
                    medicos.add(mapear(rs));
                }
                return medicos;
            }
        } catch (SQLException e) {
            throw error("listar los medicos", e);
        }
    }

    private static MedicoDTO mapear(ResultSet rs) throws SQLException {
        return new MedicoDTO(rs.getLong("id"), rs.getString("nombres"), rs.getString("apellidos"),
                TipoEspecialidad.valueOf(rs.getString("especialidad")), rs.getInt("duracion_cita_min"),
                rs.getInt("cupo_maximo_dia"), rs.getBoolean("activo"));
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
