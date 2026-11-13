package com.Hospital.CNS.cns_backend.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.FakeCifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.FakeSesionRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeUsuarioRepository;
import com.Hospital.CNS.cns_backend.model.identidad.LimitadorIntentos;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioDTO;

/**
 * Sesiones reales (AuthService con Fakes, sin BD) para las pruebas de controllers. Reemplaza a las cabeceras
 * temporales X-Usuario-Id / X-Rol / X-Persona-Id: {@code .header("Authorization", sesiones.bearer(1, "MEDICO", 7))}.
 * Cada combinacion (usuarioId, rol, personaId) es una cuenta distinta.
 */
public final class SesionesDePrueba {

    private static final String CLAVE = "clave-de-prueba";

    private final FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
    private final FakeCifradorClave cifrador = new FakeCifradorClave();
    private final AuthService auth = new AuthService(usuarios, cifrador, new FakeSesionRepository(), null,
            LimitadorIntentos.sinLimite(), new RelojSistema(), Duration.ofHours(8));
    private final Map<String, String> tokens = new HashMap<>();

    public AuthService auth() {
        return auth;
    }

    public FakeUsuarioRepository usuarios() {
        return usuarios;
    }

    public ResolutorUsuario resolutor() {
        return new ResolutorUsuario(auth);
    }

    /** Valor del header Authorization de una cuenta con ese rol y personaId (la crea la primera vez). */
    public String bearer(long usuarioId, String rol, long personaId) {
        String clave = usuarioId + "-" + rol + "-" + personaId;
        String token = tokens.computeIfAbsent(clave, k -> {
            String nombre = "prueba-" + k;
            usuarios.store(new UsuarioDTO(usuarios.nextIdentity(), nombre, cifrador.cifrar(CLAVE),
                    RolUsuario.valueOf(rol), personaId, "Usuario " + k, true));
            return auth.login(nombre, CLAVE).token();
        });
        return "Bearer " + token;
    }
}
