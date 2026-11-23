package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.reportes.FichaSupervisionDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ResumenEspecialidadDTO;
import com.Hospital.CNS.cns_backend.model.reportes.SupervisionService;

/**
 * CU-19: agenda de supervision del administrador, solo lectura. Sin fecha usa la de hoy.
 *
 * <pre>
 * GET /api/v1/admin/supervision?fecha=2026-11-23                      resumen por especialidad y medico
 * GET /api/v1/admin/supervision/medicos/{medicoId}/fichas?fecha=...   fichas de un medico
 * </pre>
 *
 * [1a] sin rol ADMINISTRADOR: ACCESO_DENEGADO (403). [3b] POST/PUT/PATCH/DELETE: SOLO_LECTURA (403).
 */
@RestController
@RequestMapping("/api/v1/admin/supervision")
public class SupervisionController {

    private final SupervisionService supervision;
    private final ResolutorUsuario resolutor;

    public SupervisionController(SupervisionService supervision, ResolutorUsuario resolutor) {
        this.supervision = supervision;
        this.resolutor = resolutor;
    }

    @GetMapping
    public List<ResumenEspecialidadDTO> resumen(
            @RequestHeader HttpHeaders cabeceras,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {
        exigirAdministrador(resolutor, cabeceras);
        return supervision.resumenDelDia(fecha);
    }

    @GetMapping("/medicos/{medicoId}/fichas")
    public List<FichaSupervisionDTO> fichasDeMedico(
            @RequestHeader HttpHeaders cabeceras,
            @PathVariable("medicoId") long medicoId,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {
        exigirAdministrador(resolutor, cabeceras);
        return supervision.fichasDeMedico(medicoId, fecha);
    }

    /** [3b] La agenda de supervision no modifica citas. */
    @RequestMapping(value = {"", "/**"},
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public void intentoModificar(@RequestHeader HttpHeaders cabeceras) {
        exigirAdministrador(resolutor, cabeceras);
        throw soloLectura();
    }

    static void exigirAdministrador(ResolutorUsuario resolutor, HttpHeaders cabeceras) {
        Permisos.exigir(resolutor, cabeceras, "Solo el administrador puede ver la supervision y los reportes",
                RolUsuario.ADMINISTRADOR);
    }

    static DominioException soloLectura() {
        return new DominioException("SOLO_LECTURA",
                "La supervision y los reportes son de solo lectura: no se pueden modificar citas desde aqui");
    }
}
