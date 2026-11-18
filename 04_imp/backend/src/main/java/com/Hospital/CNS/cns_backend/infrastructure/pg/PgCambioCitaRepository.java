package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;

/** Auditoria de citas en citas.cambio_cita (migracion V10). El id lo pone la base. */
public class PgCambioCitaRepository implements CambioCitaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgCambioCitaRepository.class);
    private static final int MAX_MOTIVO = 500;

    private static final String SELECCION = "select id, cita_id, accion, usuario, fecha_hora, motivo "
            + "from citas.cambio_cita";

    @Override
    public void registrar(CambioCitaDTO c) {
        String sql = "insert into citas.cambio_cita (cita_id, accion, usuario, fecha_hora, motivo) "
                + "values (?, ?, ?, ?, ?)";
        String motivo = c.motivo() != null && c.motivo().length() > MAX_MOTIVO
                ? c.motivo().substring(0, MAX_MOTIVO) : c.motivo();
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, c.citaId());
            stmt.setString(2, c.accion());
            stmt.setString(3, c.usuario());
            stmt.setTimestamp(4, Timestamp.valueOf(c.fechaHora()));
            stmt.setString(5, motivo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "registrar el cambio de la cita " + c.citaId(), e);
        }
    }

    @Override
    public List<CambioCitaDTO> deCita(long citaId) {
        return consultar(SELECCION + " where cita_id = ? order by fecha_hora, id", List.of(citaId));
    }

    /** Cada filtro null se ignora; {@code desde} y {@code hasta} incluyen el dia indicado. */
    @Override
    public List<CambioCitaDTO> buscar(Long citaId, String usuario, LocalDate desde, LocalDate hasta) {
        StringBuilder sql = new StringBuilder(SELECCION).append(" where true");
        List<Object> parametros = new ArrayList<>();
        if (citaId != null) {
            sql.append(" and cita_id = ?");
            parametros.add(citaId);
        }
        if (usuario != null) {
            sql.append(" and usuario = ?");
            parametros.add(usuario);
        }
        if (desde != null) {
            sql.append(" and fecha_hora >= ?");
            parametros.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hasta != null) {
            sql.append(" and fecha_hora < ?");
            parametros.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        }
        sql.append(" order by fecha_hora, id");
        return consultar(sql.toString(), parametros);
    }

    private List<CambioCitaDTO> consultar(String sql, List<Object> parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<CambioCitaDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new CambioCitaDTO(rs.getLong("id"), rs.getLong("cita_id"), rs.getString("accion"),
                            rs.getString("usuario"), rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            rs.getString("motivo")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar los cambios de citas", e);
        }
    }
}
