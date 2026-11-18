package com.Hospital.CNS.cns_backend.model.identidad;

/**
 * Datos para que el titular cree su cuenta. Nombre y fecha de nacimiento se toman de la tabla interna de
 * afiliados: el titular solo prueba quien es (numero de afiliado + su CI) y elige usuario y contrasena.
 */
public record RegistroTitularDTO(String numeroAfiliado, String ci, String usuario, String clave) {
}
