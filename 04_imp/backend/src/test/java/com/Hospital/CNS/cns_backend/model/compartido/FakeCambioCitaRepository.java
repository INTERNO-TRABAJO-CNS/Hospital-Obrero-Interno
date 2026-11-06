package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CambioCitaRepository en memoria. {@code cambios} es publica para que los tests verifiquen. */
public class FakeCambioCitaRepository implements CambioCitaRepository {

    public final List<CambioCitaDTO> cambios = new ArrayList<>();

    @Override
    public synchronized void registrar(CambioCitaDTO cambio) {
        cambios.add(cambio);
    }

    @Override
    public synchronized List<CambioCitaDTO> deCita(long citaId) {
        return cambios.stream().filter(c -> c.citaId() == citaId).toList();
    }

    /** Cada filtro null se ignora; {@code desde} y {@code hasta} incluyen el dia indicado. */
    @Override
    public synchronized List<CambioCitaDTO> buscar(Long citaId, String usuario, LocalDate desde, LocalDate hasta) {
        return cambios.stream()
                .filter(c -> citaId == null || c.citaId() == citaId)
                .filter(c -> usuario == null || usuario.equals(c.usuario()))
                .filter(c -> desde == null || !c.fechaHora().toLocalDate().isBefore(desde))
                .filter(c -> hasta == null || !c.fechaHora().toLocalDate().isAfter(hasta))
                .toList();
    }
}
