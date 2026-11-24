package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;

/**
 * Frena los intentos a fuerza bruta: tras {@code maximo} fallos dentro de {@code ventana} para la misma clave
 * (un usuario en el login, un numero de afiliado en el registro), bloquea esa clave durante {@code bloqueo}
 * y responde INTENTOS_EXCEDIDOS (429). Vive en memoria: alcanza para un solo servidor.
 */
public class LimitadorIntentos {

    private static final int LIMPIAR_DESDE = 10_000;

    private record Registro(int fallos, LocalDateTime primero, LocalDateTime bloqueadoHasta) {
    }

    private final int maximo;
    private final Duration ventana;
    private final Duration bloqueo;
    private final Reloj reloj;
    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public LimitadorIntentos(int maximo, Duration ventana, Duration bloqueo, Reloj reloj) {
        this.maximo = maximo;
        this.ventana = ventana;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    /** Nunca bloquea (pruebas y constructores de compatibilidad). */
    public static LimitadorIntentos sinLimite() {
        return new LimitadorIntentos(Integer.MAX_VALUE, Duration.ofMinutes(1), Duration.ZERO, new RelojSistema());
    }

    /** Lanza INTENTOS_EXCEDIDOS si la clave esta bloqueada ahora. */
    public void comprobar(String clave) {
        Registro r = registros.get(clave);
        LocalDateTime ahora = reloj.ahora();
        if (r != null && r.bloqueadoHasta() != null && ahora.isBefore(r.bloqueadoHasta())) {
            long minutos = Math.max(1, (Duration.between(ahora, r.bloqueadoHasta()).toSeconds() + 59) / 60);
            throw new DominioException("INTENTOS_EXCEDIDOS",
                    "Demasiados intentos fallidos. Intente de nuevo en " + minutos + " minuto(s)");
        }
    }

    public void registrarFallo(String clave) {
        LocalDateTime ahora = reloj.ahora();
        registros.compute(clave, (k, r) -> {
            boolean empiezaDeNuevo = r == null
                    || r.primero().plus(ventana).isBefore(ahora)
                    || (r.bloqueadoHasta() != null && !ahora.isBefore(r.bloqueadoHasta()));
            Registro base = empiezaDeNuevo ? new Registro(0, ahora, null) : r;
            int fallos = base.fallos() + 1;
            return new Registro(fallos, base.primero(), fallos >= maximo ? ahora.plus(bloqueo) : null);
        });
        if (registros.size() > LIMPIAR_DESDE) {
            registros.entrySet().removeIf(e -> e.getValue().primero().plus(ventana).plus(bloqueo).isBefore(ahora));
        }
    }

    /** Un intento correcto borra los fallos anteriores. */
    public void limpiar(String clave) {
        registros.remove(clave);
    }
}
