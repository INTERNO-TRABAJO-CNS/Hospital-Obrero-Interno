package com.Hospital.CNS.cns_backend.model.compartido;

/**
 * Estados de una cita. La cita nunca se borra: se pasa a CANCELADA.
 * PROGRAMADA -> ASISTENCIA_CONFIRMADA -> ATENDIDA, o CANCELADA.
 */
public enum EstadoCita {
    PROGRAMADA,
    ASISTENCIA_CONFIRMADA,
    ATENDIDA,
    CANCELADA
}
