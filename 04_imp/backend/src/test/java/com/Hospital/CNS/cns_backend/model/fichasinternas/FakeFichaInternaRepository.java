package com.Hospital.CNS.cns_backend.model.fichasinternas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** FichaInternaRepository en memoria. {@code fichas} y {@code derivaciones} son publicas para los tests. */
public class FakeFichaInternaRepository implements FichaInternaRepository {

    public final Map<Long, FichaInternaDTO> fichas = new HashMap<>();
    public final Map<Long, Long> derivaciones = new HashMap<>();
    public int bloqueos;
    private long siguiente = 1001;

    @Override
    public synchronized long nextIdentity() {
        return siguiente++;
    }

    @Override
    public synchronized void insertar(FichaInternaDTO ficha) {
        if (fichas.containsKey(ficha.fichaId())) {
            throw new IllegalStateException("ficha repetida " + ficha.fichaId());
        }
        fichas.put(ficha.fichaId(), ficha);
    }

    @Override
    public synchronized void guardarAsignacion(FichaInternaDTO ficha) {
        fichas.put(ficha.fichaId(), ficha);
    }

    @Override
    public synchronized boolean cambiarEstado(long fichaId, EstadoFichaInterna desde, EstadoFichaInterna hasta,
                                              String observaciones) {
        FichaInternaDTO f = fichas.get(fichaId);
        if (f == null || f.estado() != desde) {
            return false;
        }
        fichas.put(fichaId, f.conEstado(hasta, observaciones));
        return true;
    }

    @Override
    public synchronized boolean registrarDerivacion(long citaOrigenId, long fichaId) {
        return derivaciones.putIfAbsent(citaOrigenId, fichaId) == null;
    }

    @Override
    public synchronized Optional<FichaInternaDTO> porId(long fichaId) {
        return Optional.ofNullable(fichas.get(fichaId));
    }

    @Override
    public synchronized Optional<FichaInternaDTO> deCita(long citaOrigenId) {
        return fichas.values().stream().filter(f -> Long.valueOf(citaOrigenId).equals(f.citaOrigenId())).findFirst();
    }

    @Override
    public synchronized boolean pacienteTieneFichaActiva(long pacienteId) {
        return fichas.values().stream().anyMatch(f -> f.pacienteId() == pacienteId && f.activa());
    }

    @Override
    public synchronized List<FichaInternaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha) {
        return filtrar(f -> Long.valueOf(medicoId).equals(f.medicoId()) && fecha.equals(f.fecha()));
    }

    @Override
    public synchronized List<FichaInternaDTO> asignadasEntre(TipoEspecialidad especialidad, LocalDate desde,
                                                             LocalDate hasta) {
        return filtrar(f -> f.especialidad() == especialidad && f.estado() == EstadoFichaInterna.ASIGNADA
                && !f.fecha().isBefore(desde) && !f.fecha().isAfter(hasta));
    }

    @Override
    public synchronized List<FichaInternaDTO> enEspera(TipoEspecialidad especialidad) {
        return filtrar(f -> f.especialidad() == especialidad && f.estado() == EstadoFichaInterna.EN_ESPERA);
    }

    @Override
    public synchronized List<FichaInternaDTO> activas(TipoEspecialidad especialidad, boolean pediatrica) {
        return filtrar(f -> f.especialidad() == especialidad && f.pediatrica() == pediatrica && f.activa());
    }

    @Override
    public synchronized List<FichaInternaDTO> delPaciente(long pacienteId) {
        return filtrar(f -> f.pacienteId() == pacienteId);
    }

    @Override
    public synchronized boolean medicoTieneFichaConPaciente(long medicoId, long pacienteId) {
        return fichas.values().stream().anyMatch(f -> f.pacienteId() == pacienteId
                && (f.medicoDerivanteId() == medicoId || Long.valueOf(medicoId).equals(f.medicoId())));
    }

    @Override
    public synchronized void bloquearEspecialidad(TipoEspecialidad especialidad) {
        bloqueos++;
    }

    private List<FichaInternaDTO> filtrar(java.util.function.Predicate<FichaInternaDTO> condicion) {
        List<FichaInternaDTO> lista = new ArrayList<>();
        for (FichaInternaDTO f : fichas.values()) {
            if (condicion.test(f)) {
                lista.add(f);
            }
        }
        lista.sort(java.util.Comparator.comparingLong(FichaInternaDTO::fichaId));
        return lista;
    }
}
