package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CitaRepository en memoria. {@code citas} y {@code horasFin} son publicas para que los tests verifiquen.
 * Simula UNIQUE(medico, fecha, hora) entre citas no CANCELADA y el UPDATE atomico de cambiarEstado.
 */
public class FakeCitaRepository implements CitaRepository {

    public final List<CitaDTO> citas = new ArrayList<>();
    public final Map<Long, LocalDateTime> horasFin = new HashMap<>();

    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized boolean store(CitaDTO cita) {
        if (cita.estado() != EstadoCita.CANCELADA) {
            for (CitaDTO otra : citas) {
                boolean mismoHorario = otra.medicoId() == cita.medicoId()
                        && otra.fecha().equals(cita.fecha())
                        && otra.hora().equals(cita.hora());
                if (otra.citaId() != cita.citaId() && otra.estado() != EstadoCita.CANCELADA && mismoHorario) {
                    return false;
                }
            }
        }
        ultimoId = Math.max(ultimoId, cita.citaId());
        int i = indiceDe(cita.citaId());
        if (i >= 0) {
            citas.set(i, cita);
        } else {
            citas.add(cita);
        }
        return true;
    }

    @Override
    public synchronized Optional<CitaDTO> porId(long citaId) {
        int i = indiceDe(citaId);
        return i >= 0 ? Optional.of(citas.get(i)) : Optional.empty();
    }

    @Override
    public synchronized List<CitaDTO> delDia(LocalDate fecha) {
        return citas.stream()
                .filter(c -> c.fecha().equals(fecha))
                .sorted(Comparator.comparing(CitaDTO::hora).thenComparingLong(CitaDTO::medicoId))
                .toList();
    }

    @Override
    public synchronized List<CitaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha) {
        return citas.stream()
                .filter(c -> c.medicoId() == medicoId && c.fecha().equals(fecha))
                .sorted(Comparator.comparing(CitaDTO::hora))
                .toList();
    }

    @Override
    public synchronized List<CitaDTO> delPaciente(long pacienteId) {
        return citas.stream()
                .filter(c -> c.pacienteId() == pacienteId)
                .sorted(Comparator.comparing(CitaDTO::fecha).thenComparing(CitaDTO::hora))
                .toList();
    }

    @Override
    public synchronized int contarDelMedicoEnFecha(long medicoId, LocalDate fecha) {
        return (int) citas.stream()
                .filter(c -> c.medicoId() == medicoId && c.fecha().equals(fecha) && c.estado() != EstadoCita.CANCELADA)
                .count();
    }

    @Override
    public synchronized boolean pacienteTieneCitaEnFecha(long pacienteId, LocalDate fecha) {
        return citas.stream()
                .anyMatch(c -> c.pacienteId() == pacienteId && c.fecha().equals(fecha)
                        && c.estado() != EstadoCita.CANCELADA);
    }

    /** Cuenta como "en su agenda" cualquier cita del medico con ese paciente que no este CANCELADA. */
    @Override
    public synchronized boolean medicoTienePacienteEnAgenda(long medicoId, long pacienteId) {
        return citas.stream()
                .anyMatch(c -> c.medicoId() == medicoId && c.pacienteId() == pacienteId
                        && c.estado() != EstadoCita.CANCELADA);
    }

    @Override
    public synchronized int contarProgramadasDelMedico(long medicoId) {
        return (int) citas.stream()
                .filter(c -> c.medicoId() == medicoId && c.estado() == EstadoCita.PROGRAMADA)
                .count();
    }

    @Override
    public synchronized int contarProgramadasEnConsultorio(int consultorioId) {
        return (int) citas.stream()
                .filter(c -> c.consultorio() == consultorioId && c.estado() == EstadoCita.PROGRAMADA)
                .count();
    }

    @Override
    public synchronized List<CitaDTO> programadasHasta(LocalDateTime limite) {
        return citas.stream()
                .filter(c -> c.estado() == EstadoCita.PROGRAMADA
                        && !LocalDateTime.of(c.fecha(), c.hora()).isAfter(limite))
                .sorted(Comparator.comparing(CitaDTO::fecha).thenComparing(CitaDTO::hora))
                .toList();
    }

    @Override
    public synchronized boolean cambiarEstado(long citaId, EstadoCita desde, EstadoCita hasta) {
        int i = indiceDe(citaId);
        if (i < 0 || citas.get(i).estado() != desde) {
            return false;
        }
        citas.set(i, conEstado(citas.get(i), hasta));
        return true;
    }

    @Override
    public synchronized boolean registrarFin(long citaId, LocalDateTime horaFin) {
        if (!cambiarEstado(citaId, EstadoCita.ASISTENCIA_CONFIRMADA, EstadoCita.ATENDIDA)) {
            return false;
        }
        horasFin.put(citaId, horaFin);
        return true;
    }

    private int indiceDe(long citaId) {
        for (int i = 0; i < citas.size(); i++) {
            if (citas.get(i).citaId() == citaId) {
                return i;
            }
        }
        return -1;
    }

    private static CitaDTO conEstado(CitaDTO c, EstadoCita estado) {
        return new CitaDTO(c.citaId(), c.pacienteId(), c.nombrePaciente(), c.numeroAfiliado(),
                c.medicoId(), c.nombreMedico(), c.especialidad(), c.consultorio(),
                c.fecha(), c.hora(), c.numeroFicha(), estado);
    }
}
