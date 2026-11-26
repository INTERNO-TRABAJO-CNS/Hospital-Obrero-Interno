package com.Hospital.CNS.cns_backend.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.FakeAfiliadoRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeCifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.FakePacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeSesionRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeUsuarioRepository;
import com.Hospital.CNS.cns_backend.model.identidad.LimitadorIntentos;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;
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

    /**
     * PacienteService con una afiliacion: {@code titular} y sus {@code beneficiarios} (todos con la afiliacion
     * {@code afiliado}). Sirve para las reglas del grupo familiar (RN-05).
     */
    public static PacienteService pacientes(String afiliado, long titular, long... beneficiarios) {
        FakePacienteRepository repo = new FakePacienteRepository();
        repo.store(new PacienteDTO(titular, afiliado, "CI-" + titular, "Titular", "Prueba",
                LocalDate.of(1980, 1, 1), true));
        for (long b : beneficiarios) {
            repo.store(new PacienteDTO(b, afiliado, "CI-" + b, "Beneficiario", "Prueba",
                    LocalDate.of(2020, 1, 1), false));
        }
        return new PacienteService(repo, new FakeAfiliadoRepository(), new FakeHistorialRepository(),
                new FakeUsuarioRepository(), new FakeCifradorClave(), new RelojSistema());
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
