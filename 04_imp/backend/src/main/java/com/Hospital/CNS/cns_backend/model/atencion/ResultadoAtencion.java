package com.Hospital.CNS.cns_backend.model.atencion;

/** Resultado de CU-15 al registrar la receta y el fin de la atencion. */
public enum ResultadoAtencion {
    REGISTRADA,
    RECETA_INVALIDA,
    NO_ES_DE_SU_AGENDA,
    ESTADO_NO_PERMITE,
    NO_ENCONTRADA
}
