package com.Hospital.CNS.cns_backend.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.CuentaDTO;
import com.Hospital.CNS.cns_backend.model.identidad.NuevaCuentaDTO;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioService;

/**
 * Cuentas del personal (solo ADMINISTRADOR): GET y POST /api/v1/admin/usuarios, DELETE /{id} (baja logica).
 * Los pacientes no se crean aqui: lo hacen ellos en el registro del titular.
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarios;
    private final ResolutorUsuario resolutor;

    public AdminUsuarioController(UsuarioService usuarios, ResolutorUsuario resolutor) {
        this.usuarios = usuarios;
        this.resolutor = resolutor;
    }

    @GetMapping
    public List<CuentaDTO> listar(@RequestHeader HttpHeaders cabeceras) {
        exigirAdministrador(cabeceras);
        return usuarios.listarCuentasPersonal();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaDTO crear(@RequestHeader HttpHeaders cabeceras, @RequestBody NuevaCuentaDTO datos) {
        exigirAdministrador(cabeceras);
        return usuarios.crearCuentaPersonal(datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        UsuarioAutenticado admin = exigirAdministrador(cabeceras);
        usuarios.darDeBajaCuenta(id, admin.usuarioId());
    }

    private UsuarioAutenticado exigirAdministrador(HttpHeaders cabeceras) {
        return Permisos.exigir(resolutor, cabeceras, "Solo el administrador puede gestionar las cuentas",
                RolUsuario.ADMINISTRADOR);
    }
}
