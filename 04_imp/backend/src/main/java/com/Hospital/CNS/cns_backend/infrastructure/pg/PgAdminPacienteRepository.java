package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.Date;
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
import com.Hospital.CNS.cns_backend.model.identidad.AdminPacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteAdminDTO;

/** AdminPacienteRepository sobre PostgreSQL (identidad.paciente, identidad.afiliado, identidad.usuario). */
public class PgAdminPacienteRepository implements AdminPacienteRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgAdminPacienteRepository.class);
    private static final String SELECCION = "select p.id, p.numero_afiliado, p.ci, p.nombres, p.apellidos, "
            + "p.fecha_nacimiento, p.es_titular, p.activo, (select u.usuario from identidad.usuario u "
            + "where u.rol = 'PACIENTE' and u.persona_id = p.id order by u.id limit 1) as usuario "
            + "from identidad.paciente p";

    @Override
    public List<PacienteAdminDTO> listar() {
        return consultar(SELECCION + " order by lower(p.apellidos), lower(p.nombres), p.id");
    }

    @Override
    public Optional<PacienteAdminDTO> porId(long pacienteId) {
        List<PacienteAdminDTO> r = consultar(SELECCION + " where p.id = ?", pacienteId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public Optional<AfiliadoDTO> afiliadoPorNumero(String numeroAfiliado) {
        String sql = "select numero_afiliado, ci, nombres, apellidos, fecha_nacimiento, activo "
                + "from identidad.afiliado where numero_afiliado = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroAfiliado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AfiliadoDTO(rs.getString("numero_afiliado"), rs.getString("ci"),
                        rs.getString("nombres"), rs.getString("apellidos"),
                        rs.getDate("fecha_nacimiento").toLocalDate(), rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "buscar el afiliado", e);
        }
    }

    @Override
    public boolean guardarAfiliado(AfiliadoDTO a) {
        String sql = "insert into identidad.afiliado (numero_afiliado, ci, nombres, apellidos, fecha_nacimiento, activo) "
                + "values (?, ?, ?, ?, ?, true) on conflict (numero_afiliado) do update set "
                + "nombres = excluded.nombres, apellidos = excluded.apellidos, "
                + "fecha_nacimiento = excluded.fecha_nacimiento, activo = true";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.numeroAfiliado());
            stmt.setString(2, a.ci());
            stmt.setString(3, a.nombres());
            stmt.setString(4, a.apellidos());
            stmt.setDate(5, Date.valueOf(a.fechaNacimiento()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (ErroresPg.UNICO.equals(e.getSQLState())) {
                return false;
            }
            throw ErroresPg.traducir(LOG, "guardar el afiliado " + a.numeroAfiliado(), e);
        }
    }

    @Override
    public void actualizarDatos(long pacienteId, String nombres, String apellidos, LocalDate fechaNacimiento) {
        actualizar("update identidad.paciente set nombres = ?, apellidos = ?, fecha_nacimiento = ? where id = ?",
                nombres, apellidos, Date.valueOf(fechaNacimiento), pacienteId);
        actualizar("update identidad.usuario set nombre = ? where rol = 'PACIENTE' and persona_id = ?",
                (nombres + " " + apellidos).trim(), pacienteId);
    }

    @Override
    public void cambiarActivo(long pacienteId, boolean activo) {
        actualizar("update identidad.paciente set activo = ? where id = ?", activo, pacienteId);
    }

    @Override
    public int contarCitasPendientes(long pacienteId) {
        String sql = "select count(*) from citas.cita where paciente_id = ? "
                + "and estado in ('PROGRAMADA', 'ASISTENCIA_CONFIRMADA')";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pacienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "contar las citas del paciente " + pacienteId, e);
        }
    }

    private void actualizar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "actualizar el paciente", e);
        }
    }

    private List<PacienteAdminDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<PacienteAdminDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new PacienteAdminDTO(rs.getLong("id"), rs.getString("numero_afiliado"),
                            rs.getString("ci"), rs.getString("nombres"), rs.getString("apellidos"),
                            rs.getDate("fecha_nacimiento").toLocalDate(), rs.getBoolean("es_titular"),
                            rs.getBoolean("activo"), rs.getString("usuario")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar pacientes", e);
        }
    }
}
