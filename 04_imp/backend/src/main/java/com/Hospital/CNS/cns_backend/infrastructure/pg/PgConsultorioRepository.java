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
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;

/** ConsultorioRepository sobre PostgreSQL (tabla recursos.consultorio). */
public class PgConsultorioRepository implements ConsultorioRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgConsultorioRepository.class);

    @Override
    public int nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('recursos.consultorio_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw error("generar el id del consultorio", e);
        }
    }

    @Override
    public void store(ConsultorioDTO c) {
        String sql = "insert into recursos.consultorio (id, nombre, activo) values (?, ?, ?) "
                + "on conflict (id) do update set nombre = excluded.nombre, activo = excluded.activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, c.consultorioId());
            stmt.setString(2, c.nombre());
            stmt.setBoolean(3, c.activo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "guardar el consultorio " + c.consultorioId(), e,
                    "CONSULTORIO_DUPLICADO", "Ya existe un consultorio con ese nombre");
        }
    }

    @Override
    public Optional<ConsultorioDTO> porId(int consultorioId) {
        String sql = "select id, nombre, activo from recursos.consultorio where id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, consultorioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar el consultorio " + consultorioId, e);
        }
    }

    @Override
    public List<ConsultorioDTO> listar() {
        String sql = "select id, nombre, activo from recursos.consultorio order by id";
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<ConsultorioDTO> consultorios = new ArrayList<>();
            while (rs.next()) {
                consultorios.add(mapear(rs));
            }
            return consultorios;
        } catch (SQLException e) {
            throw error("listar los consultorios", e);
        }
    }

    @Override
    public boolean existeActivoConNombre(String nombre) {
        String sql = "select 1 from recursos.consultorio where activo and lower(nombre) = lower(?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw error("comprobar el nombre del consultorio", e);
        }
    }

    private static ConsultorioDTO mapear(ResultSet rs) throws SQLException {
        return new ConsultorioDTO(rs.getInt("id"), rs.getString("nombre"), rs.getBoolean("activo"));
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
