package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.ArrayList;
import java.util.List;

/**
 * FinalizacionCita falsa para probar controllers: devuelve siempre {@code resultado}
 * (cambialo en el test) y guarda cada llamada en {@code llamadas}.
 */
public class FakeFinalizacionCita implements FinalizacionCita {

    public record Llamada(long citaId, long medicoId) {
    }

    public ResultadoFinalizacion resultado = ResultadoFinalizacion.FINALIZADA;
    public final List<Llamada> llamadas = new ArrayList<>();

    @Override
    public synchronized ResultadoFinalizacion finalizar(long citaId, long medicoId) {
        llamadas.add(new Llamada(citaId, medicoId));
        return resultado;
    }
}
