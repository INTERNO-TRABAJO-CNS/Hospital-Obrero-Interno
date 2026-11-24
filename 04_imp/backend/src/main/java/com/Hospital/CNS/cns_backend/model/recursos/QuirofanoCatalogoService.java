package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.List;
import java.util.function.IntUnaryOperator;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * CU-02 (T100-19): catalogo de quirofanos del administrador. Reglas: nombre unico entre los activos; especialidad
 * que opera (ESPECIALIDAD_NO_OPERA); apertura antes del cierre (HORARIO_INVALIDO); limpieza de 30 a 45 min
 * (LIMPIEZA_FUERA_DE_RANGO). No se comparte: con cirugias programadas a futuro no se cambia la especialidad ni se
 * desactiva (QUIROFANO_CON_CIRUGIAS). Nunca se borra.
 */
public class QuirofanoCatalogoService {

    public static final int LIMPIEZA_MINIMA = 30;
    public static final int LIMPIEZA_MAXIMA = 45;

    private final QuirofanoRepository quirofanos;

    /** Cirugias PROGRAMADA a futuro de un quirofano (lo da el repositorio de cirugias). */
    private final IntUnaryOperator programadasFuturas;

    public QuirofanoCatalogoService(QuirofanoRepository quirofanos, IntUnaryOperator programadasFuturas) {
        this.quirofanos = quirofanos;
        this.programadasFuturas = programadasFuturas;
    }

    public List<QuirofanoVistaDTO> listar() {
        return quirofanos.listar().stream().map(this::vista).toList();
    }

    public QuirofanoVistaDTO crear(QuirofanoDTO datos) {
        QuirofanoDTO nuevo = validar(new QuirofanoDTO(0, datos.nombre(), datos.especialidad(), datos.apertura(),
                datos.cierre(), datos.limpiezaMin(), true));
        nuevo = new QuirofanoDTO(quirofanos.nextIdentity(), nuevo.nombre(), nuevo.especialidad(), nuevo.apertura(),
                nuevo.cierre(), nuevo.limpiezaMin(), true);
        guardar(nuevo);
        return vista(nuevo);
    }

    public QuirofanoVistaDTO editar(int quirofanoId, QuirofanoDTO datos) {
        QuirofanoDTO actual = existente(quirofanoId);
        QuirofanoDTO nuevo = validar(new QuirofanoDTO(quirofanoId, datos.nombre(), datos.especialidad(),
                datos.apertura(), datos.cierre(), datos.limpiezaMin(), actual.activo()));
        if (nuevo.especialidad() != actual.especialidad() && programadasFuturas.applyAsInt(quirofanoId) > 0) {
            throw conCirugias();
        }
        guardar(nuevo);
        return vista(nuevo);
    }

    public void desactivar(int quirofanoId) {
        QuirofanoDTO actual = existente(quirofanoId);
        if (programadasFuturas.applyAsInt(quirofanoId) > 0) {
            throw conCirugias();
        }
        guardar(new QuirofanoDTO(actual.quirofanoId(), actual.nombre(), actual.especialidad(), actual.apertura(),
                actual.cierre(), actual.limpiezaMin(), false));
    }

    private static QuirofanoDTO validar(QuirofanoDTO q) {
        String nombre = q.nombre() == null ? "" : q.nombre().trim();
        if (nombre.isEmpty() || nombre.length() > 40 || q.especialidad() == null || q.apertura() == null
                || q.cierre() == null) {
            throw new DominioException("DATOS_INVALIDOS", "Complete nombre (hasta 40 caracteres), especialidad y horario");
        }
        if (!q.especialidad().operaEnQuirofano()) {
            throw new DominioException("ESPECIALIDAD_NO_OPERA",
                    "Solo Gastroenterologia, Hematologia y Dermatologia operan en quirofano");
        }
        if (!q.apertura().isBefore(q.cierre())) {
            throw new DominioException("HORARIO_INVALIDO", "La apertura debe ser antes del cierre");
        }
        if (q.limpiezaMin() < LIMPIEZA_MINIMA || q.limpiezaMin() > LIMPIEZA_MAXIMA) {
            throw new DominioException("LIMPIEZA_FUERA_DE_RANGO",
                    "La limpieza debe durar entre " + LIMPIEZA_MINIMA + " y " + LIMPIEZA_MAXIMA + " minutos");
        }
        return new QuirofanoDTO(q.quirofanoId(), nombre, q.especialidad(), q.apertura(), q.cierre(), q.limpiezaMin(),
                q.activo());
    }

    private void guardar(QuirofanoDTO q) {
        if (!quirofanos.store(q)) {
            throw new DominioException("QUIROFANO_DUPLICADO", "Ya existe un quirofano activo con ese nombre");
        }
    }

    private QuirofanoDTO existente(int quirofanoId) {
        return quirofanos.porId(quirofanoId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "Quirofano no encontrado"));
    }

    private QuirofanoVistaDTO vista(QuirofanoDTO q) {
        return new QuirofanoVistaDTO(q.quirofanoId(), q.nombre(), q.especialidad(), q.apertura(), q.cierre(),
                q.limpiezaMin(), q.activo(), programadasFuturas.applyAsInt(q.quirofanoId()));
    }

    private static DominioException conCirugias() {
        return new DominioException("QUIROFANO_CON_CIRUGIAS",
                "No se puede cambiar la especialidad ni desactivar un quirofano con cirugias programadas a futuro");
    }
}
