package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.ArrayList;
import java.util.List;

/**
 * CancelacionCita falsa para probar controllers: devuelve siempre {@code resultado}
 * (cambialo en el test) y guarda cada llamada en {@code llamadas}.
 */
public class FakeCancelacionCita implements CancelacionCita {

    public record Llamada(long citaId, long pacienteId) {
    }

    public ResultadoCancelacion resultado = ResultadoCancelacion.CANCELADA;
    public final List<Llamada> llamadas = new ArrayList<>();

    @Override
    public synchronized ResultadoCancelacion cancelarPorPaciente(long citaId, long pacienteId) {
        llamadas.add(new Llamada(citaId, pacienteId));
        return resultado;
    }
}
