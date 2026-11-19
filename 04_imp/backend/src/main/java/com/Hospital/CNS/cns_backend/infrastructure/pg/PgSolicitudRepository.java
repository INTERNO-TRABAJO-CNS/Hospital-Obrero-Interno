package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * SolicitudRepository sobre PostgreSQL (tabla citas.solicitud, migracion V10).
 *
 * <p>{@link #siguientesParaProcesar} toma las filas con {@code FOR UPDATE SKIP LOCKED}: dentro de una transaccion,
 * dos procesos que leen la cola al mismo tiempo nunca reciben la misma solicitud.</p>
 */
public class PgSolicitudRepository implements SolicitudRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgSolicitudRepository.class);

    private static final String SELECCION = "select id, paciente_id, especialidad, dia_solicitado, prioridad, "
            + "orden_llegada, estado, cita_id from citas.solicitud";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('citas.solicitud_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el id de la solicitud", e);
        }
    }

    @Override
    public void store(SolicitudDTO s) {
        String sql = "insert into citas.solicitud (id, paciente_id, especialidad, dia_solicitado, prioridad, "
                + "orden_llegada, estado, cita_id) "
                + "values (?, ?, ?, ?, ?, coalesce(?, nextval('citas.solicitud_orden_seq')), ?, ?) "
                + "on conflict (id) do update set dia_solicitado = excluded.dia_solicitado, "
                + "prioridad = excluded.prioridad, estado = excluded.estado, cita_id = excluded.cita_id, "
                + "actualizado_en = now()";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, s.solicitudId());
            stmt.setLong(2, s.pacienteId());
            stmt.setString(3, s.especialidad().name());
            stmt.setObject(4, s.diaSolicitado());
            stmt.setInt(5, s.prioridad());
            if (s.ordenLlegada() > 0) {
                stmt.setLong(6, s.ordenLlegada());
            } else {
                stmt.setNull(6, Types.BIGINT); // la base pone el siguiente orden de llegada
            }
            stmt.setString(7, s.estado().name());
            if (s.citaId() == null) {
                stmt.setNull(8, Types.BIGINT);
            } else {
                stmt.setLong(8, s.citaId());
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "guardar la solicitud " + s.solicitudId(), e, "SOLICITUD_DUPLICADA",
                    "La cita ya pertenece a otra solicitud");
        }
    }

    @Override
    public Optional<SolicitudDTO> porId(long solicitudId) {
        List<SolicitudDTO> r = consultar(SELECCION + " where id = ?", solicitudId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public List<SolicitudDTO> porPaciente(long pacienteId) {
        return consultar(SELECCION + " where paciente_id = ? order by orden_llegada desc", pacienteId);
    }

    @Override
    public boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia) {
        String sql = "select exists (select 1 from citas.solicitud where paciente_id = ? and dia_solicitado = ? "
                + "and estado in ('PENDIENTE', 'EN_ESPERA'))";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pacienteId);
            stmt.setObject(2, dia);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "revisar las solicitudes del paciente " + pacienteId, e);
        }
    }

    @Override
    public List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max) {
        return consultar(SELECCION + " where estado in ('PENDIENTE', 'EN_ESPERA') and dia_solicitado <= ? "
                + "order by prioridad, orden_llegada limit ? for update skip locked", hasta, max);
    }

    private List<SolicitudDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<SolicitudDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    long citaId = rs.getLong("cita_id");
                    lista.add(new SolicitudDTO(rs.getLong("id"), rs.getLong("paciente_id"),
                            TipoEspecialidad.valueOf(rs.getString("especialidad")),
                            rs.getObject("dia_solicitado", LocalDate.class), rs.getInt("prioridad"),
                            rs.getLong("orden_llegada"), EstadoSolicitud.valueOf(rs.getString("estado")),
                            rs.wasNull() ? null : citaId));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar solicitudes", e);
        }
    }
}
