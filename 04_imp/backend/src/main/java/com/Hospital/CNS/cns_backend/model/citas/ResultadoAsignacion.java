package com.Hospital.CNS.cns_backend.model.citas;

/** Lo que paso con la solicitud que tomo CU-05. */
public enum ResultadoAsignacion {
    /** Se registro la cita PROGRAMADA (CU-07) y la solicitud quedo ASIGNADA. */
    ASIGNADA,
    /** [2a] NO HAY CUPOS ese dia: espera al siguiente dia habil. [3b] otra solicitud tenia el cupo: reintenta. */
    EN_ESPERA,
    /** [2b] Se agotaron los 15 dias habiles sin cupo. */
    SIN_CUPO,
    /** No hay solicitudes PENDIENTE ni EN_ESPERA para procesar. */
    NADA_PENDIENTE
}
