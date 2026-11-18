package com.Hospital.CNS.cns_backend.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.historial.HistorialPacienteDTO;
import com.Hospital.CNS.cns_backend.model.historial.HistorialService;
import com.Hospital.CNS.cns_backend.model.historial.PacienteDeAgendaDTO;

/**
 * CU-14: historial del paciente para el medico autenticado. Solo lectura.
 * GET /{pacienteId}/historial y GET ?afiliado= (buscador: solo pacientes de su agenda).
 */
@RestController
@RequestMapping("/api/v1/medico/pacientes")
public class HistorialController {

    private final HistorialService historial;
    private final ResolutorUsuario resolutor;

    public HistorialController(HistorialService historial, ResolutorUsuario resolutor) {
        this.historial = historial;
        this.resolutor = resolutor;
    }

    @GetMapping("/{pacienteId}/historial")
    public HistorialPacienteDTO verHistorial(@RequestHeader HttpHeaders cabeceras,
                                             @PathVariable("pacienteId") long pacienteId) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return historial.verHistorial(medico.personaId(), pacienteId);
    }

    @GetMapping
    public List<PacienteDeAgendaDTO> buscar(@RequestHeader HttpHeaders cabeceras,
                                            @RequestParam(name = "afiliado", required = false) String afiliado) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return historial.buscarEnAgenda(medico.personaId(), afiliado);
    }
}
