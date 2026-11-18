package com.Hospital.CNS.cns_backend.infrastructure.planificador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.Hospital.CNS.cns_backend.model.citas.AsignacionService;

/**
 * T100-03 (CU-05): cada 5 segundos (contados desde que termina la vuelta anterior) procesa hasta 20 solicitudes de
 * la cola. Un error no detiene el proceso: queda en el log y se reintenta en la siguiente vuelta.
 */
public class ProcesadorCola {

    public static final int MAXIMO_POR_VUELTA = 20;

    private static final Logger LOG = LoggerFactory.getLogger(ProcesadorCola.class);

    private final AsignacionService service;

    public ProcesadorCola(AsignacionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 5000)
    public void procesar() {
        try {
            int procesadas = service.procesarCola(MAXIMO_POR_VUELTA);
            if (procesadas > 0) {
                LOG.info("Cola de solicitudes: {} procesadas", procesadas);
            }
        } catch (RuntimeException e) {
            LOG.error("No se pudo procesar la cola de solicitudes", e);
        }
    }
}
