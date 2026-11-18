package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;

/**
 * AgendaRepository en memoria sobre un {@link FakeCitaRepository}. {@link #tomarPorOtro} simula que otra solicitud
 * simultanea tiene la agenda (como el SKIP LOCKED de PgAgendaRepository). La duracion de cada medico se configura con
 * {@link #duracion} (por defecto 30 min), igual que recursos.medico.duracion_cita_min.
 */
public class FakeAgendaRepository implements AgendaRepository {

    private static final int DURACION_POR_DEFECTO = 30;

    private record Agenda(long medicoId, LocalDate fecha) {
    }

    private final FakeCitaRepository citas;
    private final Set<Agenda> tomadasPorOtro = new HashSet<>();
    private final Map<Long, Integer> duraciones = new HashMap<>();
    public int intentosDeBloqueo = 0;

    public FakeAgendaRepository(FakeCitaRepository citas) {
        this.citas = citas;
    }

    public void tomarPorOtro(long medicoId, LocalDate fecha) {
        tomadasPorOtro.add(new Agenda(medicoId, fecha));
    }

    public void duracion(long medicoId, int minutos) {
        duraciones.put(medicoId, minutos);
    }

    @Override
    public boolean intentarBloquear(long medicoId, LocalDate fecha) {
        intentosDeBloqueo++;
        return !tomadasPorOtro.contains(new Agenda(medicoId, fecha));
    }

    @Override
    public List<CitaDTO> ocupacionDelMedico(long medicoId, LocalDate fecha) {
        return citas.delMedicoEnFecha(medicoId, fecha).stream()
                .filter(c -> c.estado() != EstadoCita.CANCELADA)
                .toList();
    }

    @Override
    public boolean consultorioOcupado(int consultorioId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        return citas.delDia(fecha).stream()
                .filter(c -> c.consultorio() == consultorioId && c.estado() != EstadoCita.CANCELADA)
                .anyMatch(c -> c.hora().isBefore(fin)
                        && c.hora().plusMinutes(duraciones.getOrDefault(c.medicoId(), DURACION_POR_DEFECTO))
                                .isAfter(inicio));
    }
}
