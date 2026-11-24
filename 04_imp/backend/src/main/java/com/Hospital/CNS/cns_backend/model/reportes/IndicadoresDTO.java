package com.Hospital.CNS.cns_backend.model.reportes;

/**
 * CU-20: indicadores de gestion de un periodo. Un indicador sin datos que lo respalden va en {@code null} (CU-20
 * [3a]): nunca se inventa un valor.
 *
 * @param solicitudes             demanda: solicitudes registradas en el periodo
 * @param sinCupo                 solicitudes que terminaron SIN_CUPO
 * @param canceladas              citas del periodo canceladas
 * @param canceladasAutomaticas   de ellas, las que cancelo el sistema por no confirmar asistencia (inasistencias)
 * @param fichasPorMedicoDia      carga de trabajo: fichas no canceladas por medico y dia ({@code null} sin fichas)
 * @param minutosConfirmacionFin  promedio de minutos entre la confirmacion de asistencia y el fin de la atencion
 *                                ({@code null} sin atenciones). Espera y atencion por separado no son medibles:
 *                                // PENDIENTE PV-17
 * @param atencionesMedidas       atenciones con las que se calculo {@code minutosConfirmacionFin}
 */
public record IndicadoresDTO(int solicitudes, int sinCupo, int canceladas, int canceladasAutomaticas,
                             Double fichasPorMedicoDia, Double minutosConfirmacionFin, int atencionesMedidas) {
}
