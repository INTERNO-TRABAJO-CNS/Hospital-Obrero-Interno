package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;

/**
 * Solicitud de cita. {@code prioridad}: 1 normal, 2 tras cancelar o reprogramar.
 * {@code citaId} es null hasta que la solicitud pasa a ASIGNADA.
 */
public record SolicitudDTO(long solicitudId, long pacienteId, TipoEspecialidad especialidad,
                           LocalDate diaSolicitado, int prioridad,
                           long ordenLlegada, EstadoSolicitud estado, Long citaId) {
}
