package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.util.List;

public interface NotificacionRepository {
    void store(NotificacionDTO notificacion);

    List<NotificacionDTO> delPaciente(long pacienteId);

    void marcarAvisoEnviado(long citaId);

    boolean avisoYaEnviado(long citaId);
}