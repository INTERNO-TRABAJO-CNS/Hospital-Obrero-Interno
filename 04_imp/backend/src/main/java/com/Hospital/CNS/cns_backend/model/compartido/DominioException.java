package com.Hospital.CNS.cns_backend.model.compartido;

/**
 * Error de regla de negocio. El campo {@code codigo} lo traduce el
 * {@code ManejadorErrores} (controller) a un estado HTTP y a un JSON
 * {@code {codigo, mensaje}}.
 */
public class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigo;

    public DominioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public DominioException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
