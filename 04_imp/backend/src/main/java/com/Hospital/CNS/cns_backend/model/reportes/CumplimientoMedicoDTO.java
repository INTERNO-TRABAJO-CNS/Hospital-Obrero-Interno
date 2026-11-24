package com.Hospital.CNS.cns_backend.model.reportes;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-19/CU-20: fichas atendidas por un medico en un periodo frente a la meta diaria (minimo 5, maximo 7).
 *
 * @param dias            dias del periodo (hasta hoy) en que el medico tuvo fichas
 * @param promedioDiario  fichas atendidas / dias
 * @param diasBajoMeta    dias con menos de {@link ReporteService#META_MINIMA} fichas atendidas
 * @param bajoMeta        el promedio diario no llega a la meta minima
 */
public record CumplimientoMedicoDTO(long medicoId, String nombreMedico, TipoEspecialidad especialidad, int dias,
                                    int fichasAtendidas, double promedioDiario, int diasBajoMeta, boolean bajoMeta) {
}
