package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaService;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaVistaDTO;
import com.Hospital.CNS.cns_backend.model.fichasinternas.PacienteParaFichaDTO;
import com.Hospital.CNS.cns_backend.model.fichasinternas.SolicitudFichaInterna;

/**
 * Fichas internas (T100-20/21/22). El medicoId sale siempre del token, nunca del request.
 *
 * <pre>
 * Medico general / pediatra:
 *   POST /api/v1/medico/fichas-internas                       -> 201 ficha (CU-21)
 *   GET  /api/v1/medico/fichas-internas/por-cita/{citaId}     -> ficha o 404
 *   GET  /api/v1/medico/fichas-internas/cola?especialidad=&pediatrica=   (CU-25)
 *   GET  /api/v1/medico/fichas-internas/pacientes?afiliado=   (urgencia sin cita)
 *   GET  /api/v1/medico/pacientes/{pacienteId}/fichas-internas (historial)
 * Especialista (CU-23):
 *   GET  /api/v1/especialista/fichas-internas?fecha=
 *   POST /api/v1/especialista/fichas-internas/{id}/atendida   {observaciones}
 *   POST /api/v1/especialista/fichas-internas/{id}/no-se-presento
 * Medico o administrador:
 *   GET  /api/v1/fichas-internas/{id}
 * </pre>
 */
@RestController
public class FichaInternaController {

    private final FichaInternaService service;
    private final ResolutorUsuario resolutor;

    public FichaInternaController(FichaInternaService service, ResolutorUsuario resolutor) {
        this.service = service;
        this.resolutor = resolutor;
    }

    @PostMapping("/api/v1/medico/fichas-internas")
    public ResponseEntity<FichaInternaVistaDTO> generar(@RequestHeader HttpHeaders cabeceras,
                                                        @RequestBody SolicitudFichaInterna solicitud) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generar(solicitud, medico.personaId()));
    }

    @GetMapping("/api/v1/medico/fichas-internas/por-cita/{citaId}")
    public ResponseEntity<FichaInternaVistaDTO> deCita(@RequestHeader HttpHeaders cabeceras, @PathVariable long citaId) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        FichaInternaVistaDTO f = service.deCita(citaId, medico.personaId());
        return f == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(f);
    }

    @GetMapping("/api/v1/medico/fichas-internas/cola")
    public List<FichaInternaVistaDTO> cola(@RequestHeader HttpHeaders cabeceras,
                                           @RequestParam("especialidad") TipoEspecialidad especialidad,
                                           @RequestParam(name = "pediatrica", defaultValue = "false") boolean pediatrica) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return service.cola(especialidad, pediatrica, medico.personaId());
    }

    @GetMapping("/api/v1/medico/fichas-internas/pacientes")
    public List<PacienteParaFichaDTO> pacientes(@RequestHeader HttpHeaders cabeceras,
                                                @RequestParam("afiliado") String afiliado) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return service.buscarPacientes(afiliado, medico.personaId());
    }

    @GetMapping("/api/v1/medico/pacientes/{pacienteId}/fichas-internas")
    public List<FichaInternaVistaDTO> delPaciente(@RequestHeader HttpHeaders cabeceras, @PathVariable long pacienteId) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return service.delPaciente(pacienteId, medico.personaId());
    }

    @GetMapping("/api/v1/especialista/fichas-internas")
    public List<FichaInternaVistaDTO> agenda(@RequestHeader HttpHeaders cabeceras,
                                             @RequestParam(name = "fecha", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return service.agendaEspecialista(medico.personaId(), fecha);
    }

    @PostMapping("/api/v1/especialista/fichas-internas/{id}/atendida")
    public FichaInternaVistaDTO atendida(@RequestHeader HttpHeaders cabeceras, @PathVariable long id,
                                         @RequestBody(required = false) Map<String, String> cuerpo) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        String observaciones = cuerpo == null ? null : cuerpo.get("observaciones");
        return service.marcarAtendida(id, medico.personaId(), observaciones);
    }

    @PostMapping("/api/v1/especialista/fichas-internas/{id}/no-se-presento")
    public FichaInternaVistaDTO noSePresento(@RequestHeader HttpHeaders cabeceras, @PathVariable long id) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return service.noSePresento(id, medico.personaId());
    }

    @GetMapping("/api/v1/fichas-internas/{id}")
    public FichaInternaVistaDTO porId(@RequestHeader HttpHeaders cabeceras, @PathVariable long id) {
        UsuarioAutenticado u = Permisos.exigir(resolutor, cabeceras, "Solo el personal medico ve las fichas internas",
                RolUsuario.MEDICO, RolUsuario.ADMINISTRADOR);
        return service.porId(id, u.rol() == RolUsuario.MEDICO ? u.personaId() : null);
    }
}
