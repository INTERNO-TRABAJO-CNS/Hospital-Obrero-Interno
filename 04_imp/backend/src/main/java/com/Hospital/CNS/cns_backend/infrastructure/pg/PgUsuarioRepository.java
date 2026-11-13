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
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioDTO;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioRepository;

/**
 * UsuarioRepository sobre PostgreSQL (tabla identidad.usuario, migracion V2). La clave se guarda cifrada.
 * Una cuenta MEDICO solo cuenta como activa si su medico sigue activo en recursos.medico.
 */
public class PgUsuarioRepository implements UsuarioRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgUsuarioRepository.class);
    private static final String VIOLACION_UNIQUE = "23505";
    private static final String SELECCION = "select u.id, u.usuario, u.clave_cifrada, u.rol, u.persona_id, u.nombre, "
            + "(u.activo and (u.rol <> 'MEDICO' or exists (select 1 from recursos.medico m "
            + "where m.id = u.persona_id and m.activo))) as activo from identidad.usuario u";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('identidad.usuario_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el id del usuario", e);
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
            throw ErroresPg.traducir(LOG, "guardar el usuario " + u.usuarioId(), e);
        }
    }

    @Override
    public Optional<UsuarioDTO> porUsuario(String usuario) {
        List<UsuarioDTO> r = consultar(SELECCION + " where lower(u.usuario) = lower(?)", usuario);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public Optional<UsuarioDTO> porId(long usuarioId) {
        List<UsuarioDTO> r = consultar(SELECCION + " where u.id = ?", usuarioId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public List<UsuarioDTO> listar() {
        return consultar(SELECCION + " order by u.rol, lower(u.usuario)");
    }

    @Override
    public boolean existeActivoConRol(RolUsuario rol) {
        String sql = "select 1 from identidad.usuario where rol = ? and activo limit 1";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rol.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "buscar usuarios con rol " + rol, e);
        }
    }

    private List<UsuarioDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<UsuarioDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new UsuarioDTO(rs.getLong("id"), rs.getString("usuario"), rs.getString("clave_cifrada"),
                            RolUsuario.valueOf(rs.getString("rol")), rs.getLong("persona_id"), rs.getString("nombre"),
                            rs.getBoolean("activo")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "buscar usuarios", e);
        }
    }
}
