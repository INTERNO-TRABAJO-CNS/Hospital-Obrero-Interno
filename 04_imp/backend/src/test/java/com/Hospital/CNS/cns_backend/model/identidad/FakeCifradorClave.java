package com.Hospital.CNS.cns_backend.model.identidad;

/** Cifrador de pruebas: "cifra" agregando un prefijo, para verificar que nunca se guarda la clave tal cual. */
public class FakeCifradorClave implements CifradorClave {

    public static final String PREFIJO = "cifrada:";

    @Override
    public String cifrar(String clave) {
        return PREFIJO + clave;
    }

    @Override
    public boolean coincide(String clave, String claveCifrada) {
        return (PREFIJO + clave).equals(claveCifrada);
    }
}
