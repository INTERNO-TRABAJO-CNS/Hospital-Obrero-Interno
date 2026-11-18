package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.fichasinternas.CambioFichaRepository;

/** Auditoria de fichas internas y cirugias en fichas_internas.cambio (migracion V7). */
public class PgCambioFichaRepository implements CambioFichaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgCambioFichaRepository.class);

    @Override
    public void registrar(String objeto, long objetoId, String accion, String usuario, String motivo) {
        String sql = "insert into fichas_internas.cambio (objeto, objeto_id, accion, usuario, motivo) "
                + "values (?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, objeto);
            stmt.setLong(2, objetoId);
            stmt.setString(3, accion);
            stmt.setString(4, usuario);
            stmt.setString(5, motivo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "registrar el cambio de " + objeto + " " + objetoId, e);
        }
    }
}
