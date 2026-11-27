package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** DiaNoHabilRepository en memoria. {@code dias} es publica para que los tests verifiquen. */
public class FakeDiaNoHabilRepository implements DiaNoHabilRepository {

    public final List<DiaNoHabilDTO> dias = new ArrayList<>();

    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized void store(DiaNoHabilDTO dia) {
        ultimoId = Math.max(ultimoId, dia.diaNoHabilId());
        for (int i = 0; i < dias.size(); i++) {
            if (dias.get(i).diaNoHabilId() == dia.diaNoHabilId()) {
                dias.set(i, dia);
                return;
            }
        }
        dias.add(dia);
    }

    @Override
    public synchronized Optional<DiaNoHabilDTO> porId(long diaNoHabilId) {
        return dias.stream().filter(d -> d.diaNoHabilId() == diaNoHabilId).findFirst();
    }

    @Override
    public synchronized List<DiaNoHabilDTO> listar() {
        return dias.stream().sorted(Comparator.comparing(DiaNoHabilDTO::fecha)).toList();
    }

    @Override
    public synchronized boolean existeActivoEnFecha(LocalDate fecha) {
        return dias.stream().anyMatch(d -> d.activo() && d.fecha().equals(fecha));
    }
}
