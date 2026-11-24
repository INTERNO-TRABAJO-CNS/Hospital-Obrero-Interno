package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void cadaCodigoSeTraduceAlEstadoHttpEsperado() {
        assertEquals(400, estado("DATOS_INVALIDOS"));
        assertEquals(400, estado("ROL_INVALIDO"));
        assertEquals(403, estado("NO_ES_DEL_PACIENTE"));
        assertEquals(403, estado("NO_ES_DE_SU_AGENDA"));
        assertEquals(404, estado("NO_ENCONTRADA"));
        assertEquals(409, estado("ESTADO_NO_PERMITE"));
        assertEquals(409, estado("CITA_DUPLICADA_EN_DIA"));
        assertEquals(409, estado("CAMBIO_AFECTA_CITAS"));
        assertEquals(409, estado("HORARIO_SOLAPADO"));
    }

    @Test
    void codigosDeAgendaHistorialAtencionYPacientes() {
        assertEquals(400, estado("RECETA_INVALIDA"));
        assertEquals(401, estado("CREDENCIALES_INVALIDAS"));
        assertEquals(403, estado("PACIENTE_FUERA_DE_AGENDA"));
        assertEquals(404, estado("AFILIADO_NO_ENCONTRADO"));
        assertEquals(409, estado("YA_REGISTRADO"));
        assertEquals(409, estado("USUARIO_DUPLICADO"));
    }

    @Test
    void codigosDeSesionDuplicadosEIntentos() {
        assertEquals(401, estado("NO_AUTENTICADO"));
        assertEquals(401, estado("SESION_VENCIDA"));
        assertEquals(409, estado("CONSULTORIO_DUPLICADO"));
        assertEquals(409, estado("DIA_NO_HABIL_DUPLICADO"));
        assertEquals(429, estado("INTENTOS_EXCEDIDOS"));
    }

    @Test
    void errorInesperado_500SinDetallesParaElCliente() {
        ResponseEntity<ManejadorErrores.ErrorRespuesta> r = manejador.manejarInesperado(
                new IllegalStateException("No se pudo conectar a PostgreSQL: clave incorrecta"));

        assertEquals(500, r.getStatusCode().value());
        assertEquals("ERROR_INTERNO", r.getBody().codigo());
        assertEquals(false, r.getBody().mensaje().contains("PostgreSQL"));
    }

    @Test
    void unCodigoDesconocidoEsReglaDeNegocio422() {
        assertEquals(422, estado("LIMITE_FICHAS_MEDICO"));
    }

    @Test
    void elCuerpoLlevaCodigoYMensaje() {
        ResponseEntity<ManejadorErrores.ErrorRespuesta> respuesta =
                manejador.manejarDominio(new DominioException("NO_ENCONTRADA", "La cita no existe"));

        assertEquals("NO_ENCONTRADA", respuesta.getBody().codigo());
        assertEquals("La cita no existe", respuesta.getBody().mensaje());
    }

    private int estado(String codigo) {
        return manejador.manejarDominio(new DominioException(codigo, "x")).getStatusCode().value();
    }
}
