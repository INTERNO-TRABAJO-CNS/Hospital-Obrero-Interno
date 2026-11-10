package com.Hospital.CNS.cns_backend.model.recursos;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/** Datos del medico autenticado para la web Nuxt. Un medico dado de baja no entra (ACCESO_DENEGADO). */
public class MedicoActualService {

    private final MedicoRepository medicos;

    public MedicoActualService(MedicoRepository medicos) {
        this.medicos = medicos;
    }

    public MedicoActualDTO yo(long medicoId) {
        MedicoDTO m = medicos.porId(medicoId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No existe un medico con ese id"));
        if (!m.activo()) {
            throw new DominioException("ACCESO_DENEGADO", "El medico esta dado de baja");
        }
        return new MedicoActualDTO(m.medicoId(), (m.nombres() + " " + m.apellidos()).trim(), m.especialidad(),
                m.especialidad().puedeDerivar(), m.especialidad().operaEnQuirofano());
    }
}
