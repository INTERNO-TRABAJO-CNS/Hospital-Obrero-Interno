package com.Hospital.CNS.cns_backend.infrastructure.planificador;

import com.Hospital.CNS.cns_backend.model.comunicacion.AvisoPrevioService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AvisoT45 {
    private final AvisoPrevioService service;

    public AvisoT45(AvisoPrevioService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 60000)
    public void ejecutar() {
        service.generarAvisosT45();
    }
}