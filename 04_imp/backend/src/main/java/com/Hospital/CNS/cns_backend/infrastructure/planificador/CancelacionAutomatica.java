package com.Hospital.CNS.cns_backend.infrastructure.planificador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import com.Hospital.CNS.cns_backend.model.operaciones.CancelacionService;

@Component
public class CancelacionAutomatica {

    private static final Logger LOG = LoggerFactory.getLogger(CancelacionAutomatica.class);

    private final CancelacionService service;

    public CancelacionAutomatica(CancelacionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 60000)
    public void cancelarAutomaticas() {
        try {
            int canceladas = service.cancelarAutomaticas();
            LOG.info("Citas canceladas automaticamente por falta de confirmacion: {}", canceladas);
        } catch (RuntimeException e) {
            LOG.error("No se pudieron cancelar automaticamente las citas sin confirmacion", e);
        }
    }
}