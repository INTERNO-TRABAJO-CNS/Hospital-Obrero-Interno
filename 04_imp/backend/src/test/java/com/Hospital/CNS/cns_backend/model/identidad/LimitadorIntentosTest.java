package com.Hospital.CNS.cns_backend.model.identidad;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;

class LimitadorIntentosTest {

    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(2026, 11, 13, 9, 0));
    private final LimitadorIntentos limitador =
            new LimitadorIntentos(3, Duration.ofMinutes(15), Duration.ofMinutes(10), reloj);

    @Test
    void bloqueaTrasElMaximo_yDesbloqueaAlPasarElTiempo() {
        limitador.registrarFallo("login:juan");
        limitador.registrarFallo("login:juan");
        assertDoesNotThrow(() -> limitador.comprobar("login:juan"));

        limitador.registrarFallo("login:juan");
        DominioException e = assertThrows(DominioException.class, () -> limitador.comprobar("login:juan"));
        assertEquals("INTENTOS_EXCEDIDOS", e.getCodigo());

        reloj.avanzar(Duration.ofMinutes(10));
        assertDoesNotThrow(() -> limitador.comprobar("login:juan"));
    }

    @Test
    void fallosFueraDeLaVentana_noSeAcumulan() {
        limitador.registrarFallo("login:ana");
        limitador.registrarFallo("login:ana");
        reloj.avanzar(Duration.ofMinutes(16));
        limitador.registrarFallo("login:ana");

        assertDoesNotThrow(() -> limitador.comprobar("login:ana"));
    }

    @Test
    void unIntentoCorrecto_borraLosFallos_yCadaClaveEsIndependiente() {
        limitador.registrarFallo("login:juan");
        limitador.registrarFallo("login:juan");
        limitador.limpiar("login:juan");
        limitador.registrarFallo("login:juan");
        limitador.registrarFallo("login:otro");
        limitador.registrarFallo("login:otro");

        assertDoesNotThrow(() -> limitador.comprobar("login:juan"));
        assertDoesNotThrow(() -> limitador.comprobar("login:otro"));
    }
}
