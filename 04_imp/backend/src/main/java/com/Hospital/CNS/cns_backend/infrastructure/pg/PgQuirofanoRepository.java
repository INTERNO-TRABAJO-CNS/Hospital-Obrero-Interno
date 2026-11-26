package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoRepository;

/** QuirofanoRepository sobre quirofano.quirofano (migracion V8). */
public class PgQuirofanoRepository implements QuirofanoRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgQuirofanoRepository.class);
    private static final String SELECCION =
            "select id, nombre, especialidad, apertura, cierre, limpieza_min, activo from quirofano.quirofano";

    @Override
    public int nextIdentity() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select nextval('quirofano.quirofano_id_seq')");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "generar el id del quirofano", e);
        }
    }

    @Override
    public boolean store(QuirofanoDTO q) {
        String sql = "insert into quirofano.quirofano (id, nombre, especialidad, apertura, cierre, limpieza_min, activo) "
                + "values (?, ?, ?, ?, ?, ?, ?) on conflict (id) do update set nombre = excluded.nombre, "
                + "especialidad = excluded.especialidad, apertura = excluded.apertura, cierre = excluded.cierre, "
                + "limpieza_min = excluded.limpieza_min, activo = excluded.activo";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, q.quirofanoId());
            stmt.setString(2, q.nombre());
            stmt.setString(3, q.especialidad().name());
            stmt.setObject(4, q.apertura());
            stmt.setObject(5, q.cierre());
            stmt.setInt(6, q.limpiezaMin());
            stmt.setBoolean(7, q.activo());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (ErroresPg.UNICO.equals(e.getSQLState())) {
                return false;
            }
            throw ErroresPg.traducir(LOG, "guardar el quirofano " + q.nombre(), e);
        }
    }

    @Override
    public Optional<QuirofanoDTO> porId(int quirofanoId) {
        List<QuirofanoDTO> r = consultar(SELECCION + " where id = ?", quirofanoId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public List<QuirofanoDTO> listar() {
        return consultar(SELECCION + " order by nombre, id");
    }

    @Override
    public List<QuirofanoDTO> activosDe(TipoEspecialidad especialidad) {
        return consultar(SELECCION + " where especialidad = ? and activo order by nombre", especialidad.name());
    }

    private List<QuirofanoDTO> consultar(String sql, Object... parametros) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<QuirofanoDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new QuirofanoDTO(rs.getInt("id"), rs.getString("nombre"),
                            TipoEspecialidad.valueOf(rs.getString("especialidad")),
                            rs.getObject("apertura", LocalTime.class), rs.getObject("cierre", LocalTime.class),
                            rs.getInt("limpieza_min"), rs.getBoolean("activo")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "consultar quirofanos", e);
        }
    }
}
