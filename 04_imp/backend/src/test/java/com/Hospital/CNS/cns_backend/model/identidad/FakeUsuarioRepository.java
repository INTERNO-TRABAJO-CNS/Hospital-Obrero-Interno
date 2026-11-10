package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** UsuarioRepository en memoria. Simula el UNIQUE de usuario sin distinguir mayusculas. */
public class FakeUsuarioRepository implements UsuarioRepository {

    public final List<UsuarioDTO> usuarios = new ArrayList<>();
    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized boolean store(UsuarioDTO u) {
        boolean ocupado = usuarios.stream()
                .anyMatch(x -> x.usuarioId() != u.usuarioId() && x.usuario().equalsIgnoreCase(u.usuario()));
        if (ocupado) {
            return false;
        }
        usuarios.removeIf(x -> x.usuarioId() == u.usuarioId());
        usuarios.add(u);
        ultimoId = Math.max(ultimoId, u.usuarioId());
        return true;
    }

    @Override
    public synchronized Optional<UsuarioDTO> porUsuario(String usuario) {
        return usuarios.stream().filter(x -> x.usuario().equalsIgnoreCase(usuario)).findFirst();
    }
}
