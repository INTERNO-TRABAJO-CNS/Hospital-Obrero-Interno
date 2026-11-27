package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Quirofanos (tabla quirofano.quirofano, migracion V8). Implementacion: PgQuirofanoRepository. */
public interface QuirofanoRepository {

    int nextIdentity();

    /** Inserta o actualiza (por quirofanoId). false si ya hay otro ACTIVO con ese nombre. */
    boolean store(QuirofanoDTO quirofano);

    Optional<QuirofanoDTO> porId(int quirofanoId);

    /** Todos (activos y dados de baja), por nombre. */
    List<QuirofanoDTO> listar();

    /** Activos de una especialidad, por nombre. */
    List<QuirofanoDTO> activosDe(TipoEspecialidad especialidad);
}
