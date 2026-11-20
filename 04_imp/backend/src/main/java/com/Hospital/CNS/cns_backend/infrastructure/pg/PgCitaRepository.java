package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.comunicacion.CitaProgramada;
import com.Hospital.CNS.cns_backend.model.comunicacion.CitaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PgCitaRepository implements CitaRepository {
    private final JdbcTemplate jdbc;

    public PgCitaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CitaProgramada> programadasEntre(Instant desde, Instant hasta) {
        return jdbc.query(
                "SELECT id, paciente_id, fecha_hora_inicio, asistencia_confirmada FROM cita "
                        + "WHERE estado = 'PROGRAMADA' AND fecha_hora_inicio >= ? AND fecha_hora_inicio <= ?",
                (rs, rowNum) -> new CitaProgramada(
                        rs.getLong("id"),
                        rs.getLong("paciente_id"),
                        rs.getTimestamp("fecha_hora_inicio").toInstant(),
                        rs.getBoolean("asistencia_confirmada")),
                Timestamp.from(desde),
                Timestamp.from(hasta));
    }
}