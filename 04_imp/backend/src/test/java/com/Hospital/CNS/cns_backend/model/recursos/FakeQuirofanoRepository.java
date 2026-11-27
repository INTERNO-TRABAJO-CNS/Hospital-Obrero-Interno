package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** QuirofanoRepository en memoria (nombre unico entre los activos). {@code quirofanos} es publica. */
public class FakeQuirofanoRepository implements QuirofanoRepository {

    public final List<QuirofanoDTO> quirofanos = new ArrayList<>();
    private int ultimo;

    @Override
    public synchronized int nextIdentity() {
        return ++ultimo;
    }

    @Override
    public synchronized boolean store(QuirofanoDTO q) {
        boolean repetido = q.activo() && quirofanos.stream().anyMatch(o -> o.quirofanoId() != q.quirofanoId()
                && o.activo() && o.nombre().equalsIgnoreCase(q.nombre()));
        if (repetido) {
            return false;
        }
        quirofanos.removeIf(o -> o.quirofanoId() == q.quirofanoId());
        quirofanos.add(q);
        ultimo = Math.max(ultimo, q.quirofanoId());
        return true;
    }

    @Override
    public synchronized Optional<QuirofanoDTO> porId(int quirofanoId) {
        return quirofanos.stream().filter(q -> q.quirofanoId() == quirofanoId).findFirst();
    }

    @Override
    public synchronized List<QuirofanoDTO> listar() {
        List<QuirofanoDTO> lista = new ArrayList<>(quirofanos);
        lista.sort(Comparator.comparing(QuirofanoDTO::nombre));
        return lista;
    }

    @Override
    public synchronized List<QuirofanoDTO> activosDe(TipoEspecialidad especialidad) {
        return listar().stream().filter(q -> q.activo() && q.especialidad() == especialidad).toList();
    }
}
