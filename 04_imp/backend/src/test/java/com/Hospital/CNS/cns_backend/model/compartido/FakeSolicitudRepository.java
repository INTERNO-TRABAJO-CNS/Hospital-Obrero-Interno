package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** SolicitudRepository en memoria. {@code solicitudes} es publica para que los tests verifiquen. */
public class FakeSolicitudRepository implements SolicitudRepository {

    public final List<SolicitudDTO> solicitudes = new ArrayList<>();

    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    /** Si ya existe una solicitud con ese id la reemplaza. */
    @Override
    public synchronized void store(SolicitudDTO s) {
        ultimoId = Math.max(ultimoId, s.solicitudId());
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).solicitudId() == s.solicitudId()) {
                solicitudes.set(i, s);
                return;
            }
        }
        solicitudes.add(s);
    }

    @Override
    public synchronized Optional<SolicitudDTO> porId(long solicitudId) {
        return solicitudes.stream().filter(s -> s.solicitudId() == solicitudId).findFirst();
    }

    @Override
    public synchronized boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia) {
        return solicitudes.stream()
                .anyMatch(s -> s.pacienteId() == pacienteId && s.diaSolicitado().equals(dia) && esActiva(s));
    }

    /** PENDIENTE o EN_ESPERA con dia solicitado hasta {@code hasta}; orden: prioridad y luego ordenLlegada. */
    @Override
    public synchronized List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max) {
        return solicitudes.stream()
                .filter(s -> esActiva(s) && !s.diaSolicitado().isAfter(hasta))
                .sorted(Comparator.comparingInt(SolicitudDTO::prioridad).thenComparingLong(SolicitudDTO::ordenLlegada))
                .limit(max)
                .toList();
    }

    private static boolean esActiva(SolicitudDTO s) {
        return s.estado() == EstadoSolicitud.PENDIENTE || s.estado() == EstadoSolicitud.EN_ESPERA;
    }
}
