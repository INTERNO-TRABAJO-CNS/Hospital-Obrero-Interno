package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaDTO;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaRepository;

/**
 * FichaInternaRepository sobre las 20 tablas de {@code fichas_internas} (migracion V7). Escribe en la tabla de la
 * especialidad (nombre tomado del enum, nunca del usuario) y lee por la vista {@code v_todas} con los nombres de
 * paciente, medicos y consultorio por JOIN.
 */
public class PgFichaInternaRepository implements FichaInternaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgFichaInternaRepository.class);

    /** Clave del candado de cada especialidad (pg_advisory_xact_lock). */
    private static final long CANDADO_BASE = 710_000L;

    private static final String SELECCION = "select f.id, f.especialidad, f.pediatrica, f.paciente_id, "
            + "trim(p.nombres || ' ' || p.apellidos) as nombre_paciente, p.numero_afiliado, p.fecha_nacimiento, "
            + "f.cita_origen_id, f.medico_derivante_id, trim(d.nombres || ' ' || d.apellidos) as nombre_derivante, "
            + "d.especialidad as especialidad_derivante, f.medico_id, trim(m.nombres || ' ' || m.apellidos) as "
            + "nombre_medico, f.consultorio_id, c.nombre as nombre_consultorio, f.fecha, f.hora, f.duracion_min, "
            + "f.gravedad, f.urgencia, f.adulto_mayor, f.orden_llegada, f.motivo, f.observaciones, f.estado, "
            + "f.creada_en "
            + "from fichas_internas.v_todas f "
            + "join identidad.paciente p on p.id = f.paciente_id "
            + "join recursos.medico d on d.id = f.medico_derivante_id "
            + "left join recursos.medico m on m.id = f.medico_id "
            + "left join recursos.consultorio c on c.id = f.consultorio_id";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('fichas_internas.ficha_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el numero de ticket", e);
        }
    }

    @Override
    public void insertar(FichaInternaDTO f) {
        String sql = "insert into fichas_internas." + tabla(f.especialidad(), f.pediatrica())
                + " (id, paciente_id, cita_origen_id, medico_derivante_id, medico_id, consultorio_id, fecha, hora, "
                + "duracion_min, gravedad, urgencia, adulto_mayor, orden_llegada, motivo, observaciones, estado, "
                + "creada_en) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        actualizar("guardar la ficha interna " + f.fichaId(), sql, f.fichaId(), f.pacienteId(), f.citaOrigenId(),
                f.medicoDerivanteId(), f.medicoId(), f.consultorioId(), f.fecha(), f.hora(), f.duracionMin(),
                f.gravedad().name(), f.urgencia(), f.adultoMayor(), f.ordenLlegada(), f.motivo(), f.observaciones(),
                f.estado().name(), Timestamp.valueOf(f.creadaEn()));
    }

    @Override
    public void guardarAsignacion(FichaInternaDTO f) {
        String sql = "update fichas_internas." + tabla(f.especialidad(), f.pediatrica())
                + " set medico_id = ?, consultorio_id = ?, fecha = ?, hora = ?, duracion_min = ?, estado = ? "
                + "where id = ?";
        actualizar("asignar la ficha interna " + f.fichaId(), sql, f.medicoId(), f.consultorioId(), f.fecha(),
                f.hora(), f.duracionMin(), f.estado().name(), f.fichaId());
    }

    @Override
    public boolean cambiarEstado(long fichaId, EstadoFichaInterna desde, EstadoFichaInterna hasta,
                                 String observaciones) {
        Optional<FichaInternaDTO> f = porId(fichaId);
        if (f.isEmpty()) {
            return false;
        }
        String sql = "update fichas_internas." + tabla(f.get().especialidad(), f.get().pediatrica())
                + " set estado = ?, observaciones = ? where id = ? and estado = ?";
        return actualizar("cambiar el estado de la ficha " + fichaId, sql, hasta.name(), observaciones, fichaId,
                desde.name()) == 1;
    }

    @Override
    public boolean registrarDerivacion(long citaOrigenId, long fichaId) {
        return actualizar("registrar la derivacion", "insert into fichas_internas.derivacion (cita_origen_id, ficha_id) "
                + "values (?, ?) on conflict (cita_origen_id) do nothing", citaOrigenId, fichaId) == 1;
    }

    @Override
    public Optional<FichaInternaDTO> porId(long fichaId) {
        List<FichaInternaDTO> r = consultar(SELECCION + " where f.id = ?", fichaId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public Optional<FichaInternaDTO> deCita(long citaOrigenId) {
        List<FichaInternaDTO> r = consultar(SELECCION + " where f.cita_origen_id = ?", citaOrigenId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public boolean pacienteTieneFichaActiva(long pacienteId) {
        return contar("select count(*) from fichas_internas.v_todas where paciente_id = ? "
                + "and estado in ('EN_ESPERA', 'ASIGNADA')", pacienteId) > 0;
    }

    @Override
    public List<FichaInternaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha) {
        return consultar(SELECCION + " where f.medico_id = ? and f.fecha = ? order by f.hora", medicoId, fecha);
    }

    @Override
    public List<FichaInternaDTO> asignadasEntre(TipoEspecialidad especialidad, LocalDate desde, LocalDate hasta) {
        return consultar(SELECCION + " where f.especialidad = ? and f.estado = 'ASIGNADA' and f.fecha between ? and ? "
                + "order by f.fecha, f.hora", especialidad.name(), desde, hasta);
    }

    @Override
    public List<FichaInternaDTO> enEspera(TipoEspecialidad especialidad) {
        return consultar(SELECCION + " where f.especialidad = ? and f.estado = 'EN_ESPERA' order by f.orden_llegada",
                especialidad.name());
    }

    @Override
    public List<FichaInternaDTO> activas(TipoEspecialidad especialidad, boolean pediatrica) {
        return consultar(SELECCION + " where f.especialidad = ? and f.pediatrica = ? "
                + "and f.estado in ('EN_ESPERA', 'ASIGNADA') order by f.orden_llegada", especialidad.name(), pediatrica);
    }

    @Override
    public List<FichaInternaDTO> delPaciente(long pacienteId) {
        return consultar(SELECCION + " where f.paciente_id = ? order by f.creada_en", pacienteId);
    }

    @Override
    public boolean medicoTieneFichaConPaciente(long medicoId, long pacienteId) {
        return contar("select count(*) from fichas_internas.v_todas where paciente_id = ? "
                + "and (medico_id = ? or medico_derivante_id = ?)", pacienteId, medicoId, medicoId) > 0;
    }

    @Override
    public void bloquearEspecialidad(TipoEspecialidad especialidad) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select pg_advisory_xact_lock(?)")) {
            stmt.setLong(1, CANDADO_BASE + especialidad.ordinal());
            stmt.execute();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "bloquear la agenda de " + especialidad, e);
        }
    }

    /** Nombre de la tabla de la lista fija: fichas_internas.gastroenterologia[_pediatria]. */
    static String tabla(TipoEspecialidad especialidad, boolean pediatrica) {
        if (!especialidad.esInterna()) {
            throw new IllegalArgumentException("No es una especialidad interna: " + especialidad);
        }
        return especialidad.name().toLowerCase() + (pediatrica ? "_pediatria" : "");
    }

    private List<FichaInternaDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = preparar(conn, sql, parametros);
             ResultSet rs = stmt.executeQuery()) {
            List<FichaInternaDTO> lista = new ArrayList<>();
            while (rs.next()) {
                long medico = rs.getLong("medico_id");
                Long medicoId = rs.wasNull() ? null : medico;
                int consultorio = rs.getInt("consultorio_id");
                Integer consultorioId = rs.wasNull() ? null : consultorio;
                long cita = rs.getLong("cita_origen_id");
                Long citaId = rs.wasNull() ? null : cita;
                Timestamp creada = rs.getTimestamp("creada_en");
                lista.add(new FichaInternaDTO(rs.getLong("id"), TipoEspecialidad.valueOf(rs.getString("especialidad")),
                        rs.getBoolean("pediatrica"), rs.getLong("paciente_id"), rs.getString("nombre_paciente"),
                        rs.getString("numero_afiliado"), rs.getObject("fecha_nacimiento", LocalDate.class), citaId,
                        rs.getLong("medico_derivante_id"), rs.getString("nombre_derivante"),
                        TipoEspecialidad.valueOf(rs.getString("especialidad_derivante")), medicoId,
                        rs.getString("nombre_medico"), consultorioId, rs.getString("nombre_consultorio"),
                        rs.getObject("fecha", LocalDate.class), rs.getObject("hora", LocalTime.class),
                        rs.getInt("duracion_min"), Gravedad.valueOf(rs.getString("gravedad")), rs.getBoolean("urgencia"),
                        rs.getBoolean("adulto_mayor"), rs.getLong("orden_llegada"), rs.getString("motivo"),
                        rs.getString("observaciones"), EstadoFichaInterna.valueOf(rs.getString("estado")),
                        creada == null ? null : creada.toLocalDateTime()));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar fichas internas", e);
        }
    }

    private int contar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = preparar(conn, sql, parametros);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "contar fichas internas", e);
        }
    }

    private int actualizar(String accion, String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = preparar(conn, sql, parametros)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, accion, e);
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
