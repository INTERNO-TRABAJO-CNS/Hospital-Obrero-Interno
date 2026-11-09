package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** MedicoRepository en memoria. {@code medicos} es publica para que los tests verifiquen. */
public class FakeMedicoRepository implements MedicoRepository {

    public final List<MedicoDTO> medicos = new ArrayList<>();

    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized void store(MedicoDTO medico) {
        ultimoId = Math.max(ultimoId, medico.medicoId());
        for (int i = 0; i < medicos.size(); i++) {
            if (medicos.get(i).medicoId() == medico.medicoId()) {
                medicos.set(i, medico);
                return;
            }
        }
        medicos.add(medico);
    }

    @Override
    public synchronized Optional<MedicoDTO> porId(long medicoId) {
        return medicos.stream().filter(m -> m.medicoId() == medicoId).findFirst();
    }

    @Override
    public synchronized List<MedicoDTO> listar(TipoEspecialidad filtro) {
        return medicos.stream().filter(m -> filtro == null || m.especialidad() == filtro).toList();
    }
}
