package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;

/**
 * Aviso para el paciente. Se refiere a una cita comun, a una ficha interna o a una cirugia (los otros dos
 * van en null). Se guarda en notificacion.notificacion (decision del 15/11/2026).
 */
public record NotificacionDTO(long notificacionId, long pacienteId, Long citaId, Long fichaId, Long cirugiaId,
                              String tipo, String mensaje, LocalDateTime fechaHora) {

    public static NotificacionDTO deFicha(long pacienteId, long fichaId, String tipo, String mensaje,
                                          LocalDateTime fechaHora) {
        return new NotificacionDTO(0, pacienteId, null, fichaId, null, tipo, mensaje, fechaHora);
    }

    public static NotificacionDTO deCirugia(long pacienteId, long cirugiaId, String tipo, String mensaje,
                                            LocalDateTime fechaHora) {
        return new NotificacionDTO(0, pacienteId, null, null, cirugiaId, tipo, mensaje, fechaHora);
    }
}
