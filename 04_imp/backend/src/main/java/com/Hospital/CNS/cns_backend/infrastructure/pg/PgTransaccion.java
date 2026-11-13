package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.util.function.Supplier;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/** Transaccion de PostgreSQL: todos los Pg*Repository llamados dentro usan la misma conexion. */
public class PgTransaccion implements Transaccion {

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        return DB.enTransaccion(trabajo);
    }
}
