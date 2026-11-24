package com.Hospital.CNS.cns_backend.model.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** T100-19: reglas del catalogo de quirofanos. */
class QuirofanoCatalogoServiceTest {

    private final FakeQuirofanoRepository repo = new FakeQuirofanoRepository();
    private final Map<Integer, Integer> programadas = new HashMap<>();
    private final QuirofanoCatalogoService service =
            new QuirofanoCatalogoService(repo, id -> programadas.getOrDefault(id, 0));

    @Test
    void crearQuirofano_limpieza29_y46_rechaza_30_ok() {
        assertCodigo("LIMPIEZA_FUERA_DE_RANGO", () -> service.crear(datos("G-1", TipoEspecialidad.GASTROENTEROLOGIA, 29)));
        assertCodigo("LIMPIEZA_FUERA_DE_RANGO", () -> service.crear(datos("G-1", TipoEspecialidad.GASTROENTEROLOGIA, 46)));

        QuirofanoVistaDTO q = service.crear(datos(" G-1 ", TipoEspecialidad.GASTROENTEROLOGIA, 30));

        assertEquals("G-1", q.nombre());
        assertEquals(1, repo.quirofanos.size());
    }

    @Test
    void crearQuirofano_infectologia_ESPECIALIDAD_NO_OPERA() {
        assertCodigo("ESPECIALIDAD_NO_OPERA", () -> service.crear(datos("I-1", TipoEspecialidad.INFECTOLOGIA, 30)));
    }

    @Test
    void nombreRepetido_y_horarioAlReves() {
        service.crear(datos("G-1", TipoEspecialidad.GASTROENTEROLOGIA, 30));

        assertCodigo("QUIROFANO_DUPLICADO", () -> service.crear(datos("g-1", TipoEspecialidad.HEMATOLOGIA, 30)));
        assertCodigo("HORARIO_INVALIDO", () -> service.crear(new QuirofanoDTO(0, "H-1", TipoEspecialidad.HEMATOLOGIA,
                LocalTime.of(19, 0), LocalTime.of(7, 0), 30, true)));
    }

    @Test
    void cambiarEspecialidad_oDesactivar_conCirugias_409() {
        QuirofanoVistaDTO q = service.crear(datos("G-1", TipoEspecialidad.GASTROENTEROLOGIA, 30));
        programadas.put(q.quirofanoId(), 2);

        assertCodigo("QUIROFANO_CON_CIRUGIAS",
                () -> service.editar(q.quirofanoId(), datos("G-1", TipoEspecialidad.DERMATOLOGIA, 30)));
        assertCodigo("QUIROFANO_CON_CIRUGIAS", () -> service.desactivar(q.quirofanoId()));
        assertEquals(45, service.editar(q.quirofanoId(), datos("G-1", TipoEspecialidad.GASTROENTEROLOGIA, 45))
                .limpiezaMin(), "sin cambiar la especialidad si se puede editar");

        programadas.clear();
        service.desactivar(q.quirofanoId());
        assertFalse(repo.porId(q.quirofanoId()).orElseThrow().activo());
    }

    private static QuirofanoDTO datos(String nombre, TipoEspecialidad esp, int limpieza) {
        return new QuirofanoDTO(0, nombre, esp, LocalTime.of(7, 0), LocalTime.of(19, 0), limpieza, true);
    }

    private static void assertCodigo(String codigo, Runnable accion) {
        DominioException e = assertThrows(DominioException.class, accion::run);
        assertEquals(codigo, e.getCodigo());
    }
}
