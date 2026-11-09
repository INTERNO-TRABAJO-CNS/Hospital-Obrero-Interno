package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilDTO;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;

/** DiaNoHabilRepository sobre PostgreSQL (tabla recursos.dia_no_habil). */
public class PgDiaNoHabilRepository implements DiaNoHabilRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgDiaNoHabilRepository.class);

    private static final String SELECCION = "select id, fecha, descripcion, activo from recursos.dia_no_habil";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('recursos.dia_no_habil_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw error("generar el id del dia no habil", e);
        }
    }

    @Override
    public void store(DiaNoHabilDTO d) {
        String sql = "insert into recursos.dia_no_habil (id, fecha, descripcion, activo) values (?, ?, ?, ?) "
                + "on conflict (id) do update set fecha = excluded.fecha, "
                + "descripcion = excluded.descripcion, activo = excluded.activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, d.diaNoHabilId());
            stmt.setObject(2, d.fecha());
            stmt.setString(3, d.descripcion());
            stmt.setBoolean(4, d.activo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw error("guardar el dia no habil " + d.diaNoHabilId(), e);
        }
    }

    @Override
    public Optional<DiaNoHabilDTO> porId(long diaNoHabilId) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECCION + " where id = ?")) {
            stmt.setLong(1, diaNoHabilId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar el dia no habil " + diaNoHabilId, e);
        }
    }

    @Override
    public List<DiaNoHabilDTO> listar() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECCION + " order by fecha, id");
             ResultSet rs = stmt.executeQuery()) {
            List<DiaNoHabilDTO> dias = new ArrayList<>();
            while (rs.next()) {
                dias.add(mapear(rs));
            }
            return dias;
        } catch (SQLException e) {
            throw error("listar los dias no habiles", e);
        }
    }

    @Override
    public boolean existeActivoEnFecha(LocalDate fecha) {
        String sql = "select 1 from recursos.dia_no_habil where activo and fecha = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw error("comprobar la fecha del dia no habil", e);
        }
    }

    private static DiaNoHabilDTO mapear(ResultSet rs) throws SQLException {
        return new DiaNoHabilDTO(rs.getLong("id"), rs.getObject("fecha", LocalDate.class),
                rs.getString("descripcion"), rs.getBoolean("activo"));
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
