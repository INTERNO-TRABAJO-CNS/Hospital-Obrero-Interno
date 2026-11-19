package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

public class PgSolicitudRepository implements SolicitudRepository {

    private static final Logger LOG =
            LoggerFactory.getLogger(PgSolicitudRepository.class);

    @Override
    public long nextIdentity() {
        String sql = "select nextval('citas.solicitud_id_seq')";

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el id de la solicitud", e);
        }
    }

    @Override
    public void store(SolicitudDTO solicitud) {
        String sql = """
                insert into citas.solicitud
                    (id, paciente_id, especialidad, dia_solicitado,
                     prioridad, orden_llegada, estado, cita_id)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do update set
                    paciente_id = excluded.paciente_id,
                    especialidad = excluded.especialidad,
                    dia_solicitado = excluded.dia_solicitado,
                    prioridad = excluded.prioridad,
                    orden_llegada = excluded.orden_llegada,
                    estado = excluded.estado,
                    cita_id = excluded.cita_id
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, solicitud.solicitudId());
            stmt.setLong(2, solicitud.pacienteId());
            stmt.setString(3, solicitud.especialidad().name());
            stmt.setObject(4, solicitud.diaSolicitado());
            stmt.setInt(5, solicitud.prioridad());
            stmt.setLong(6, solicitud.ordenLlegada());
            stmt.setString(7, solicitud.estado().name());

            if (solicitud.citaId() == null) {
                stmt.setNull(8, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(8, solicitud.citaId());
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw ErroresPg.traducir(
                    LOG, "guardar la solicitud " + solicitud.solicitudId(), e);
        }
    }

    @Override
    public Optional<SolicitudDTO> porId(long solicitudId) {
        String sql = """
                select id, paciente_id, especialidad, dia_solicitado,
                       prioridad, orden_llegada, estado, cita_id
                from citas.solicitud
                where id = ?
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, solicitudId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar la solicitud " + solicitudId, e);
        }
    }

    @Override
    public List<SolicitudDTO> porPaciente(long pacienteId) {
        String sql = """
                select id, paciente_id, especialidad, dia_solicitado,
                       prioridad, orden_llegada, estado, cita_id
                from citas.solicitud
                where paciente_id = ?
                order by orden_llegada desc
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<SolicitudDTO> solicitudes = new ArrayList<>();

                while (rs.next()) {
                    solicitudes.add(mapear(rs));
                }

                return solicitudes;
            }

        } catch (SQLException e) {
            throw ErroresPg.traducir(
                    LOG,
                    "consultar solicitudes del paciente " + pacienteId,
                    e
            );
        }
    }

    @Override
    public boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia) {
        String sql = """
                select count(*)
                from citas.solicitud
                where paciente_id = ?
                  and dia_solicitado = ?
                  and estado in ('PENDIENTE', 'EN_ESPERA')
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);
            stmt.setObject(2, dia);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw ErroresPg.traducir(
                    LOG, "verificar solicitudes activas del paciente", e);
        }
    }

    @Override
    public List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max) {
        String sql = """
                select id, paciente_id, especialidad, dia_solicitado,
                       prioridad, orden_llegada, estado, cita_id
                from citas.solicitud
                where dia_solicitado <= ?
                  and estado in ('PENDIENTE', 'EN_ESPERA')
                order by dia_solicitado asc, prioridad desc, orden_llegada asc
                limit ?
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, hasta);
            stmt.setInt(2, max);

            try (ResultSet rs = stmt.executeQuery()) {
                List<SolicitudDTO> solicitudes = new ArrayList<>();

                while (rs.next()) {
                    solicitudes.add(mapear(rs));
                }

                return solicitudes;
            }

        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar solicitudes pendientes", e);
        }
    }

    private SolicitudDTO mapear(ResultSet rs) throws SQLException {
        Long citaId = rs.getObject("cita_id", Long.class);

        return new SolicitudDTO(
                rs.getLong("id"),
                rs.getLong("paciente_id"),
                TipoEspecialidad.valueOf(rs.getString("especialidad")),
                rs.getObject("dia_solicitado", LocalDate.class),
                rs.getInt("prioridad"),
                rs.getLong("orden_llegada"),
                EstadoSolicitud.valueOf(rs.getString("estado")),
                citaId
        );
    }
}