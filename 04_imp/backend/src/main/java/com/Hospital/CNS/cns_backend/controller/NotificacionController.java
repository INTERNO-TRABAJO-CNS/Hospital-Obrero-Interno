package com.Hospital.CNS.cns_backend.controller;

import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    private final NotificacionRepository repository;
    private final ResolutorUsuario resolutor;

    public NotificacionController(NotificacionRepository repository, ResolutorUsuario resolutor) {
        this.repository = repository;
        this.resolutor = resolutor;
    }

    @GetMapping
    public List<NotificacionDTO> delPaciente(
            @RequestHeader HttpHeaders cabeceras,
            @RequestParam long pacienteId) {
        UsuarioAutenticado usuario = Permisos.exigir(
                resolutor,
                cabeceras,
                "Solo el paciente puede consultar sus notificaciones",
                RolUsuario.PACIENTE);
        if (usuario.personaId() != pacienteId) {
            throw new DominioException("NO_ES_DEL_PACIENTE", "No puede consultar las notificaciones de otro paciente");
        }
        return repository.delPaciente(pacienteId);
    }
}