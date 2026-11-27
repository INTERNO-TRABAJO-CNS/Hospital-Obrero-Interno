package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** HorarioRepository en memoria. {@code horarios} es publica para que los tests verifiquen. */
public class FakeHorarioRepository implements HorarioRepository {

    public final List<HorarioDTO> horarios = new ArrayList<>();

    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized void store(HorarioDTO horario) {
        ultimoId = Math.max(ultimoId, horario.horarioId());
        for (int i = 0; i < horarios.size(); i++) {
            if (horarios.get(i).horarioId() == horario.horarioId()) {
                horarios.set(i, horario);
                return;
            }
        }
        horarios.add(horario);
    }

    @Override
    public synchronized Optional<HorarioDTO> porId(long horarioId) {
        return horarios.stream().filter(h -> h.horarioId() == horarioId).findFirst();
    }

    @Override
    public synchronized List<HorarioDTO> listar() {
        return List.copyOf(horarios);
    }

    @Override
    public synchronized List<HorarioDTO> delMedico(long medicoId) {
        return horarios.stream().filter(h -> h.medicoId() == medicoId).toList();
    }

    @Override
    public synchronized List<HorarioDTO> delConsultorio(int consultorioId) {
        return horarios.stream().filter(h -> h.consultorioId() == consultorioId).toList();
    }
}
