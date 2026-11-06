package com.Hospital.CNS.cns_backend.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * Traduce los errores a JSON {@code {codigo, mensaje}} con el estado HTTP segun el codigo.
 *
 * <pre>
 * 400  DATOS_INVALIDOS, ENCABEZADO_FALTANTE, ENCABEZADO_INVALIDO, ROL_INVALIDO
 * 403  ACCESO_DENEGADO, NO_ES_DEL_PACIENTE, NO_ES_DE_SU_AGENDA
 * 404  NO_ENCONTRADA
 * 409  ESTADO_NO_PERMITE, CITA_DUPLICADA_EN_DIA, HORARIO_OCUPADO
 * 422  cualquier otro codigo (regla de negocio incumplida)
 * </pre>
 *
 * Si necesitas un codigo nuevo en 400/403/404/409, avisa a Benjamin para agregarlo aqui.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    private static final Set<String> CODIGOS_400 =
            Set.of("DATOS_INVALIDOS", "ENCABEZADO_FALTANTE", "ENCABEZADO_INVALIDO", "ROL_INVALIDO");
    private static final Set<String> CODIGOS_403 =
            Set.of("ACCESO_DENEGADO", "NO_ES_DEL_PACIENTE", "NO_ES_DE_SU_AGENDA");
    private static final Set<String> CODIGOS_404 = Set.of("NO_ENCONTRADA");
    private static final Set<String> CODIGOS_409 =
            Set.of("ESTADO_NO_PERMITE", "CITA_DUPLICADA_EN_DIA", "HORARIO_OCUPADO");

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

    /** Estado HTTP de un codigo de error. Visible para probarlo sin levantar Spring. */
    static int estadoDe(String codigo) {
        if (CODIGOS_400.contains(codigo)) {
            return 400;
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
        return 422;
    }
}
