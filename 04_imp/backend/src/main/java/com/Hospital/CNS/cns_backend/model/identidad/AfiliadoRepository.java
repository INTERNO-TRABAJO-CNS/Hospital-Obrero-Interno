package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.Optional;

/** Tabla interna de afiliados (sin integracion externa). Implementacion: PgAfiliadoRepository. */
public interface AfiliadoRepository {
    /** true si existe un afiliado ACTIVO con ese numero. */
    boolean existePorNumero(String numeroAfiliado);

    /** El afiliado (titular) activo con ese numero. */
    Optional<AfiliadoDTO> titularPorNumero(String numeroAfiliado);
}
