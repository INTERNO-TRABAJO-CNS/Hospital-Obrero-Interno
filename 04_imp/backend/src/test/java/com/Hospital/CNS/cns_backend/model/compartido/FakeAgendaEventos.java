package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.ArrayList;
import java.util.List;

/** AgendaEventos en memoria. {@code eventos} es publica para que los tests verifiquen. */
public class FakeAgendaEventos implements AgendaEventos {

    public final List<CitaDTO> eventos = new ArrayList<>();

    @Override
    public synchronized void citaCambio(CitaDTO cita) {
        eventos.add(cita);
    }
}
