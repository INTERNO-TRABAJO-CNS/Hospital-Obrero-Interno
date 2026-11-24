package com.Hospital.CNS.cns_backend.infrastructure.planificador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaService;

/**
 * T100-20: el unico proceso programado nuevo. Al inicio de cada dia habil (06:00, hora del hospital) las fichas
 * EN_ESPERA buscan un hueco libre en orden de prioridad, sin desplazar a nadie. En dias no habiles no hace nada.
 */
public class ProcesadorFichasEnEspera {

    private static final Logger LOG = LoggerFactory.getLogger(ProcesadorFichasEnEspera.class);

    private final FichaInternaService service;

    public ProcesadorFichasEnEspera(FichaInternaService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 0 6 * * MON-FRI", zone = "${cns.zona-horaria:America/La_Paz}")
    public void procesar() {
        try {
            int asignadas = service.procesarEnEspera();
            LOG.info("Fichas internas en espera asignadas hoy: {}", asignadas);
        } catch (RuntimeException e) {
            LOG.error("No se pudieron procesar las fichas en espera", e);
        }
    }
}
