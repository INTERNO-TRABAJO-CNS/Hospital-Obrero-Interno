package com.Hospital.CNS.cns_backend.model.recursos;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Datos de un medico que viajan entre controller, service y repository. */
public record MedicoDTO(long medicoId, String nombres, String apellidos, TipoEspecialidad especialidad,
                        int duracionCitaMin, int cupoMaximoDia, boolean activo) {
}
