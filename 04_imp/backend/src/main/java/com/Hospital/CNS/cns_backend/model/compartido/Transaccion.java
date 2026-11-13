package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.function.Supplier;

/**
 * Agrupa varios pasos que deben guardarse juntos: si uno lanza una excepcion, no queda ninguno.
 * El model no conoce la base de datos: la implementacion real es {@code PgTransaccion} (infrastructure)
 * y en las pruebas se usa {@link #DIRECTA}.
 */
public interface Transaccion {

    /** Ejecuta los pasos sin transaccion (pruebas con Fakes y constructores de compatibilidad). */
    Transaccion DIRECTA = new Transaccion() {
        @Override
        public <T> T ejecutar(Supplier<T> trabajo) {
            return trabajo.get();
        }
    };

    <T> T ejecutar(Supplier<T> trabajo);

    default void ejecutar(Runnable trabajo) {
        ejecutar(() -> {
            trabajo.run();
            return null;
        });
    }
}
