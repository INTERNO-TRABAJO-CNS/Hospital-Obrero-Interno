package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.citas.ResultadoSolicitud;
import com.Hospital.CNS.cns_backend.model.citas.SolicitudService;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;

/**
 * CU-04: el paciente elige especialidad y dia (RF-04); medico, hora y consultorio los asigna el sistema. El titular
 * tambien pide para sus beneficiarios (RN-05) indicando {@code pacienteId}; sin el, la solicitud es para el mismo.
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    /** {@code pacienteId} es opcional: el titular o uno de sus beneficiarios. */
    public record SolicitudRequest(TipoEspecialidad especialidad, LocalDate dia, Long pacienteId) {
    }

    private static final String SOLO_PACIENTES = "Solo los pacientes pueden solicitar y consultar citas";

    private final SolicitudService solicitudes;
    private final PacienteService pacientes;
    private final ResolutorUsuario resolutor;

    public SolicitudController(SolicitudService solicitudes, PacienteService pacientes, ResolutorUsuario resolutor) {
        this.solicitudes = solicitudes;
        this.pacientes = pacientes;
        this.resolutor = resolutor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoSolicitud solicitar(
            @RequestHeader HttpHeaders cabeceras,
            @RequestBody SolicitudRequest datos) {

        UsuarioAutenticado usuario = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTES, RolUsuario.PACIENTE);
        long pacienteId = pacientes.pacienteDelGrupo(usuario.personaId(), datos.pacienteId());

        SolicitudDTO solicitud = solicitudes.solicitar(pacienteId, datos.especialidad(), datos.dia());

        return ResultadoSolicitud.desde(solicitud);
    }

    /** Dias que se pueden elegir: habiles desde hoy hasta 15 dias habiles despues (sin feriados). */
    @GetMapping("/dias-disponibles")
    public List<LocalDate> diasDisponibles(@RequestHeader HttpHeaders cabeceras) {
        Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTES, RolUsuario.PACIENTE);
        return solicitudes.diasHabilesDisponibles();
    }

    /** Las solicitudes del titular y de sus beneficiarios, la mas reciente primero. */
    @GetMapping
    public List<ResultadoSolicitud> listar(@RequestHeader HttpHeaders cabeceras) {
        UsuarioAutenticado usuario = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTES, RolUsuario.PACIENTE);

        return pacientes.grupoFamiliar(usuario.personaId()).stream()
                .map(PacienteDTO::pacienteId)
                .flatMap(id -> solicitudes.listarPorPaciente(id).stream())
                .sorted(Comparator.comparingLong(SolicitudDTO::ordenLlegada).reversed())
                .map(ResultadoSolicitud::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public ResultadoSolicitud consultar(
            @RequestHeader HttpHeaders cabeceras,
            @PathVariable("id") long id) {

        UsuarioAutenticado usuario = Permisos.exigir(resolutor, cabeceras, SOLO_PACIENTES, RolUsuario.PACIENTE);
        SolicitudDTO propia = pacientes.grupoFamiliar(usuario.personaId()).stream()
                .flatMap(p -> solicitudes.listarPorPaciente(p.pacienteId()).stream())
                .filter(s -> s.solicitudId() == id)
                .findFirst()
                // NO_ENCONTRADA o NO_ES_DEL_PACIENTE, segun corresponda.
                .orElseGet(() -> solicitudes.consultar(usuario.personaId(), id));

        return ResultadoSolicitud.desde(propia);
    }
}
