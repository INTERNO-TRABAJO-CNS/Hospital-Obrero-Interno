package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.historial.CambioCitaVistaDTO;
import com.Hospital.CNS.cns_backend.model.historial.HistorialCambiosService;

/**
 * CU-18 (T100-13): {@code GET /api/v1/admin/cambios?cita=&usuario=&desde=YYYY-MM-DD&hasta=YYYY-MM-DD}. Solo
 * ENFERMERIA y ADMINISTRADOR; solo lectura (no hay POST, PUT ni DELETE).
 */
@RestController
public class CambiosController {

    private final HistorialCambiosService historial;
    private final ResolutorUsuario resolutor;

    public CambiosController(HistorialCambiosService historial, ResolutorUsuario resolutor) {
        this.historial = historial;
        this.resolutor = resolutor;
    }

    @GetMapping("/api/v1/admin/cambios")
    public List<CambioCitaVistaDTO> consultar(
            @RequestHeader HttpHeaders cabeceras,
            @RequestParam(name = "cita", required = false) Long citaId,
            @RequestParam(name = "usuario", required = false) String usuario,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta) {
        Permisos.exigir(resolutor, cabeceras, "Solo enfermería y el administrador consultan el historial de cambios",
                RolUsuario.ENFERMERIA, RolUsuario.ADMINISTRADOR);
        return historial.consultarConCita(citaId, usuario, desde, hasta);
    }
}
