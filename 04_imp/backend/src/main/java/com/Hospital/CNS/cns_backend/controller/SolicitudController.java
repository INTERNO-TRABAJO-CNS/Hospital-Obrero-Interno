package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
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

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    public record SolicitudRequest(
            TipoEspecialidad especialidad,
            LocalDate diaSolicitado) {
    }

    private final SolicitudService solicitudes;
    private final ResolutorUsuario resolutor;

    public SolicitudController(
            SolicitudService solicitudes,
            ResolutorUsuario resolutor) {

        this.solicitudes = solicitudes;
        this.resolutor = resolutor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoSolicitud solicitar(
            @RequestHeader HttpHeaders cabeceras,
            @RequestBody SolicitudRequest datos) {

        UsuarioAutenticado usuario = Permisos.exigir(
                resolutor,
                cabeceras,
                "Solo los pacientes pueden solicitar citas",
                RolUsuario.PACIENTE
        );

        SolicitudDTO solicitud = solicitudes.solicitar(
                usuario.personaId(),
                datos.especialidad(),
                datos.diaSolicitado()
        );

        return ResultadoSolicitud.desde(solicitud);
    }

    @GetMapping
    public List<ResultadoSolicitud> listar(
            @RequestHeader HttpHeaders cabeceras) {

        UsuarioAutenticado usuario = Permisos.exigir(
                resolutor,
                cabeceras,
                "Solo los pacientes pueden consultar sus solicitudes",
                RolUsuario.PACIENTE
        );

        return solicitudes.listarPorPaciente(usuario.personaId())
                .stream()
                .map(ResultadoSolicitud::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public ResultadoSolicitud consultar(
            @RequestHeader HttpHeaders cabeceras,
            @PathVariable("id") long id) {

        UsuarioAutenticado usuario = Permisos.exigir(
                resolutor,
                cabeceras,
                "Solo los pacientes pueden consultar sus solicitudes",
                RolUsuario.PACIENTE
        );

        SolicitudDTO solicitud = solicitudes.consultar(
                usuario.personaId(),
                id
        );

        return ResultadoSolicitud.desde(solicitud);
    }
}