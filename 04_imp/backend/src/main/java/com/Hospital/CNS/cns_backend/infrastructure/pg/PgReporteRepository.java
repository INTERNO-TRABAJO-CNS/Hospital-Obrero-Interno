package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.reportes.FichaSupervisionDTO;
import com.Hospital.CNS.cns_backend.model.reportes.IndicadoresDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteEstadoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteGrupoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteRepository;
import com.Hospital.CNS.cns_backend.model.reportes.ResumenEspecialidadDTO;

/**
 * ReporteRepository sobre PostgreSQL: solo SELECT (count / group by) sobre citas.cita, citas.solicitud,
 * citas.cambio_cita y los catalogos de recursos. No modifica nada.
 *
 * <p>Una cancelacion es automatica cuando la registro el sistema en citas.cambio_cita (accion CANCELADA, usuario
 * SISTEMA o Sistema): el paciente no confirmo su asistencia hasta T-30.</p>
 */
public class PgReporteRepository implements ReporteRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PgReporteRepository.class);

    /** Condicion "esta cita la cancelo el sistema" (alias c = citas.cita). */
    private static final String CANCELADA_POR_SISTEMA = "exists (select 1 from citas.cambio_cita x "
            + "where x.cita_id = c.id and x.accion = 'CANCELADA' and upper(x.usuario) = 'SISTEMA')";

    @Override
    public List<ResumenEspecialidadDTO> resumenDelDia(LocalDate fecha) {
        String sql = "with horario as ("
                + "  select h.medico_id, min(h.hora_inicio) as inicio, max(h.hora_fin) as fin"
                + "  from recursos.horario_medico h"
                + "  where h.activo and h.dia_semana = ?"
                + "    and not exists (select 1 from recursos.dia_no_habil d where d.activo and d.fecha = ?)"
                + "  group by h.medico_id),"
                + " fichas as ("
                + "  select c.medico_id,"
                + "    count(*) filter (where c.estado <> 'CANCELADA') as fichas,"
                + "    count(*) filter (where c.estado = 'PROGRAMADA') as programadas,"
                + "    count(*) filter (where c.estado = 'ASISTENCIA_CONFIRMADA') as confirmadas,"
                + "    count(*) filter (where c.estado = 'ATENDIDA') as atendidas,"
                + "    count(*) filter (where c.estado = 'CANCELADA') as canceladas"
                + "  from citas.cita c where c.fecha = ? group by c.medico_id)"
                + " select m.id, m.nombres || ' ' || m.apellidos as nombre, m.especialidad, m.cupo_maximo_dia,"
                + "  h.inicio, h.fin, coalesce(f.fichas, 0) as fichas, coalesce(f.programadas, 0) as programadas,"
                + "  coalesce(f.confirmadas, 0) as confirmadas, coalesce(f.atendidas, 0) as atendidas,"
                + "  coalesce(f.canceladas, 0) as canceladas"
                + " from recursos.medico m"
                + " left join horario h on h.medico_id = m.id"
                + " left join fichas f on f.medico_id = m.id"
                + " where (m.activo and h.medico_id is not null) or f.medico_id is not null"
                + " order by m.especialidad, nombre, m.id";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fecha.getDayOfWeek().getValue());
            stmt.setObject(2, fecha);
            stmt.setObject(3, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ResumenEspecialidadDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new ResumenEspecialidadDTO(TipoEspecialidad.valueOf(rs.getString("especialidad")),
                            rs.getLong("id"), rs.getString("nombre"), hora(rs.getTime("inicio")),
                            hora(rs.getTime("fin")), rs.getInt("cupo_maximo_dia"), rs.getInt("fichas"),
                            rs.getInt("programadas"), rs.getInt("confirmadas"), rs.getInt("atendidas"),
                            rs.getInt("canceladas")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "leer el resumen de supervision del " + fecha, e);
        }
    }

    @Override
    public List<FichaSupervisionDTO> fichasDeMedico(long medicoId, LocalDate fecha) {
        String sql = "select c.id, c.numero_ficha, c.hora, c.estado, c.consultorio_id,"
                + " coalesce(p.nombres || ' ' || p.apellidos, '') as paciente"
                + " from citas.cita c join identidad.paciente p on p.id = c.paciente_id"
                + " where c.medico_id = ? and c.fecha = ? order by c.hora, c.numero_ficha";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, medicoId);
            stmt.setObject(2, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                List<FichaSupervisionDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new FichaSupervisionDTO(rs.getLong("id"), rs.getInt("numero_ficha"),
                            rs.getTime("hora").toLocalTime(), EstadoCita.valueOf(rs.getString("estado")),
                            rs.getInt("consultorio_id"), rs.getString("paciente")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "leer las fichas del medico " + medicoId, e);
        }
    }

    @Override
    public List<ReporteEstadoDTO> citasPorEstado(LocalDate desde, LocalDate hasta) {
        String sql = "select c.estado, count(*) as cantidad,"
                + " count(*) filter (where c.estado = 'CANCELADA' and " + CANCELADA_POR_SISTEMA + ") as automaticas"
                + " from citas.cita c where c.fecha between ? and ? group by c.estado";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, desde);
            stmt.setObject(2, hasta);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReporteEstadoDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new ReporteEstadoDTO(EstadoCita.valueOf(rs.getString("estado")),
                            rs.getInt("cantidad"), rs.getInt("automaticas")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "contar las citas por estado", e);
        }
    }

    @Override
    public List<AtendidasDia> atendidasPorMedicoYDia(LocalDate desde, LocalDate hasta) {
        String sql = "select c.medico_id, m.nombres || ' ' || m.apellidos as nombre, m.especialidad, c.fecha,"
                + " count(*) filter (where c.estado = 'ATENDIDA') as atendidas"
                + " from citas.cita c join recursos.medico m on m.id = c.medico_id"
                + " where c.fecha between ? and ?"
                + " group by c.medico_id, m.nombres, m.apellidos, m.especialidad, c.fecha"
                + " order by c.medico_id, c.fecha";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, desde);
            stmt.setObject(2, hasta);
            try (ResultSet rs = stmt.executeQuery()) {
                List<AtendidasDia> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new AtendidasDia(rs.getLong("medico_id"), rs.getString("nombre"),
                            TipoEspecialidad.valueOf(rs.getString("especialidad")),
                            rs.getObject("fecha", LocalDate.class), rs.getInt("atendidas")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "contar las fichas atendidas por medico", e);
        }
    }

    @Override
    public List<ReporteGrupoDTO> solicitudesPorEspecialidad(LocalDate desde, LocalDate hasta) {
        return grupos("select s.especialidad as grupo, count(*) as cantidad from citas.solicitud s"
                + " where s.creado_en >= ? and s.creado_en < ? group by s.especialidad",
                "contar las solicitudes por especialidad", inicio(desde), finExclusivo(hasta));
    }

    @Override
    public List<ReporteGrupoDTO> atendidasPorConsultorio(LocalDate desde, LocalDate hasta) {
        return grupos("select co.nombre as grupo, count(*) as cantidad from citas.cita c"
                + " join recursos.consultorio co on co.id = c.consultorio_id"
                + " where c.estado = 'ATENDIDA' and c.fecha between ? and ? group by co.id, co.nombre",
                "contar las fichas atendidas por consultorio", desde, hasta);
    }

    @Override
    public IndicadoresDTO indicadores(LocalDate desde, LocalDate hasta) {
        String sqlSolicitudes = "select count(*) as solicitudes,"
                + " count(*) filter (where s.estado = 'SIN_CUPO') as sin_cupo"
                + " from citas.solicitud s where s.creado_en >= ? and s.creado_en < ?";
        String sqlCitas = "select count(*) filter (where c.estado = 'CANCELADA') as canceladas,"
                + " count(*) filter (where c.estado = 'CANCELADA' and " + CANCELADA_POR_SISTEMA + ") as automaticas,"
                + " count(*) filter (where c.estado <> 'CANCELADA') as fichas,"
                + " count(distinct (c.medico_id, c.fecha)) filter (where c.estado <> 'CANCELADA') as medico_dias"
                + " from citas.cita c where c.fecha between ? and ?";
        String sqlTiempo = "select avg(extract(epoch from (c.hora_fin - x.fecha_hora)) / 60) as minutos,"
                + " count(*) as muestras"
                + " from citas.cita c join citas.cambio_cita x"
                + "   on x.cita_id = c.id and x.accion = 'ASISTENCIA_CONFIRMADA'"
                + " where c.estado = 'ATENDIDA' and c.hora_fin is not null and c.hora_fin >= x.fecha_hora"
                + "   and c.fecha between ? and ?";
        try (Connection conn = DB.getConnection()) {
            int solicitudes;
            int sinCupo;
            try (PreparedStatement stmt = conn.prepareStatement(sqlSolicitudes)) {
                stmt.setTimestamp(1, inicio(desde));
                stmt.setTimestamp(2, finExclusivo(hasta));
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    solicitudes = rs.getInt("solicitudes");
                    sinCupo = rs.getInt("sin_cupo");
                }
            }
            int canceladas;
            int automaticas;
            Double fichasPorMedicoDia;
            try (PreparedStatement stmt = conn.prepareStatement(sqlCitas)) {
                stmt.setObject(1, desde);
                stmt.setObject(2, hasta);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    canceladas = rs.getInt("canceladas");
                    automaticas = rs.getInt("automaticas");
                    int medicoDias = rs.getInt("medico_dias");
                    fichasPorMedicoDia = medicoDias == 0 ? null : (double) rs.getInt("fichas") / medicoDias;
                }
            }
            Double minutos;
            int muestras;
            try (PreparedStatement stmt = conn.prepareStatement(sqlTiempo)) {
                stmt.setObject(1, desde);
                stmt.setObject(2, hasta);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    muestras = rs.getInt("muestras");
                    double promedio = rs.getDouble("minutos");
                    minutos = rs.wasNull() || muestras == 0 ? null : promedio;
                }
            }
            return new IndicadoresDTO(solicitudes, sinCupo, canceladas, automaticas, fichasPorMedicoDia, minutos,
                    muestras);
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "calcular los indicadores de gestion", e);
        }
    }

    private List<ReporteGrupoDTO> grupos(String sql, String accion, Object desde, Object hasta) {
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, desde);
            stmt.setObject(2, hasta);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReporteGrupoDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new ReporteGrupoDTO(rs.getString("grupo"), rs.getInt("cantidad")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, accion, e);
        }
    }

    private static Timestamp inicio(LocalDate dia) {
        return Timestamp.valueOf(dia.atStartOfDay());
    }

    private static Timestamp finExclusivo(LocalDate dia) {
        return Timestamp.valueOf(dia.plusDays(1).atStartOfDay());
    }

    private static LocalTime hora(Time t) {
        return t == null ? null : t.toLocalTime();
    }
}
