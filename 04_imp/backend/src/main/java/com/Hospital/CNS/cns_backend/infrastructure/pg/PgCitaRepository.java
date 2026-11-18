package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CitaRepository sobre PostgreSQL (tabla citas.cita, migracion V5). Nombre del paciente, numero de afiliado y
 * nombre del medico se leen con JOIN (no se copian en la cita). Las CANCELADAS no cuentan para el UNIQUE
 * (medico, fecha, hora): {@code store} devuelve false si ese UNIQUE salta.
 */
public class PgCitaRepository implements CitaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgCitaRepository.class);

    private static final String SELECCION = "select c.id, c.paciente_id, "
            + "coalesce(p.nombres || ' ' || p.apellidos, '') as nombre_paciente, p.numero_afiliado, "
            + "c.medico_id, coalesce(m.nombres || ' ' || m.apellidos, '') as nombre_medico, c.especialidad, "
            + "c.consultorio_id, c.fecha, c.hora, c.numero_ficha, c.estado "
            + "from citas.cita c "
            + "join identidad.paciente p on p.id = c.paciente_id "
            + "join recursos.medico m on m.id = c.medico_id";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('citas.cita_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el id de la cita", e);
        }
    }

    @Override
    public boolean store(CitaDTO c) {
        String sql = "insert into citas.cita (id, paciente_id, medico_id, consultorio_id, especialidad, fecha, hora, "
                + "numero_ficha, estado) values (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "on conflict (id) do update set medico_id = excluded.medico_id, "
                + "consultorio_id = excluded.consultorio_id, especialidad = excluded.especialidad, "
                + "fecha = excluded.fecha, hora = excluded.hora, numero_ficha = excluded.numero_ficha, "
                + "estado = excluded.estado";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, c.citaId());
            stmt.setLong(2, c.pacienteId());
            stmt.setLong(3, c.medicoId());
            stmt.setInt(4, c.consultorio());
            stmt.setString(5, c.especialidad().name());
            stmt.setObject(6, c.fecha());
            stmt.setObject(7, c.hora());
            stmt.setInt(8, c.numeroFicha());
            stmt.setString(9, c.estado().name());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (ErroresPg.UNICO.equals(e.getSQLState())) {
                return false;
            }
            throw ErroresPg.traducir(LOG, "guardar la cita " + c.citaId(), e);
        }
    }

    @Override
    public Optional<CitaDTO> porId(long citaId) {
        List<CitaDTO> r = consultar(SELECCION + " where c.id = ?", citaId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public List<CitaDTO> delDia(LocalDate fecha) {
        return consultar(SELECCION + " where c.fecha = ? order by c.hora, c.medico_id", fecha);
    }

    @Override
    public List<CitaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha) {
        return consultar(SELECCION + " where c.medico_id = ? and c.fecha = ? order by c.hora, c.numero_ficha",
                medicoId, fecha);
    }

    @Override
    public List<CitaDTO> delPaciente(long pacienteId) {
        return consultar(SELECCION + " where c.paciente_id = ? order by c.fecha, c.hora", pacienteId);
    }

    @Override
    public int contarDelMedicoEnFecha(long medicoId, LocalDate fecha) {
        return contar("select count(*) from citas.cita where medico_id = ? and fecha = ? and estado <> 'CANCELADA'",
                medicoId, fecha);
    }

    @Override
    public boolean pacienteTieneCitaEnFecha(long pacienteId, LocalDate fecha) {
        return contar("select count(*) from citas.cita where paciente_id = ? and fecha = ? and estado <> 'CANCELADA'",
                pacienteId, fecha) > 0;
    }

    @Override
    public boolean medicoTienePacienteEnAgenda(long medicoId, long pacienteId) {
        return contar("select count(*) from citas.cita where medico_id = ? and paciente_id = ? "
                + "and estado <> 'CANCELADA'", medicoId, pacienteId) > 0;
    }

    @Override
    public int contarProgramadasDelMedico(long medicoId) {
        return contar("select count(*) from citas.cita where medico_id = ? and estado = 'PROGRAMADA'", medicoId);
    }

    @Override
    public int contarProgramadasEnConsultorio(int consultorioId) {
        return contar("select count(*) from citas.cita where consultorio_id = ? and estado = 'PROGRAMADA'",
                consultorioId);
    }

    @Override
    public List<CitaDTO> programadasHasta(LocalDateTime limite) {
        return consultar(SELECCION + " where c.estado = 'PROGRAMADA' and (c.fecha + c.hora) <= ? "
                + "order by c.fecha, c.hora", Timestamp.valueOf(limite));
    }

    @Override
    public boolean cambiarEstado(long citaId, EstadoCita desde, EstadoCita hasta) {
        return actualizar("update citas.cita set estado = ? where id = ? and estado = ?",
                hasta.name(), citaId, desde.name()) == 1;
    }

    @Override
    public boolean registrarFin(long citaId, LocalDateTime horaFin) {
        return actualizar("update citas.cita set estado = 'ATENDIDA', hora_fin = ? "
                + "where id = ? and estado = 'ASISTENCIA_CONFIRMADA'", Timestamp.valueOf(horaFin), citaId) == 1;
    }

    private List<CitaDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = preparar(conn, sql, parametros);
             ResultSet rs = stmt.executeQuery()) {
            List<CitaDTO> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new CitaDTO(rs.getLong("id"), rs.getLong("paciente_id"), rs.getString("nombre_paciente").trim(),
                        rs.getString("numero_afiliado"), rs.getLong("medico_id"), rs.getString("nombre_medico").trim(),
                        TipoEspecialidad.valueOf(rs.getString("especialidad")), rs.getInt("consultorio_id"),
                        rs.getObject("fecha", LocalDate.class), rs.getObject("hora", LocalTime.class),
                        rs.getInt("numero_ficha"), EstadoCita.valueOf(rs.getString("estado"))));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar citas", e);
        }
    }

    private int contar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = preparar(conn, sql, parametros);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "contar citas", e);
        }
    }

    private int actualizar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = preparar(conn, sql, parametros)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "actualizar la cita", e);
        }
    }

    private static PreparedStatement preparar(Connection conn, String sql, Object... parametros) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            stmt.setObject(i + 1, parametros[i]);
        }
        return stmt;
    }
}
