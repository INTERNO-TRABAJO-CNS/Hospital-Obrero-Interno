package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.infrastructure.ws.NotificacionesWs;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;

/**
 * Guarda los avisos al paciente en notificacion.notificacion (decision del 15/11/2026). Dentro de una
 * transaccion se guardan junto con la ficha o la cirugia: si algo falla, no queda un aviso suelto.
 */
public class PgNotificacionPublicador implements NotificacionPublicador {

    private static final Logger LOG = LoggerFactory.getLogger(PgNotificacionPublicador.class);
    private static final int MAX_MENSAJE = 500;

    private final NotificacionesWs websocket;

    public PgNotificacionPublicador(NotificacionesWs websocket) {
        this.websocket = websocket;
    }

    @Override
    public void publicar(NotificacionDTO n) {
        String sql = "insert into notificacion.notificacion (paciente_id, cita_id, ficha_id, cirugia_id, tipo, mensaje, "
                + "fecha_hora) values (?, ?, ?, ?, ?, ?, ?) "
                + "on conflict (cita_id, tipo) where tipo = 'AVISO_T45' do nothing";
        String mensaje = n.mensaje().length() > MAX_MENSAJE ? n.mensaje().substring(0, MAX_MENSAJE) : n.mensaje();
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, n.pacienteId());
            stmt.setObject(2, n.citaId());
            stmt.setObject(3, n.fichaId());
            stmt.setObject(4, n.cirugiaId());
            stmt.setString(5, n.tipo());
            stmt.setString(6, mensaje);
            stmt.setTimestamp(7, Timestamp.valueOf(n.fechaHora()));
            if (stmt.executeUpdate() == 1) {
                DB.despuesDeCommit(() -> websocket.enviar(n));
            }
        } catch (SQLException e) {
            throw ErroresPg.traducir(LOG, "guardar el aviso " + n.tipo(), e);
        }
    }
}
