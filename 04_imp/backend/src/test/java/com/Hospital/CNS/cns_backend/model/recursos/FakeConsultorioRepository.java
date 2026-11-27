package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** ConsultorioRepository en memoria. {@code consultorios} es publica para que los tests verifiquen. */
public class FakeConsultorioRepository implements ConsultorioRepository {

    public final List<ConsultorioDTO> consultorios = new ArrayList<>();

    private int ultimoId = 0;

    @Override
    public synchronized int nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized void store(ConsultorioDTO consultorio) {
        ultimoId = Math.max(ultimoId, consultorio.consultorioId());
        for (int i = 0; i < consultorios.size(); i++) {
            if (consultorios.get(i).consultorioId() == consultorio.consultorioId()) {
                consultorios.set(i, consultorio);
                return;
            }
        }
        consultorios.add(consultorio);
    }

    @Override
    public synchronized Optional<ConsultorioDTO> porId(int consultorioId) {
        return consultorios.stream().filter(c -> c.consultorioId() == consultorioId).findFirst();
    }

    @Override
    public synchronized List<ConsultorioDTO> listar() {
        return List.copyOf(consultorios);
    }

    @Override
    public synchronized boolean existeActivoConNombre(String nombre) {
        return consultorios.stream().anyMatch(c -> c.activo() && c.nombre().equalsIgnoreCase(nombre));
    }
}
