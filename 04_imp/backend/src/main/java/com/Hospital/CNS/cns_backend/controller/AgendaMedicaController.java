package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.operaciones.AgendaDiaDTO;
import com.Hospital.CNS.cns_backend.model.operaciones.AgendaMedicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/medico")
public class AgendaMedicaController {

    private final AgendaMedicaService agendaMedicaService;

    public AgendaMedicaController(AgendaMedicaService agendaMedicaService) {
        this.agendaMedicaService = agendaMedicaService;
    }

    @GetMapping("/agenda")
    public ResponseEntity<AgendaDiaDTO> obtenerAgenda(
            @RequestHeader("X-User-Role") String rol,
            @RequestHeader("X-User-Id") long medicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        if (!"MEDICO".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(403).build();
        }

        AgendaDiaDTO agenda = agendaMedicaService.agendaDelMedico(medicoId, fecha);
        return ResponseEntity.ok(agenda);
    }
}