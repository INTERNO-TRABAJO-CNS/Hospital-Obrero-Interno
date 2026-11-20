package com.Hospital.CNS.cns_backend.infrastructure.planificador;

import java.time.Instant;

import com.Hospital.CNS.cns_backend.model.comunicacion.Reloj;
import org.springframework.stereotype.Component;

@Component
public class RelojSistema implements Reloj {
    @Override
    public Instant ahora() {
        return Instant.now();
    }
}