package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FichasDelDiaService {

    private final CitaRepository citasRepository;
    private final Reloj reloj;

    public FichasDelDiaService(CitaRepository citas, Reloj reloj) {
        this.citasRepository = citas;
        this.reloj = reloj;
    }

    public List<CitaDTO> buscar(CriterioFicha criterio, String valor) {
        LocalDate hoy = reloj.ahora().toLocalDate();
        
        if (criterio == null) {
            throw new IllegalArgumentException("Criterio inválido");
        }

        List<CitaDTO> citasDelDia = citasRepository.delDia(hoy);

        return citasDelDia.stream().filter(cita -> {
            switch (criterio) {
                case PACIENTE:
                    return cita.nombrePaciente() != null && cita.nombrePaciente().toLowerCase().contains(valor.toLowerCase());
                case MEDICO:
                    return cita.nombreMedico() != null && cita.nombreMedico().toLowerCase().contains(valor.toLowerCase());
                case ESTADO:
                    return cita.estado() != null && cita.estado().name().equalsIgnoreCase(valor);
                case CONSULTORIO:
                    return String.valueOf(cita.consultorio()).toLowerCase().contains(valor.toLowerCase());
                default:
                    return false;
            }
        }).collect(Collectors.toList());
    }

    public List<CitaDTO> listadoParaTv() {
        LocalDate hoy = reloj.ahora().toLocalDate();
        return citasRepository.delDia(hoy);
    }
}