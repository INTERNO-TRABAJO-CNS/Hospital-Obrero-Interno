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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.CatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilDTO;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/**
 * CU-02: catalogos del administrador. Solo traduce HTTP a {@link CatalogoService} y de vuelta:
 * no tiene SQL ni reglas de negocio. Los errores de regla salen como DominioException y los
 * convierte {@link ManejadorErrores}.
 *
 * <p>[1a] sin rol ADMINISTRADOR -> ACCESO_DENEGADO (403). El usuario se lee con el
 * ResolutorUsuario TEMPORAL (headers X-Usuario-Id, X-Rol, X-Persona-Id) hasta S3-08.</p>
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogoController {

    private final CatalogoService catalogo;
    private final ResolutorUsuario resolutor;

    public AdminCatalogoController(CatalogoService catalogo, ResolutorUsuario resolutor) {
        this.catalogo = catalogo;
        this.resolutor = resolutor;
    }

    // ------------------------------------------------------------------ medicos

    @GetMapping("/medicos")
    public List<MedicoDTO> listarMedicos(
            @RequestHeader HttpHeaders cabeceras,
            @RequestParam(name = "especialidad", required = false) TipoEspecialidad especialidad) {
        exigirAdministrador(cabeceras);
        return catalogo.listarMedicos(especialidad);
    }

    @PostMapping("/medicos")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicoDTO crearMedico(@RequestHeader HttpHeaders cabeceras, @RequestBody MedicoDTO datos) {
        exigirAdministrador(cabeceras);
        return catalogo.crearMedico(datos);
    }

    @PutMapping("/medicos/{id}")
    public MedicoDTO modificarMedico(@RequestHeader HttpHeaders cabeceras,
                                     @PathVariable("id") long id, @RequestBody MedicoDTO datos) {
        exigirAdministrador(cabeceras);
        return catalogo.modificarMedico(id, datos);
    }

    /** Baja logica: el medico queda con activo=false, no se borra. */
    @DeleteMapping("/medicos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBajaMedico(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        exigirAdministrador(cabeceras);
        catalogo.darDeBajaMedico(id);
    }

    // ------------------------------------------------------------- consultorios

    @GetMapping("/consultorios")
    public List<ConsultorioDTO> listarConsultorios(@RequestHeader HttpHeaders cabeceras) {
        exigirAdministrador(cabeceras);
        return catalogo.listarConsultorios();
    }

    @PostMapping("/consultorios")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultorioDTO crearConsultorio(@RequestHeader HttpHeaders cabeceras, @RequestBody ConsultorioDTO datos) {
        exigirAdministrador(cabeceras);
        return catalogo.crearConsultorio(datos);
    }

    // ----------------------------------------------------------------- horarios

    @GetMapping("/horarios")
    public List<HorarioDTO> listarHorarios(@RequestHeader HttpHeaders cabeceras) {
        exigirAdministrador(cabeceras);
        return catalogo.listarHorarios();
    }

    @PostMapping("/horarios")
    @ResponseStatus(HttpStatus.CREATED)
    public HorarioDTO definirHorario(@RequestHeader HttpHeaders cabeceras, @RequestBody HorarioDTO datos) {
        exigirAdministrador(cabeceras);
        return catalogo.definirHorario(datos);
    }

    // ---------------------------------------------------------- dias no habiles

    @GetMapping("/dias-no-habiles")
    public List<DiaNoHabilDTO> listarDiasNoHabiles(@RequestHeader HttpHeaders cabeceras) {
        exigirAdministrador(cabeceras);
        return catalogo.listarDiasNoHabiles();
    }

    @PostMapping("/dias-no-habiles")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaNoHabilDTO registrarDiaNoHabil(@RequestHeader HttpHeaders cabeceras, @RequestBody DiaNoHabilDTO datos) {
        exigirAdministrador(cabeceras);
        return catalogo.registrarDiaNoHabil(datos);
    }

    // ------------------------------------------------------------------ interno

    private void exigirAdministrador(HttpHeaders cabeceras) {
        UsuarioAutenticado usuario = resolutor.resolver(cabeceras::getFirst);
        if (usuario.rol() != RolUsuario.ADMINISTRADOR) {
            throw new DominioException("ACCESO_DENEGADO", "Solo el administrador puede gestionar los catalogos");
        }
    }
}
