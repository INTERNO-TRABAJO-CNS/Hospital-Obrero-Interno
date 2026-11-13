package com.Hospital.CNS.cns_backend.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * Traduce los errores a JSON {@code {codigo, mensaje}} con el estado HTTP segun el codigo.
 *
 * <pre>
 * 400  DATOS_INVALIDOS, ENCABEZADO_FALTANTE, ENCABEZADO_INVALIDO, ROL_INVALIDO, RECETA_INVALIDA
 * 401  CREDENCIALES_INVALIDAS, NO_AUTENTICADO, SESION_VENCIDA
 * 403  ACCESO_DENEGADO, NO_ES_DEL_PACIENTE, NO_ES_DE_SU_AGENDA, PACIENTE_FUERA_DE_AGENDA
 * 404  NO_ENCONTRADA, AFILIADO_NO_ENCONTRADO
 * 409  ESTADO_NO_PERMITE, CITA_DUPLICADA_EN_DIA, HORARIO_OCUPADO, CAMBIO_AFECTA_CITAS, HORARIO_SOLAPADO,
 *      YA_REGISTRADO, USUARIO_DUPLICADO, CONSULTORIO_DUPLICADO, DIA_NO_HABIL_DUPLICADO
 * 429  INTENTOS_EXCEDIDOS
 * 422  cualquier otro codigo (regla de negocio incumplida)
 * 500  ERROR_INTERNO: cualquier error inesperado (el detalle va al log, nunca al cliente)
 * </pre>
 *
 * Si necesitas un codigo nuevo en 400/401/403/404/409/429, avisa a Benjamin para agregarlo aqui.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    private static final Set<String> CODIGOS_400 =
            Set.of("DATOS_INVALIDOS", "ENCABEZADO_FALTANTE", "ENCABEZADO_INVALIDO", "ROL_INVALIDO", "RECETA_INVALIDA");
    private static final Set<String> CODIGOS_401 = Set.of("CREDENCIALES_INVALIDAS", "NO_AUTENTICADO", "SESION_VENCIDA");
    private static final Set<String> CODIGOS_403 =
            Set.of("ACCESO_DENEGADO", "NO_ES_DEL_PACIENTE", "NO_ES_DE_SU_AGENDA", "PACIENTE_FUERA_DE_AGENDA");
    private static final Set<String> CODIGOS_404 = Set.of("NO_ENCONTRADA", "AFILIADO_NO_ENCONTRADO");
    private static final Set<String> CODIGOS_409 =
            Set.of("ESTADO_NO_PERMITE", "CITA_DUPLICADA_EN_DIA", "HORARIO_OCUPADO", "CAMBIO_AFECTA_CITAS", "HORARIO_SOLAPADO",
                    "YA_REGISTRADO", "USUARIO_DUPLICADO", "CONSULTORIO_DUPLICADO", "DIA_NO_HABIL_DUPLICADO");
    private static final Set<String> CODIGOS_429 = Set.of("INTENTOS_EXCEDIDOS");

    /** Cuerpo de todas las respuestas de error. */
    public record ErrorRespuesta(String codigo, String mensaje) {
    }

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<ErrorRespuesta> manejarDominio(DominioException e) {
        int estado = estadoDe(e.getCodigo());
        LOG.debug("DominioException {} -> HTTP {}: {}", e.getCodigo(), estado, e.getMessage());
        return ResponseEntity.status(estado).body(new ErrorRespuesta(e.getCodigo(), e.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorRespuesta> manejarDatosInvalidos(Exception e) {
        LOG.debug("Datos invalidos: {}", e.getMessage());
        return ResponseEntity.status(400)
                .body(new ErrorRespuesta("DATOS_INVALIDOS", "Los datos enviados no son validos"));
    }

    /**
     * Errores propios de Spring MVC (ruta inexistente, metodo no permitido, parametro faltante...) conservan su
     * estado; cualquier otro error es del servidor: 500 sin detalles para el cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarInesperado(Exception e) {
        if (e instanceof ErrorResponse respuesta) {
            int estado = respuesta.getStatusCode().value();
            return ResponseEntity.status(estado).body(new ErrorRespuesta(codigoHttp(estado), mensajeHttp(estado)));
        }
        LOG.error("Error inesperado", e);
        return ResponseEntity.status(500)
                .body(new ErrorRespuesta("ERROR_INTERNO", "Ocurrio un error en el servidor. Intente de nuevo"));
    }

    /** Estado HTTP de un codigo de error. Visible para probarlo sin levantar Spring. */
    static int estadoDe(String codigo) {
        if (CODIGOS_400.contains(codigo)) {
            return 400;
        }
        if (CODIGOS_401.contains(codigo)) {
            return 401;
        }
        if (CODIGOS_403.contains(codigo)) {
            return 403;
        }
        if (CODIGOS_404.contains(codigo)) {
            return 404;
        }
        if (CODIGOS_409.contains(codigo)) {
            return 409;
        }
        if (CODIGOS_429.contains(codigo)) {
            return 429;
        }
        return 422;
    }

    private static String codigoHttp(int estado) {
        return switch (estado) {
            case 400 -> "DATOS_INVALIDOS";
            case 404 -> "NO_ENCONTRADA";
            case 405 -> "METODO_NO_PERMITIDO";
            default -> "ERROR_HTTP";
        };
    }

    private static String mensajeHttp(int estado) {
        return switch (estado) {
            case 400 -> "Los datos enviados no son validos";
            case 404 -> "No existe ese recurso";
            case 405 -> "Metodo no permitido en esta ruta";
            default -> "La peticion no se pudo atender (" + estado + ")";
        };
    }
}
