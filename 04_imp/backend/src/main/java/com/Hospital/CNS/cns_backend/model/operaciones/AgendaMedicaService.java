package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;

/**
 * CU-13: el medico consulta su agenda. Solo lectura.
 *
 * <p>El {@code medicoId} lo pone el controller a partir del usuario autenticado (nunca del request).
 * Sin fecha se usa el dia de hoy segun el {@link Reloj}. Se ordena por hora y las CANCELADAS se
 * devuelven con su estado para que la vista las marque. [1a] (rol distinto de MEDICO) lo resuelve
 * el controller; [3a] (sin citas) devuelve cantidad 0 y lista vacia.</p>
 */
public class AgendaMedicaService {

    private final CitaRepository citas;
    private final Reloj reloj;

    public AgendaMedicaService(CitaRepository citas, Reloj reloj) {
        this.citas = citas;
        this.reloj = reloj;
    }

    public AgendaDiaDTO agendaDelMedico(long medicoId, LocalDate fechaONull) {
        LocalDate fecha = fechaONull != null ? fechaONull : reloj.ahora().toLocalDate();
        List<FichaAgendaDTO> fichas = citas.delMedicoEnFecha(medicoId, fecha).stream()
                .filter(c -> c.medicoId() == medicoId)
                .sorted(Comparator.comparing(CitaDTO::hora).thenComparingInt(CitaDTO::numeroFicha))
                .map(c -> new FichaAgendaDTO(c.citaId(), c.numeroFicha(), c.hora(), c.nombrePaciente(), c.estado(),
                        c.pacienteId()))
                .toList();
        return new AgendaDiaDTO(fecha, fichas.size(), fichas);
    }
}
