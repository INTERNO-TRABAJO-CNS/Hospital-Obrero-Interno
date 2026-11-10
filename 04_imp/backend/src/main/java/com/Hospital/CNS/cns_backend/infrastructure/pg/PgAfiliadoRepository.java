package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoDTO;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoRepository;

/** AfiliadoRepository sobre PostgreSQL (tabla identidad.afiliado, migracion V2). */
public class PgAfiliadoRepository implements AfiliadoRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgAfiliadoRepository.class);

    @Override
    public boolean existePorNumero(String numeroAfiliado) {
        return titularPorNumero(numeroAfiliado).isPresent();
    }

    @Override
    public Optional<AfiliadoDTO> titularPorNumero(String numeroAfiliado) {
        String sql = "select numero_afiliado, ci, nombres, apellidos, fecha_nacimiento, activo "
                + "from identidad.afiliado where numero_afiliado = ? and activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroAfiliado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AfiliadoDTO(rs.getString("numero_afiliado"), rs.getString("ci"),
                        rs.getString("nombres"), rs.getString("apellidos"),
                        rs.getDate("fecha_nacimiento").toLocalDate(), rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            LOG.error("No se pudo buscar el afiliado: {}", e.getMessage(), e);
            throw new IllegalStateException("No se pudo buscar el afiliado", e);
        }
    }
}
