package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDateTime;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Resultado del login (CU-01). El cliente guarda {@code token} y lo envia en cada peticion como
 * {@code Authorization: Bearer <token>} hasta {@code venceEn}. {@code numeroAfiliado} solo viene para el rol
 * PACIENTE (lo usa el registro de beneficiarios).
 */
public record SesionDTO(long usuarioId, RolUsuario rol, long personaId, String nombre, String token,
                        LocalDateTime venceEn, String numeroAfiliado) {
}
