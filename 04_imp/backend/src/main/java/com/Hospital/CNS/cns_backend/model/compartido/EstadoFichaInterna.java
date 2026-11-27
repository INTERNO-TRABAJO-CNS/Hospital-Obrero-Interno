package com.Hospital.CNS.cns_backend.model.compartido;

/**
 * EN_ESPERA (sin hueco todavia) -> ASIGNADA -> ATENDIDA, o ASIGNADA -> CANCELADA ("no se presento"; el horario
 * no se reasigna). Sin confirmacion de asistencia.
 */
public enum EstadoFichaInterna {
    EN_ESPERA, ASIGNADA, ATENDIDA, CANCELADA
}
