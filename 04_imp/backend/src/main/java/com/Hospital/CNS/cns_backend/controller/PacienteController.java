package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;
import com.Hospital.CNS.cns_backend.model.identidad.RegistroTitularDTO;

/**
 * CU-03: registrar y consultar pacientes.
 * <ul>
 *   <li>POST /titular: el titular crea su cuenta (sin sesion).</li>
 *   <li>POST /beneficiario: el titular (solo para su afiliacion) o enfermeria.</li>
 *   <li>GET ?afiliado=&amp;ci=: enfermeria o administrador; un paciente solo su propia afiliacion.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    /** Cuerpo de POST /beneficiario. */
    public record RegistroBeneficiarioRequest(String numeroAfiliadoTitular, String ci, String nombres, String apellidos,
                                              LocalDate fechaNacimiento) {
    }

    private final PacienteService pacientes;
    private final ResolutorUsuario resolutor;

    public PacienteController(PacienteService pacientes, ResolutorUsuario resolutor) {
        this.pacientes = pacientes;
        this.resolutor = resolutor;
    }

    @PostMapping("/titular")
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDTO registrarTitular(@RequestBody RegistroTitularDTO datos) {
        return pacientes.registrarTitular(datos);
    }

    @PostMapping("/beneficiario")
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDTO registrarBeneficiario(@RequestHeader HttpHeaders cabeceras,
                                             @RequestBody RegistroBeneficiarioRequest d) {
        UsuarioAutenticado u = Permisos.exigir(resolutor, cabeceras,
                "Solo el titular o enfermeria pueden registrar beneficiarios", RolUsuario.PACIENTE, RolUsuario.ENFERMERIA);
        if (u.rol() == RolUsuario.PACIENTE) {
            pacientes.exigirMismaAfiliacion(u.personaId(), d.numeroAfiliadoTitular());
        }
        PacienteDTO datos = new PacienteDTO(0, d.numeroAfiliadoTitular(), d.ci(), d.nombres(), d.apellidos(),
                d.fechaNacimiento(), false);
        return pacientes.registrarBeneficiario(d.numeroAfiliadoTitular(), d.ci(), datos);
    }

    @GetMapping
    public List<PacienteDTO> buscar(@RequestHeader HttpHeaders cabeceras,
                                    @RequestParam(name = "afiliado", required = false) String afiliado,
                                    @RequestParam(name = "ci", required = false) String ci) {
        UsuarioAutenticado u = Permisos.exigir(resolutor, cabeceras, "No tiene permiso para consultar pacientes",
                RolUsuario.PACIENTE, RolUsuario.ENFERMERIA, RolUsuario.ADMINISTRADOR);
        if (u.rol() == RolUsuario.PACIENTE) {
            pacientes.exigirMismaAfiliacion(u.personaId(), afiliado);
        }
        return pacientes.buscar(afiliado, ci);
    }
}
