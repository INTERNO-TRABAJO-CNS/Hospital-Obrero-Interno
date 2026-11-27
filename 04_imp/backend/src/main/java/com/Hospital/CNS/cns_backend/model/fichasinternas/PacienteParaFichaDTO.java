package com.Hospital.CNS.cns_backend.model.fichasinternas;

/** Resultado de buscar por N° de afiliado en una urgencia (titular y beneficiarios de la afiliacion). */
public record PacienteParaFichaDTO(long pacienteId, String nombre, String numeroAfiliado, int edad, boolean titular) {
}
