package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Timestamp;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PgNotificacionRepository implements NotificacionRepository {
    private final JdbcTemplate jdbc;

    public PgNotificacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void store(NotificacionDTO notificacion) {
        jdbc.update(
                "INSERT INTO notificacion (paciente_id, cita_id, tipo, mensaje, creada_en) VALUES (?, ?, ?, ?, ?)",
                notificacion.pacienteId(),
                notificacion.citaId(),
                notificacion.tipo(),
                notificacion.mensaje(),
                Timestamp.from(notificacion.creadaEn()));
    }

    @Override
    public List<NotificacionDTO> delPaciente(long pacienteId) {
        return jdbc.query(
                "SELECT paciente_id, cita_id, tipo, mensaje, creada_en FROM notificacion WHERE paciente_id = ? ORDER BY creada_en DESC",
                (rs, rowNum) -> new NotificacionDTO(
                        rs.getLong("paciente_id"),
                        rs.getLong("cita_id"),
                        rs.getString("tipo"),
                        rs.getString("mensaje"),
                        rs.getTimestamp("creada_en").toInstant()),
                pacienteId);
    }

    @Override
    public void marcarAvisoEnviado(long citaId) {
        jdbc.update("UPDATE notificacion SET aviso_enviado = true WHERE cita_id = ? AND tipo = 'AVISO_T45'", citaId);
    }

    @Override
    public boolean avisoYaEnviado(long citaId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacion WHERE cita_id = ? AND tipo = 'AVISO_T45' AND aviso_enviado = true",
                Integer.class,
                citaId);
        return count != null && count > 0;
    }
}