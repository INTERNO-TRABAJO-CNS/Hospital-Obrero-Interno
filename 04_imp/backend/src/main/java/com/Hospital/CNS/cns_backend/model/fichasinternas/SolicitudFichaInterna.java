package com.Hospital.CNS.cns_backend.model.fichasinternas;

import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Lo que envia el medico general o el pediatra en "Ticket interno". Con cita: {@code citaOrigenId} (el paciente
 * sale de la cita). Urgencia sin cita: {@code pacienteId} (buscado por N° de afiliado).
 */
public record SolicitudFichaInterna(Long citaOrigenId, Long pacienteId, TipoEspecialidad especialidad,
                                    Gravedad gravedad, boolean urgencia, String motivo) {
}
