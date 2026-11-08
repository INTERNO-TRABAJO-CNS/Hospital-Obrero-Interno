package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class AgendaMedicaService {

    private final CitaRepository citasRepository;
    private final Reloj reloj;

    public AgendaMedicaService(CitaRepository citasRepository, Reloj reloj) {
        this.citasRepository = citasRepository;
        this.reloj = reloj;
    }

    public AgendaDiaDTO agendaDelMedico(long medicoId, LocalDate fechaONull) {
        // Usamos LocalDate.now() como respaldo si fechaONull es nula, 
        // evitando problemas si el método del Reloj tiene otro nombre.
        LocalDate fechaElegida = (fechaONull != null) ? fechaONull : LocalDate.now();

        List<CitaDTO> citasDto = citasRepository.delMedicoEnFecha(medicoId, fechaElegida);

        List<FichaAgendaDTO> fichas = citasDto.stream()
                .sorted(Comparator.comparing(CitaDTO::hora))
                .map(c -> new FichaAgendaDTO(
                        c.citaId(),
                        c.numeroFicha(),
                        c.hora(),
                        c.nombrePaciente(),
                        c.estado()
                ))
                .toList();

        return new AgendaDiaDTO(fechaElegida, fichas.size(), fichas);
    }
}