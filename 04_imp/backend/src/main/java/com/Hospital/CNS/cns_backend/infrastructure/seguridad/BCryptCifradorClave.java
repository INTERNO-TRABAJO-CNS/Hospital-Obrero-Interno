package com.Hospital.CNS.cns_backend.infrastructure.seguridad;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.Hospital.CNS.cns_backend.model.identidad.CifradorClave;

/** CifradorClave con BCrypt (Spring Security Crypto). El model solo conoce la interfaz. */
public class BCryptCifradorClave implements CifradorClave {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String clave) {
        return bcrypt.encode(clave);
    }

    @Override
    public boolean coincide(String clave, String claveCifrada) {
        return clave != null && claveCifrada != null && bcrypt.matches(clave, claveCifrada);
    }
}
