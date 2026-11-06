package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;

/** Fuente de la hora actual. En produccion RelojSistema; en pruebas RelojFijo. */
public interface Reloj {
    LocalDateTime ahora();
}
