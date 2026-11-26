package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaDTO;
import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CirugiaRepository sobre quirofano.cirugia (migracion V8). Los choques los impide la base con EXCLUDE: si salta
 * (SQLState 23P01) {@code insertar} y {@code reprogramar} devuelven false. Dentro de una transaccion ese error deja
 * la transaccion abortada, asi que quien llama debe lanzar una excepcion para deshacerla.
 */
public class PgCirugiaRepository implements CirugiaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgCirugiaRepository.class);

    private static final String SELECCION = "select c.id, c.ficha_interna_id, c.paciente_id, "
            + "trim(p.nombres || ' ' || p.apellidos) as nombre_paciente, p.numero_afiliado, p.fecha_nacimiento, "
            + "c.cirujano_id, trim(m.nombres || ' ' || m.apellidos) as nombre_cirujano, c.quirofano_id, "
            + "q.nombre as nombre_quirofano, c.especialidad, c.inicio, c.duracion_min, c.limpieza_min, c.gravedad, "
            + "c.urgencia, c.adulto_mayor, c.orden_llegada, c.estado, c.creada_en "
            + "from quirofano.cirugia c "
            + "join identidad.paciente p on p.id = c.paciente_id "
            + "join recursos.medico m on m.id = c.cirujano_id "
            + "join quirofano.quirofano q on q.id = c.quirofano_id";

    @Override
    public long nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('quirofano.cirugia_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el numero de cirugia", e);
        }
    }

    @Override
    public boolean insertar(CirugiaDTO c) {
        String sql = "insert into quirofano.cirugia (id, ficha_interna_id, paciente_id, cirujano_id, quirofano_id, "
                + "especialidad, inicio, duracion_min, limpieza_min, gravedad, urgencia, adulto_mayor, orden_llegada, "
                + "estado, creada_en) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return escribir("guardar la cirugia " + c.cirugiaId(), sql, c.cirugiaId(), c.fichaInternaId(), c.pacienteId(),
                c.cirujanoId(), c.quirofanoId(), c.especialidad().name(), Timestamp.valueOf(c.inicio()),
                c.duracionMin(), c.limpiezaMin(), c.gravedad().name(), c.urgencia(), c.adultoMayor(), c.ordenLlegada(),
                c.estado().name(), Timestamp.valueOf(c.creadaEn())) == 1;
    }

    @Override
    public boolean reprogramar(long cirugiaId, int quirofanoId, LocalDateTime nuevoInicio) {
        return escribir("reprogramar la cirugia " + cirugiaId, "update quirofano.cirugia set quirofano_id = ?, "
                + "inicio = ? where id = ? and estado = 'PROGRAMADA'", quirofanoId, Timestamp.valueOf(nuevoInicio),
                cirugiaId) == 1;
    }

    @Override
    public Optional<CirugiaDTO> porId(long cirugiaId) {
        List<CirugiaDTO> r = consultar(SELECCION + " where c.id = ?", cirugiaId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public boolean cambiarEstado(long cirugiaId, EstadoCirugia desde, EstadoCirugia hasta) {
        return escribir("cambiar el estado de la cirugia " + cirugiaId,
                "update quirofano.cirugia set estado = ? where id = ? and estado = ?", hasta.name(), cirugiaId,
                desde.name()) == 1;
    }

    @Override
    public List<CirugiaDTO> deEspecialidadEnFecha(TipoEspecialidad especialidad, LocalDate fecha) {
        return consultar(SELECCION + " where c.especialidad = ? and c.estado <> 'CANCELADA' and c.inicio < ? "
                + "and c.fin_con_limpieza > ? order by c.inicio", especialidad.name(),
                Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()), Timestamp.valueOf(fecha.atStartOfDay()));
    }

    @Override
    public List<CirugiaDTO> delCirujanoEnFecha(long cirujanoId, LocalDate fecha) {
        return consultar(SELECCION + " where c.cirujano_id = ? and c.estado <> 'CANCELADA' and c.inicio < ? "
                + "and c.fin > ? order by c.inicio", cirujanoId, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()),
                Timestamp.valueOf(fecha.atStartOfDay()));
    }

    @Override
    public List<CirugiaDTO> delPaciente(long pacienteId) {
        return consultar(SELECCION + " where c.paciente_id = ? order by c.inicio", pacienteId);
    }

    @Override
    public List<CirugiaDTO> programadasEntre(TipoEspecialidad especialidad, LocalDateTime desde, LocalDateTime hasta) {
        return consultar(SELECCION + " where c.especialidad = ? and c.estado = 'PROGRAMADA' and c.inicio between ? and ? "
                + "order by c.inicio", especialidad.name(), Timestamp.valueOf(desde), Timestamp.valueOf(hasta));
    }

    @Override
    public int contarProgramadasDesde(int quirofanoId, LocalDateTime desde) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "select count(*) from quirofano.cirugia where quirofano_id = ? and estado = 'PROGRAMADA' and inicio >= ?")) {
            stmt.setInt(1, quirofanoId);
            stmt.setTimestamp(2, Timestamp.valueOf(desde));
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "contar cirugias del quirofano", e);
        }
    }

    /** Filas cambiadas; 0 si la base rechaza por choque (EXCLUDE). */
    private int escribir(String accion, String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            if (ErroresPg.EXCLUSION.equals(e.getSQLState())) {
                return 0;
            }
            throw ErroresPg.traducir(LOG, accion, e);
        }
    }

    private List<CirugiaDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<CirugiaDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new CirugiaDTO(rs.getLong("id"), rs.getLong("ficha_interna_id"), rs.getLong("paciente_id"),
                            rs.getString("nombre_paciente"), rs.getString("numero_afiliado"),
                            rs.getObject("fecha_nacimiento", LocalDate.class), rs.getLong("cirujano_id"),
                            rs.getString("nombre_cirujano"), rs.getInt("quirofano_id"), rs.getString("nombre_quirofano"),
                            TipoEspecialidad.valueOf(rs.getString("especialidad")),
                            rs.getTimestamp("inicio").toLocalDateTime(), rs.getInt("duracion_min"),
                            rs.getInt("limpieza_min"), Gravedad.valueOf(rs.getString("gravedad")),
                            rs.getBoolean("urgencia"), rs.getBoolean("adulto_mayor"), rs.getLong("orden_llegada"),
                            EstadoCirugia.valueOf(rs.getString("estado")),
                            rs.getTimestamp("creada_en").toLocalDateTime()));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar cirugias", e);
        }
    }
}
