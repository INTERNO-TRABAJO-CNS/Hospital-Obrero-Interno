package com.Hospital.CNS.cns_backend.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.AdminPacienteService;
import com.Hospital.CNS.cns_backend.model.identidad.NuevoPacienteAdminDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteAdminDTO;

/**
 * Gestionar &gt; Pacientes (solo ADMINISTRADOR): GET y POST /api/v1/admin/pacientes, PUT /{id},
 * DELETE /{id} (baja logica) y POST /{id}/reactivar. Las reglas estan en {@link AdminPacienteService}.
 */
@RestController
@RequestMapping("/api/v1/admin/pacientes")
public class AdminPacienteController {

    private final AdminPacienteService pacientes;
    private final ResolutorUsuario resolutor;

    public AdminPacienteController(AdminPacienteService pacientes, ResolutorUsuario resolutor) {
        this.pacientes = pacientes;
        this.resolutor = resolutor;
    }

    @GetMapping
    public List<PacienteAdminDTO> listar(@RequestHeader HttpHeaders cabeceras) {
        exigirAdministrador(cabeceras);
        return pacientes.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteAdminDTO crear(@RequestHeader HttpHeaders cabeceras, @RequestBody NuevoPacienteAdminDTO datos) {
        exigirAdministrador(cabeceras);
        return pacientes.crear(datos);
    }

    @PutMapping("/{id}")
    public PacienteAdminDTO modificar(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id,
                                      @RequestBody NuevoPacienteAdminDTO datos) {
        exigirAdministrador(cabeceras);
        return pacientes.modificar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        exigirAdministrador(cabeceras);
        pacientes.darDeBaja(id);
    }

    @PostMapping("/{id}/reactivar")
    public PacienteAdminDTO reactivar(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        exigirAdministrador(cabeceras);
        return pacientes.reactivar(id);
    }

    private void exigirAdministrador(HttpHeaders cabeceras) {
        Permisos.exigir(resolutor, cabeceras, "Solo el administrador puede gestionar los pacientes",
                RolUsuario.ADMINISTRADOR);
    }
}
