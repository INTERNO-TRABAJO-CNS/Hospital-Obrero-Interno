package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.reportes.CumplimientoMedicoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.IndicadoresDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteEstadoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteGrupoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteService;

/**
 * CU-19 (reportes) y CU-20 (indicadores), solo lectura. Periodo con {@code ?desde=AAAA-MM-DD&hasta=AAAA-MM-DD}.
 *
 * <pre>
 * GET /api/v1/admin/reportes/estado         citas por estado (con canceladas automaticas)
 * GET /api/v1/admin/reportes/cumplimiento   fichas atendidas por medico frente a la meta (5 a 7 por dia)
 * GET /api/v1/admin/reportes/especialidad   solicitudes por especialidad
 * GET /api/v1/admin/reportes/consultorio    fichas atendidas por consultorio
 * GET /api/v1/admin/reportes/indicadores    indicadores de gestion (CU-20)
 * </pre>
 *
 * [1a] sin rol ADMINISTRADOR: ACCESO_DENEGADO (403). POST/PUT/PATCH/DELETE: SOLO_LECTURA (403).
 */
@RestController
@RequestMapping("/api/v1/admin/reportes")
public class ReporteController {

    private final ReporteService reportes;
    private final ResolutorUsuario resolutor;

    public ReporteController(ReporteService reportes, ResolutorUsuario resolutor) {
        this.reportes = reportes;
        this.resolutor = resolutor;
    }

    @GetMapping("/estado")
    public List<ReporteEstadoDTO> porEstado(@RequestHeader HttpHeaders cabeceras,
                                            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                            LocalDate desde,
                                            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                            LocalDate hasta) {
        SupervisionController.exigirAdministrador(resolutor, cabeceras);
        return reportes.porEstado(desde, hasta);
    }

    @GetMapping("/cumplimiento")
    public List<CumplimientoMedicoDTO> cumplimiento(@RequestHeader HttpHeaders cabeceras,
                                                    @RequestParam("desde")
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                    @RequestParam("hasta")
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        SupervisionController.exigirAdministrador(resolutor, cabeceras);
        return reportes.cumplimiento(desde, hasta);
    }

    @GetMapping("/especialidad")
    public List<ReporteGrupoDTO> porEspecialidad(@RequestHeader HttpHeaders cabeceras,
                                                 @RequestParam("desde")
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                 @RequestParam("hasta")
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        SupervisionController.exigirAdministrador(resolutor, cabeceras);
        return reportes.porEspecialidad(desde, hasta);
    }

    @GetMapping("/consultorio")
    public List<ReporteGrupoDTO> porConsultorio(@RequestHeader HttpHeaders cabeceras,
                                                @RequestParam("desde")
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam("hasta")
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        SupervisionController.exigirAdministrador(resolutor, cabeceras);
        return reportes.porConsultorio(desde, hasta);
    }

    @GetMapping("/indicadores")
    public IndicadoresDTO indicadores(@RequestHeader HttpHeaders cabeceras,
                                      @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                      LocalDate desde,
                                      @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                      LocalDate hasta) {
        SupervisionController.exigirAdministrador(resolutor, cabeceras);
        return reportes.indicadores(desde, hasta);
    }

    /** Los reportes no modifican citas. */
    @RequestMapping(value = {"", "/**"},
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public void intentoModificar(@RequestHeader HttpHeaders cabeceras) {
        SupervisionController.exigirAdministrador(resolutor, cabeceras);
        throw SupervisionController.soloLectura();
    }
}
