package com.Hospital.CNS.cns_backend.model.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RelojFijoTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 9, 8, 0);

    @Test
    void devuelveSiempreLaHoraIndicada() {
        RelojFijo reloj = new RelojFijo(BASE);

        assertEquals(BASE, reloj.ahora());
        assertEquals(BASE, reloj.ahora());
    }

    @Test
    void avanzaYFijaLaHora() {
        RelojFijo reloj = new RelojFijo(BASE);

        reloj.avanzar(Duration.ofMinutes(45));
        assertEquals(BASE.plusMinutes(45), reloj.ahora());

        reloj.fijar(BASE.plusDays(1));
        assertEquals(BASE.plusDays(1), reloj.ahora());
    }
}
