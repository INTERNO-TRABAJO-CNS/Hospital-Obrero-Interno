package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.Duration;
import java.time.LocalDateTime;

/** Reloj de pruebas: siempre devuelve la hora indicada, hasta que el test la mueva. */
public class RelojFijo implements Reloj {

    private volatile LocalDateTime ahora;

    public RelojFijo(LocalDateTime ahora) {
        this.ahora = ahora;
    }

    @Override
    public LocalDateTime ahora() {
        return ahora;
    }

    public void fijar(LocalDateTime nuevaHora) {
        this.ahora = nuevaHora;
    }

    public void avanzar(Duration duracion) {
        this.ahora = this.ahora.plus(duracion);
    }
}
