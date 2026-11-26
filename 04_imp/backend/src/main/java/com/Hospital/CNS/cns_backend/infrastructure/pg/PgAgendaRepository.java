package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.citas.AgendaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;

/**
 * AgendaRepository sobre PostgreSQL (citas.agenda_dia de V9 y citas.cita de V5).
 *
 * <p>{@link #intentarBloquear}: la agenda de un medico en un dia es una fila de citas.agenda_dia. Se toma con
 * {@code SELECT ... FOR UPDATE SKIP LOCKED} (no espera a nadie). Si la fila todavia no existe, se inserta: la fila
 * recien insertada queda bloqueada para los demas hasta el commit. Si otro proceso la inserto al mismo tiempo, el
 * INSERT espera a que ese termine y no inserta nada: se responde false (otro ya tomo esa agenda).</p>
 *
 * <p>El bloqueo solo sirve dentro de una transaccion ({@code PgTransaccion}); fuera de ella se libera al instante.</p>
 */
public class PgAgendaRepository implements AgendaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgAgendaRepository.class);

    private static final String TOMAR = "select 1 from citas.agenda_dia where medico_id = ? and fecha = ? "
            + "for update skip locked";
    private static final String EXISTE = "select 1 from citas.agenda_dia where medico_id = ? and fecha = ?";
    private static final String CREAR = "insert into citas.agenda_dia (medico_id, fecha) values (?, ?) "
            + "on conflict (medico_id, fecha) do nothing";
    private static final String CONSULTORIO_OCUPADO = "select exists (select 1 from citas.cita c "
            + "join recursos.medico m on m.id = c.medico_id "
            + "where c.consultorio_id = ? and c.fecha = ? and c.estado <> 'CANCELADA' "
            + "and c.hora < ? and c.hora + make_interval(mins => m.duracion_cita_min) > ?)";

    private final CitaRepository citas;

    public PgAgendaRepository() {
        this(new PgCitaRepository());
    }

    public PgAgendaRepository(CitaRepository citas) {
        this.citas = citas;
    }

    @Override
    public boolean intentarBloquear(long medicoId, LocalDate fecha) {
        try (Connection conn = DB.getConnection()) {
            if (hayFila(conn, TOMAR, medicoId, fecha)) {
                return true;
            }
            if (hayFila(conn, EXISTE, medicoId, fecha)) {
                LOG.debug("Agenda del medico {} el {} tomada por otro proceso", medicoId, fecha);
                return false;
            }
            try (PreparedStatement stmt = conn.prepareStatement(CREAR)) {
                stmt.setLong(1, medicoId);
                stmt.setObject(2, fecha);
                boolean creada = stmt.executeUpdate() == 1;
                if (!creada) {
                    LOG.debug("Agenda del medico {} el {} creada al mismo tiempo por otro proceso", medicoId, fecha);
                }
                return creada;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "bloquear la agenda del medico " + medicoId, e);
        }
    }

    @Override
    public List<CitaDTO> ocupacionDelMedico(long medicoId, LocalDate fecha) {
        return citas.delMedicoEnFecha(medicoId, fecha).stream()
                .filter(c -> c.estado() != EstadoCita.CANCELADA)
                .toList();
    }

    @Override
    public boolean consultorioOcupado(int consultorioId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CONSULTORIO_OCUPADO)) {
            stmt.setInt(1, consultorioId);
            stmt.setObject(2, fecha);
            stmt.setObject(3, fin);
            stmt.setObject(4, inicio);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "revisar el consultorio " + consultorioId, e);
        }
    }

    private static boolean hayFila(Connection conn, String sql, long medicoId, LocalDate fecha) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, medicoId);
            stmt.setObject(2, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
