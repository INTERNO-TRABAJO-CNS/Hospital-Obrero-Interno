package com.Hospital.CNS.cns_backend.infrastructure.eventos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;

/**
 * AgendaEventos provisional: solo deja el cambio en el log. T100-25 lo reemplaza por el envio real por WebSocket
 * (/ws/supervision y /ws/fichas) sin tocar el model.
 */
public class RegistroAgendaEventos implements AgendaEventos {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroAgendaEventos.class);

    @Override
    public void citaCambio(CitaDTO cita) {
        LOG.info("Cita {} {}: medico {} el {} a las {} (ficha {})", cita.citaId(), cita.estado(), cita.medicoId(),
                cita.fecha(), cita.hora(), cita.numeroFicha());
    }
}
