package com.Hospital.CNS.cns_backend.model.recursos;

/**
 * Valores por defecto para campos que el cliente omite en el JSON. Jackson 3 rechaza un campo
 * primitivo ausente; los DTO lo leen como envoltorio y lo convierten aqui.
 */
final class JsonOpcional {

    private JsonOpcional() {
    }

    static long largo(Long valor) {
        return valor == null ? 0L : valor;
    }

    static int entero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    static boolean logico(Boolean valor) {
        return valor != null && valor;
    }
}
