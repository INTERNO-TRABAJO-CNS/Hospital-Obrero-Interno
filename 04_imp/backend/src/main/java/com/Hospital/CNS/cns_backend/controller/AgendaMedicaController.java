package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.operaciones.AgendaDiaDTO;
import com.Hospital.CNS.cns_backend.model.operaciones.AgendaMedicaService;

/**
 * CU-13: agenda del medico autenticado. El medicoId sale del usuario (X-Persona-Id), nunca del request.
 * [1a] rol distinto de MEDICO -> ACCESO_DENEGADO (403).
 */
@RestController
@RequestMapping("/api/v1/medico")
public class AgendaMedicaController {

    private final AgendaMedicaService agenda;
    private final ResolutorUsuario resolutor;

    public AgendaMedicaController(AgendaMedicaService agenda, ResolutorUsuario resolutor) {
        this.agenda = agenda;
        this.resolutor = resolutor;
    }

    @GetMapping("/agenda")
    public AgendaDiaDTO agenda(@RequestHeader HttpHeaders cabeceras,
                               @RequestParam(name = "fecha", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return agenda.agendaDelMedico(medico.personaId(), fecha);
    }
}
