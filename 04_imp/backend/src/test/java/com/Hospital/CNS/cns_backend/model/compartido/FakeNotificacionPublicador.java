package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.ArrayList;
import java.util.List;

/** NotificacionPublicador en memoria. {@code publicadas} es publica para que los tests verifiquen. */
public class FakeNotificacionPublicador implements NotificacionPublicador {

    public final List<NotificacionDTO> publicadas = new ArrayList<>();

    @Override
    public synchronized void publicar(NotificacionDTO n) {
        publicadas.add(n);
    }
}
