package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Quirofano como lo ve el administrador: con sus cirugias PROGRAMADAS a futuro. */
public record QuirofanoVistaDTO(int quirofanoId, String nombre, TipoEspecialidad especialidad, LocalTime apertura,
                                LocalTime cierre, int limpiezaMin, boolean activo, int cirugiasProgramadas) {
}
