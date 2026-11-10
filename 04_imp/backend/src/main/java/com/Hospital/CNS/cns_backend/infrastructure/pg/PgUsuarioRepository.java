package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioDTO;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioRepository;

/** UsuarioRepository sobre PostgreSQL (tabla identidad.usuario, migracion V2). La clave se guarda cifrada. */
public class PgUsuarioRepository implements UsuarioRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgUsuarioRepository.class);
    private static final String VIOLACION_UNIQUE = "23505";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('identidad.usuario_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw error("generar el id del usuario", e);
        }
    }

    @Override
    public boolean store(UsuarioDTO u) {
        String sql = "insert into identidad.usuario (id, usuario, clave_cifrada, rol, persona_id, nombre, activo) "
                + "values (?, ?, ?, ?, ?, ?, ?) on conflict (id) do update set clave_cifrada = excluded.clave_cifrada, "
                + "nombre = excluded.nombre, activo = excluded.activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, u.usuarioId());
            stmt.setString(2, u.usuario());
            stmt.setString(3, u.claveCifrada());
            stmt.setString(4, u.rol().name());
            stmt.setLong(5, u.personaId());
            stmt.setString(6, u.nombre());
            stmt.setBoolean(7, u.activo());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (VIOLACION_UNIQUE.equals(e.getSQLState())) {
                return false;
            }
            throw error("guardar el usuario " + u.usuarioId(), e);
        }
    }

    @Override
    public Optional<UsuarioDTO> porUsuario(String usuario) {
        String sql = "select id, usuario, clave_cifrada, rol, persona_id, nombre, activo "
                + "from identidad.usuario where lower(usuario) = lower(?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UsuarioDTO(rs.getLong("id"), rs.getString("usuario"),
                        rs.getString("clave_cifrada"), RolUsuario.valueOf(rs.getString("rol")),
                        rs.getLong("persona_id"), rs.getString("nombre"), rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            throw error("buscar el usuario", e);
        }
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
