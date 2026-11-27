package com.Hospital.CNS.cns_backend.model.citas;

/** Respuesta de CU-06. Cada valor distinto de DISPONIBLE es un flujo alternativo del diagrama. */
public enum ResultadoDisponibilidad {
    /** Camino principal: la combinacion se puede confirmar. */
    DISPONIBLE,
    /** [2a] El medico ya tiene una cita a esa misma hora. */
    MEDICO_OCUPADO,
    /** [3a] El consultorio ya esta ocupado en ese intervalo. */
    CONSULTORIO_OCUPADO,
    /** [4a] El medico ya tiene 7 citas ese dia (sin contar CANCELADA). */
    MEDICO_COMPLETO,
    /** [4a] El paciente ya tiene una cita ese dia (en cualquier especialidad). */
    PACIENTE_YA_TIENE_CITA,
    /** T100-24: el paciente tiene una ficha interna o una cirugia que se cruza con ese intervalo. */
    PACIENTE_OCUPADO,
    /** [5a] El intervalo se cruza con otra cita del medico. */
    SOLAPADA,
    /** [5a] Otra solicitud simultanea tiene la agenda del medico en ese dia. */
    BLOQUEADA_POR_OTRO
}
