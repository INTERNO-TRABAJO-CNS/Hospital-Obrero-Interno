package com.Hospital.CNS.cns_backend.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.citas.ConsultaCitasPacienteService;
import com.Hospital.CNS.cns_backend.model.citas.ReprogramacionService;
import com.Hospital.CNS.cns_backend.model.citas.ResultadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;

/**
 * CU-08 y CU-10 (T100-07): "Mis citas" del paciente autenticado y de sus beneficiarios (RN-05). El titular sale del
 * token; {@code pacienteId} (opcional) indica sobre cual de sus beneficiarios actua y siempre se verifica que sea de
 * su misma afiliacion.
 */
@RestController
@RequestMapping("/api/v1/citas")
public class CitasPacienteController {

    private static final String SOLO_PACIENTE = "Solo el paciente puede ver y reprogramar sus citas";
    private static final Comparator<CitaDTO> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(CitaDTO::fecha).thenComparing(CitaDTO::hora).reversed();

    private final ConsultaCitasPacienteService consulta;
    private final ReprogramacionService reprogramacion;
    private final PacienteService pacientes;
    private final ResolutorUsuario resolutor;

    public CitasPacienteController(ConsultaCitasPacienteService consulta, ReprogramacionService reprogramacion,
                                   PacienteService pacientes, ResolutorUsuario resolutor) {
        this.consulta = consulta;
        this.reprogramacion = reprogramacion;
        this.pacientes = pacientes;
        this.resolutor = resolutor;
    }

    @GetMapping("/mias")
    public List<CitaDTO> misCitas(@RequestHeader HttpHeaders cabeceras) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTE, RolUsuario.PACIENTE);
        return pacientes.grupoFamiliar(paciente.personaId()).stream()
                .map(PacienteDTO::pacienteId)
                .flatMap(id -> consulta.misCitas(id).stream())
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    @GetMapping("/{id}")
    public CitaDTO detalle(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id,
                           @RequestParam(name = "pacienteId", required = false) Long pacienteId) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTE, RolUsuario.PACIENTE);
        return consulta.detalle(id, pacientes.pacienteDelGrupo(paciente.personaId(), pacienteId));
    }

    /** Cancela la cita y deja una solicitud con prioridad 2 para el siguiente dia habil. */
    @PostMapping("/{id}/reprogramar")
    public ResultadoSolicitud reprogramar(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id,
                                          @RequestParam(name = "pacienteId", required = false) Long pacienteId) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTE, RolUsuario.PACIENTE);
        long deQuien = pacientes.pacienteDelGrupo(paciente.personaId(), pacienteId);
        return ResultadoSolicitud.desde(reprogramacion.reprogramar(id, deQuien));
    }
}
