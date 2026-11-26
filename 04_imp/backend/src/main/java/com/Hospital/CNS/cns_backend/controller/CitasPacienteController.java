package com.Hospital.CNS.cns_backend.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.citas.ConsultaCitasPacienteService;
import com.Hospital.CNS.cns_backend.model.citas.ReprogramacionService;
import com.Hospital.CNS.cns_backend.model.citas.ResultadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * CU-08 y CU-10 (T100-07): "Mis citas" del paciente autenticado. El pacienteId sale del token, nunca del request.
 */
@RestController
@RequestMapping("/api/v1/citas")
public class CitasPacienteController {

    private static final String SOLO_PACIENTE = "Solo el paciente puede ver y reprogramar sus citas";

    private final ConsultaCitasPacienteService consulta;
    private final ReprogramacionService reprogramacion;
    private final ResolutorUsuario resolutor;

    public CitasPacienteController(ConsultaCitasPacienteService consulta, ReprogramacionService reprogramacion,
                                   ResolutorUsuario resolutor) {
        this.consulta = consulta;
        this.reprogramacion = reprogramacion;
        this.resolutor = resolutor;
    }

    @GetMapping("/mias")
    public List<CitaDTO> misCitas(@RequestHeader HttpHeaders cabeceras) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTE, RolUsuario.PACIENTE);
        return consulta.misCitas(paciente.personaId());
    }

    @GetMapping("/{id}")
    public CitaDTO detalle(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTE, RolUsuario.PACIENTE);
        return consulta.detalle(id, paciente.personaId());
    }

    /** Cancela la cita y deja una solicitud con prioridad 2 para el siguiente dia habil. */
    @PostMapping("/{id}/reprogramar")
    public ResultadoSolicitud reprogramar(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTE, RolUsuario.PACIENTE);
        return ResultadoSolicitud.desde(reprogramacion.reprogramar(id, paciente.personaId()));
    }
}
