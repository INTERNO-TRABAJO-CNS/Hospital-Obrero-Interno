package com.Hospital.CNS.cns_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.SesionDTO;

/**
 * CU-01: POST /api/v1/auth/login -> {usuarioId, rol, personaId, nombre, token, venceEn, numeroAfiliado}.
 * [2a] -> CREDENCIALES_INVALIDAS (401); demasiados intentos -> INTENTOS_EXCEDIDOS (429).
 * El cliente envia el token en cada peticion: {@code Authorization: Bearer <token>}.
 * POST /api/v1/auth/logout cierra la sesion de ese token.
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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader HttpHeaders cabeceras) {
        auth.cerrarSesion(ResolutorUsuario.token(cabeceras.getFirst(ResolutorUsuario.HEADER_AUTORIZACION)));
    }
}
