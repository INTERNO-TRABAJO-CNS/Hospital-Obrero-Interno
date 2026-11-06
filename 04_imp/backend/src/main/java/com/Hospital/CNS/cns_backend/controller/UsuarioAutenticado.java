package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** Usuario que hace la peticion: quien es, con que rol y a que persona corresponde. */
public record UsuarioAutenticado(long usuarioId, RolUsuario rol, long personaId) {
}
