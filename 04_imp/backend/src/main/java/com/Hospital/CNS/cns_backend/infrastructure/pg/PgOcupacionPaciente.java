package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.citas.OcupacionPaciente;

/**
 * T100-24: cruce del paciente con sus fichas internas (ASIGNADA o ATENDIDA, de las 20 tablas via
 * {@code fichas_internas.v_todas}) y sus cirugias no CANCELADAS. Cruce = {@code inicio < finPedido AND fin >
 * inicioPedido}: terminar justo cuando empieza la otra no cruza.
 */
public class PgOcupacionPaciente implements OcupacionPaciente {

    private static final Logger LOG = LoggerFactory.getLogger(PgOcupacionPaciente.class);

    private static final String SQL = "select exists ("
            + "select 1 from fichas_internas.v_todas f "
            + "where f.paciente_id = ? and f.estado in ('ASIGNADA', 'ATENDIDA') and f.fecha = ? "
            + "and f.hora < ? and f.hora + make_interval(mins => f.duracion_min) > ? "
            + "union all "
            + "select 1 from quirofano.cirugia c "
            + "where c.paciente_id = ? and c.estado <> 'CANCELADA' and c.inicio < ? and c.fin > ?)";

    @Override
    public boolean ocupado(long pacienteId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, pacienteId);
            stmt.setObject(2, fecha);
            stmt.setObject(3, fin);
            stmt.setObject(4, inicio);
            stmt.setLong(5, pacienteId);
            stmt.setTimestamp(6, Timestamp.valueOf(fecha.atTime(fin)));
            stmt.setTimestamp(7, Timestamp.valueOf(fecha.atTime(inicio)));
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "revisar la ocupacion del paciente", e);
        }
    }
}
