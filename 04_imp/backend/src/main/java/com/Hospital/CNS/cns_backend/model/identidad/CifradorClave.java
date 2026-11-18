package com.Hospital.CNS.cns_backend.model.identidad;

/** Cifra y compara contrasenas. Implementacion: BCryptCifradorClave (infrastructure). */
public interface CifradorClave {
    String cifrar(String clave);

    boolean coincide(String clave, String claveCifrada);
}
