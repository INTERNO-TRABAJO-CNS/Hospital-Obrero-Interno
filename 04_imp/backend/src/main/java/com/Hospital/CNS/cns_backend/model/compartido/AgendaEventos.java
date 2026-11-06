package com.Hospital.CNS.cns_backend.model.compartido;

/** Avisa a los clientes conectados por WebSocket (/ws/supervision y /ws/fichas). */
public interface AgendaEventos {
    void citaCambio(CitaDTO cita);
}
