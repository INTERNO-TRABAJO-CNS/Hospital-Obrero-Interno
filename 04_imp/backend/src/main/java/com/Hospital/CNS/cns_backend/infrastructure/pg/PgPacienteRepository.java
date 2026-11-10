package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;

/** PacienteRepository sobre PostgreSQL (tabla identidad.paciente, migracion V2). */
public class PgPacienteRepository implements PacienteRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgPacienteRepository.class);
    private static final String VIOLACION_UNIQUE = "23505";
    private static final String COLUMNAS = "id, numero_afiliado, ci, nombres, apellidos, fecha_nacimiento, es_titular";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('identidad.paciente_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw error("generar el id del paciente", e);
        }
    }

    @Override
    public boolean store(PacienteDTO p) {
        String sql = "insert into identidad.paciente (" + COLUMNAS + ") values (?, ?, ?, ?, ?, ?, ?) "
                + "on conflict (id) do update set nombres = excluded.nombres, apellidos = excluded.apellidos, "
                + "fecha_nacimiento = excluded.fecha_nacimiento";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, p.pacienteId());
            stmt.setString(2, p.numeroAfiliado());
            stmt.setString(3, p.ci());
            stmt.setString(4, p.nombres());
            stmt.setString(5, p.apellidos());
            stmt.setDate(6, Date.valueOf(p.fechaNacimiento()));
            stmt.setBoolean(7, p.titular());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (VIOLACION_UNIQUE.equals(e.getSQLState())) {
                return false;
            }
            throw error("guardar el paciente " + p.pacienteId(), e);
        }
    }

    @Override
    public Optional<PacienteDTO> porId(long pacienteId) {
        List<PacienteDTO> r = consultar("select " + COLUMNAS + " from identidad.paciente where id = ?", pacienteId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public Optional<PacienteDTO> porAfiliadoYCi(String numeroAfiliado, String ci) {
        List<PacienteDTO> r = consultar("select " + COLUMNAS + " from identidad.paciente "
                + "where numero_afiliado = ? and lower(ci) = lower(?)", numeroAfiliado, ci);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public List<PacienteDTO> deLaAfiliacion(String numeroAfiliado) {
        return consultar("select " + COLUMNAS + " from identidad.paciente where numero_afiliado = ? "
                + "order by es_titular desc, apellidos, nombres", numeroAfiliado);
    }

    private List<PacienteDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<PacienteDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new PacienteDTO(rs.getLong("id"), rs.getString("numero_afiliado"), rs.getString("ci"),
                            rs.getString("nombres"), rs.getString("apellidos"),
                            rs.getDate("fecha_nacimiento").toLocalDate(), rs.getBoolean("es_titular")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw error("consultar pacientes", e);
        }
    }

    private static IllegalStateException error(String accion, SQLException e) {
        LOG.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
