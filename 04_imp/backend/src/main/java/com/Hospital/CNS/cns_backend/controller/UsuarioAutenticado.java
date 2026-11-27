package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Usuario que hace la peticion: quien es, con que rol y a que persona corresponde. {@code nombre} es el nombre de
 * la cuenta (puede ser null); se usa para el historial de cambios (CU-18), p. ej. "Enfermería · M. Flores".
 */
public record UsuarioAutenticado(long usuarioId, RolUsuario rol, long personaId, String nombre) {

    /** Sin nombre (compatibilidad con el codigo y las pruebas anteriores). */
    public UsuarioAutenticado(long usuarioId, RolUsuario rol, long personaId) {
        this(usuarioId, rol, personaId, null);
    }
}
