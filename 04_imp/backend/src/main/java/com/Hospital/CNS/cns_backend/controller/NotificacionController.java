package com.Hospital.CNS.cns_backend.controller;

import java.util.List;

import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    private final NotificacionRepository repository;

    public NotificacionController(NotificacionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NotificacionDTO> delPaciente(@RequestParam long pacienteId) {
        return repository.delPaciente(pacienteId);
    }
}