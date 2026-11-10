package com.Hospital.CNS.cns_backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.SesionDTO;

/**
 * CU-01: POST /api/v1/auth/login -> {usuarioId, rol, personaId, nombre}. [2a] -> CREDENCIALES_INVALIDAS (401).
 * Hasta S3-08 el cliente reenvia esos datos en los headers X-Usuario-Id, X-Rol, X-Persona-Id.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /** Cuerpo del login. */
    public record LoginRequest(String usuario, String clave) {
    }

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public SesionDTO login(@RequestBody LoginRequest cuerpo) {
        return auth.login(cuerpo == null ? null : cuerpo.usuario(), cuerpo == null ? null : cuerpo.clave());
    }
}
