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
    private long ultimoOrden = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    /**
     * Si ya existe una solicitud con ese id la reemplaza (conservando su orden de llegada, como la base). Como la
     * secuencia citas.solicitud_orden_seq, una solicitud nueva con ordenLlegada 0 recibe el siguiente orden.
     */
    @Override
    public synchronized void store(SolicitudDTO s) {
        ultimoId = Math.max(ultimoId, s.solicitudId());
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).solicitudId() == s.solicitudId()) {
                solicitudes.set(i, conOrden(s, solicitudes.get(i).ordenLlegada()));
                return;
            }
        }
        solicitudes.add(conOrden(s, s.ordenLlegada() > 0 ? s.ordenLlegada() : ultimoOrden + 1));
    }

    private SolicitudDTO conOrden(SolicitudDTO s, long orden) {
        ultimoOrden = Math.max(ultimoOrden, orden);
        return new SolicitudDTO(s.solicitudId(), s.pacienteId(), s.especialidad(), s.diaSolicitado(), s.prioridad(),
                orden, s.estado(), s.citaId());
    }

    @Override
    public synchronized Optional<SolicitudDTO> porId(long solicitudId) {
        return solicitudes.stream()
                .filter(s -> s.solicitudId() == solicitudId)
                .findFirst();
    }

    /** Lista las solicitudes de un paciente, de la más reciente a la más antigua. */
    @Override
    public synchronized List<SolicitudDTO> porPaciente(long pacienteId) {
        return solicitudes.stream()
                .filter(s -> s.pacienteId() == pacienteId)
                .sorted(Comparator.comparingLong(SolicitudDTO::ordenLlegada).reversed())
                .toList();
    }

    @Override
    public synchronized boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia) {
        return solicitudes.stream()
                .anyMatch(s -> s.pacienteId() == pacienteId
                        && s.diaSolicitado().equals(dia)
                        && esActiva(s));
    }

    /** Pacientes de 60 anios o mas (en PostgreSQL se calcula con su fecha de nacimiento). */
    public final java.util.Set<Long> adultosMayores = new java.util.HashSet<>();

    /**
     * PENDIENTE o EN_ESPERA con dia solicitado hasta {@code hasta}; orden: prioridad, adulto mayor primero y luego
     * ordenLlegada (T100-24).
     */
    @Override
    public synchronized List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max) {
        return solicitudes.stream()
                .filter(s -> esActiva(s) && !s.diaSolicitado().isAfter(hasta))
                .sorted(Comparator.comparingInt(SolicitudDTO::prioridad)
                        .thenComparing(s -> !adultosMayores.contains(s.pacienteId()))
                        .thenComparingLong(SolicitudDTO::ordenLlegada))
                .limit(max)
                .toList();
    }

    private static boolean esActiva(SolicitudDTO s) {
        return s.estado() == EstadoSolicitud.PENDIENTE
                || s.estado() == EstadoSolicitud.EN_ESPERA;
    }
}