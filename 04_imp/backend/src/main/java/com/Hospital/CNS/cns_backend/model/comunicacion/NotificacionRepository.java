package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;

public interface NotificacionRepository {
    List<NotificacionDTO> delPaciente(long pacienteId);

    boolean avisoYaEnviado(long citaId);
}