package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;

/** HorarioRepository sobre PostgreSQL (tabla recursos.horario_medico). dia_semana: 1 lunes ... 7 domingo. */
public class PgHorarioRepository implements HorarioRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgHorarioRepository.class);

    private static final String SELECCION =
            "select id, medico_id, consultorio_id, dia_semana, hora_inicio, hora_fin, activo "
                    + "from recursos.horario_medico";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('recursos.horario_medico_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw error("generar el id del horario", e);
        }
    }

    @Override
    public void store(HorarioDTO h) {
        String sql = "insert into recursos.horario_medico "
                + "(id, medico_id, consultorio_id, dia_semana, hora_inicio, hora_fin, activo) "
                + "values (?, ?, ?, ?, ?, ?, ?) "
                + "on conflict (id) do update set medico_id = excluded.medico_id, "
                + "consultorio_id = excluded.consultorio_id, dia_semana = excluded.dia_semana, "
                + "hora_inicio = excluded.hora_inicio, hora_fin = excluded.hora_fin, activo = excluded.activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, h.horarioId());
            stmt.setLong(2, h.medicoId());
            stmt.setInt(3, h.consultorioId());
            stmt.setInt(4, h.diaSemana().getValue());
            stmt.setObject(5, h.horaInicio());
            stmt.setObject(6, h.horaFin());
            stmt.setBoolean(7, h.activo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw error("guardar el horario " + h.horarioId(), e);
        }
    }

    @Override
    public Optional<HorarioDTO> porId(long horarioId) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECCION + " where id = ?")) {
            stmt.setLong(1, horarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar el horario " + horarioId, e);
        }
    }

    @Override
    public List<HorarioDTO> listar() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECCION + " order by id");
             ResultSet rs = stmt.executeQuery()) {
            return leer(rs);
        } catch (SQLException e) {
            throw error("listar los horarios", e);
        }
    }

    @Override
    public List<HorarioDTO> delMedico(long medicoId) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECCION + " where medico_id = ? order by id")) {
            stmt.setLong(1, medicoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return leer(rs);
            }
        } catch (SQLException e) {
            throw error("listar los horarios del medico " + medicoId, e);
        }
    }

    @Override
    public List<HorarioDTO> delConsultorio(int consultorioId) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECCION + " where consultorio_id = ? order by id")) {
            stmt.setInt(1, consultorioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return leer(rs);
            }
        } catch (SQLException e) {
            throw error("listar los horarios del consultorio " + consultorioId, e);
        }
    }

    private static List<HorarioDTO> leer(ResultSet rs) throws SQLException {
        List<HorarioDTO> horarios = new ArrayList<>();
        while (rs.next()) {
            horarios.add(mapear(rs));
        }
        return horarios;
    }

    private static HorarioDTO mapear(ResultSet rs) throws SQLException {
        return new HorarioDTO(rs.getLong("id"), rs.getLong("medico_id"), rs.getInt("consultorio_id"),
                DayOfWeek.of(rs.getInt("dia_semana")), rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fin", LocalTime.class), rs.getBoolean("activo"));
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
