
package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FichaTvDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;

import java.time.LocalDate;
import java.util.List;

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

        final String valorOriginal = valor == null ? "" : valor;
        final String valorBusqueda = valorOriginal.toLowerCase();

        return citasDelDia.stream().filter(cita -> {
            switch (criterio) {
                case PACIENTE:
                    return cita.nombrePaciente() != null
                            && cita.nombrePaciente().toLowerCase().contains(valorBusqueda);
                case MEDICO:
                    return cita.nombreMedico() != null
                            && cita.nombreMedico().toLowerCase().contains(valorBusqueda);
                case ESTADO:
                    return cita.estado() != null
                            && cita.estado().name().equalsIgnoreCase(valorOriginal);
                case CONSULTORIO:
                    return String.valueOf(cita.consultorio()).contains(valorOriginal);
                default:
                    return false;
            }
        }).toList();
    }

    // Obtiene todas las fichas del día para enfermería.
    public List<CitaDTO> listadoDelDia() {
        LocalDate hoy = reloj.ahora().toLocalDate();
        return citasRepository.delDia(hoy);
    }

    // Obtiene únicamente los datos necesarios para la pantalla de TV.
    public List<FichaTvDTO> listadoParaTv() {
        LocalDate hoy = reloj.ahora().toLocalDate();

        return citasRepository.delDia(hoy).stream()
                .map(cita -> new FichaTvDTO(
                        cita.numeroFicha(),
                        cita.hora(),
                        cita.especialidad(),
                        cita.consultorio(),
                        cita.estado()
                ))
                .toList();
    }
}
