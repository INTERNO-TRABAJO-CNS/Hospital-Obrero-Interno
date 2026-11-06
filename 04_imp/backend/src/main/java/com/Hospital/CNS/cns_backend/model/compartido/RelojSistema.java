package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;

/** Reloj real, con la hora del servidor. */
public class RelojSistema implements Reloj {

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}
