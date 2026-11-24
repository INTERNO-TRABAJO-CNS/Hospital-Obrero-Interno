package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.util.List;

import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionRepository;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PgNotificacionRepository implements NotificacionRepository {
    private final JdbcTemplate jdbc;

    public PgNotificacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<NotificacionDTO> delPaciente(long pacienteId) {
        return jdbc.query(
                "SELECT id, paciente_id, cita_id, ficha_id, cirugia_id, tipo, mensaje, fecha_hora "
                        + "FROM notificacion.notificacion WHERE paciente_id = ? ORDER BY fecha_hora DESC",
                (rs, rowNum) -> new NotificacionDTO(
                        rs.getLong("id"),
                        rs.getLong("paciente_id"),
                        nullableLong(rs, "cita_id"),
                        nullableLong(rs, "ficha_id"),
                        nullableLong(rs, "cirugia_id"),
                        rs.getString("tipo"),
                        rs.getString("mensaje"),
                        rs.getTimestamp("fecha_hora").toLocalDateTime()),
                pacienteId);
    }

    @Override
    public boolean avisoYaEnviado(long citaId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacion.notificacion WHERE cita_id = ? AND tipo = 'AVISO_T45'",
                Integer.class,
                citaId);
        return count != null && count > 0;
    }

    private static Long nullableLong(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }
}