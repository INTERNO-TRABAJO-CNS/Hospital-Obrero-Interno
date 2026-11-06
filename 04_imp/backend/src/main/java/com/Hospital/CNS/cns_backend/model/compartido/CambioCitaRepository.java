package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.util.List;

public interface CambioCitaRepository {
    void registrar(CambioCitaDTO cambio);

    List<CambioCitaDTO> deCita(long citaId);

    List<CambioCitaDTO> buscar(Long citaId, String usuario, LocalDate desde, LocalDate hasta);
}
