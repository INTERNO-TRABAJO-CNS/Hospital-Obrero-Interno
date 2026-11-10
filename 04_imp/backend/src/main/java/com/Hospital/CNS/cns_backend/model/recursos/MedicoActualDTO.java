package com.Hospital.CNS.cns_backend.model.recursos;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** El medico que usa la web: arma su menu (puedeDerivar -> «Ticket interno»; operaEnQuirofano -> «Cirugias»). */
public record MedicoActualDTO(long medicoId, String nombre, TipoEspecialidad especialidad, boolean puedeDerivar,
                              boolean operaEnQuirofano) {
}
