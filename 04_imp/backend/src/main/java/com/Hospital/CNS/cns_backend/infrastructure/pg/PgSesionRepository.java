package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.identidad.SesionGuardada;
import com.Hospital.CNS.cns_backend.model.identidad.SesionRepository;

/** SesionRepository sobre PostgreSQL (tabla identidad.sesion, migracion V6). Solo guarda el hash del token. */
public class PgSesionRepository implements SesionRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgSesionRepository.class);

    @Override
    public void guardar(SesionGuardada s) {
        String sql = "insert into identidad.sesion (token_hash, usuario_id, creada_en, vence_en, cerrada_en) "
                + "values (?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.tokenHash());
            stmt.setLong(2, s.usuarioId());
            stmt.setTimestamp(3, Timestamp.valueOf(s.creadaEn()));
            stmt.setTimestamp(4, Timestamp.valueOf(s.venceEn()));
            stmt.setTimestamp(5, s.cerradaEn() == null ? null : Timestamp.valueOf(s.cerradaEn()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "guardar la sesion del usuario " + s.usuarioId(), e);
        }
    }

    @Override
    public Optional<SesionGuardada> porTokenHash(String tokenHash) {
        String sql = "select token_hash, usuario_id, creada_en, vence_en, cerrada_en from identidad.sesion "
                + "where token_hash = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tokenHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Timestamp cerrada = rs.getTimestamp("cerrada_en");
                return Optional.of(new SesionGuardada(rs.getString("token_hash"), rs.getLong("usuario_id"),
                        rs.getTimestamp("creada_en").toLocalDateTime(), rs.getTimestamp("vence_en").toLocalDateTime(),
                        cerrada == null ? null : cerrada.toLocalDateTime()));
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "buscar la sesion", e);
        }
    }

    @Override
    public void cerrar(String tokenHash, LocalDateTime cuando) {
        actualizar("update identidad.sesion set cerrada_en = ? where token_hash = ? and cerrada_en is null",
                Timestamp.valueOf(cuando), tokenHash);
    }

    @Override
    public void cerrarTodasDelUsuario(long usuarioId, LocalDateTime cuando) {
        actualizar("update identidad.sesion set cerrada_en = ? where usuario_id = ? and cerrada_en is null",
                Timestamp.valueOf(cuando), usuarioId);
    }

    @Override
    public int eliminarVencidas(LocalDateTime antesDe) {
        return actualizar("delete from identidad.sesion where vence_en < ?", Timestamp.valueOf(antesDe));
    }

    private int actualizar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "actualizar sesiones", e);
        }
    }
}
