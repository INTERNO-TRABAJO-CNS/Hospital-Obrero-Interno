package com.Hospital.CNS.cns_backend.model.reportes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.Reloj;

/**
 * CU-19: agenda de supervision del administrador (solo lectura). Resumen por especialidad y medico, y detalle de las
 * fichas de un medico. La vista se actualiza en tiempo real por /ws/supervision (AgendaEventos).
 *
 * <p>Flujos alternativos: [1a] sin rol ADMINISTRADOR lo corta el controller (ACCESO_DENEGADO); [3a] sin datos para
 * el criterio devuelve una lista vacia y la pantalla informa que no hay informacion; [3b] cualquier intento de
 * modificar lo rechaza el controller con SOLO_LECTURA.</p>
 */
public class SupervisionService {

    private final ReporteRepository repositorio;
    private final Reloj reloj;

    public SupervisionService(ReporteRepository repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    /**
     * Resumen de las especialidades basicas (las de las citas comunes) agrupado por especialidad y, dentro de cada
     * una, por medico. Con {@code fecha} null usa el dia de hoy.
     */
    public List<ResumenEspecialidadDTO> resumenDelDia(LocalDate fecha) {
        return repositorio.resumenDelDia(diaOHoy(fecha)).stream()
                .filter(r -> r.especialidad().esBasica())
                .sorted(Comparator.comparing(ResumenEspecialidadDTO::especialidad)
                        .thenComparing(ResumenEspecialidadDTO::nombreMedico)
                        .thenComparingLong(ResumenEspecialidadDTO::medicoId))
                .toList();
    }

    /** Fichas de un medico ordenadas por horario. Con {@code fecha} null usa el dia de hoy. */
    public List<FichaSupervisionDTO> fichasDeMedico(long medicoId, LocalDate fecha) {
        return repositorio.fichasDeMedico(medicoId, diaOHoy(fecha)).stream()
                .sorted(Comparator.comparing(FichaSupervisionDTO::hora)
                        .thenComparingInt(FichaSupervisionDTO::numeroFicha))
                .toList();
    }

    private LocalDate diaOHoy(LocalDate fecha) {
        return fecha != null ? fecha : reloj.ahora().toLocalDate();
    }
}
