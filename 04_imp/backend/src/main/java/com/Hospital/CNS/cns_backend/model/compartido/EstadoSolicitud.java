package com.Hospital.CNS.cns_backend.model.compartido;

/**
 * Estados de una solicitud de cita.
 * SIN_CUPO: la solicitud estuvo EN_ESPERA 15 dias habiles sin conseguir cupo.
 */
public enum EstadoSolicitud {
    PENDIENTE,
    EN_ESPERA,
    ASIGNADA,
    SIN_CUPO
}
