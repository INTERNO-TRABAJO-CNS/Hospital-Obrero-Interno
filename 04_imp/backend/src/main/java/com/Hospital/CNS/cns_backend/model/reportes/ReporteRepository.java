package com.Hospital.CNS.cns_backend.model.reportes;

import java.time.LocalDate;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Lecturas agregadas (count / group by) para reportes y supervision (CU-19/CU-20). Solo lectura: no hay ningun
 * metodo que modifique datos. Implementacion: {@code infrastructure.pg.PgReporteRepository}.
 */
public interface ReporteRepository {

    /** Fichas atendidas de un medico en un dia (para el cumplimiento de la meta diaria). */
    record AtendidasDia(long medicoId, String nombreMedico, TipoEspecialidad especialidad, LocalDate fecha,
                        int atendidas) {
    }

    /**
     * Medicos con fichas ese dia o con horario activo ese dia de la semana (si no es dia no habil), con su horario
     * y la cantidad de fichas por estado.
     */
    List<ResumenEspecialidadDTO> resumenDelDia(LocalDate fecha);

    /** Fichas de un medico en una fecha. */
    List<FichaSupervisionDTO> fichasDeMedico(long medicoId, LocalDate fecha);

    /** Citas con fecha entre {@code desde} y {@code hasta} (inclusive), contadas por estado. */
    List<ReporteEstadoDTO> citasPorEstado(LocalDate desde, LocalDate hasta);

    /** Una fila por medico y dia con fichas entre {@code desde} y {@code hasta}. */
    List<AtendidasDia> atendidasPorMedicoYDia(LocalDate desde, LocalDate hasta);

    /** Solicitudes registradas en el periodo, por especialidad (codigo de TipoEspecialidad). */
    List<ReporteGrupoDTO> solicitudesPorEspecialidad(LocalDate desde, LocalDate hasta);

    /** Fichas atendidas en el periodo, por consultorio (nombre). */
    List<ReporteGrupoDTO> atendidasPorConsultorio(LocalDate desde, LocalDate hasta);

    /** Indicadores de gestion del periodo. */
    IndicadoresDTO indicadores(LocalDate desde, LocalDate hasta);
}
