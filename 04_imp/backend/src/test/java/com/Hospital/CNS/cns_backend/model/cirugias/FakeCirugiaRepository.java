package com.Hospital.CNS.cns_backend.model.cirugias;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CirugiaRepository en memoria que rechaza los MISMOS choques que el EXCLUDE de la base: quirofano con su
 * limpieza, cirujano y paciente (las CANCELADA no cuentan). {@code cirugias} es publica para los tests.
 */
public class FakeCirugiaRepository implements CirugiaRepository {

    public final List<CirugiaDTO> cirugias = new ArrayList<>();
    private long siguiente = 58;

    @Override
    public synchronized long nextIdentity() {
        return siguiente++;
    }

    @Override
    public synchronized boolean insertar(CirugiaDTO c) {
        if (choca(c, c.cirugiaId())) {
            return false;
        }
        cirugias.add(c);
        return true;
    }

    @Override
    public synchronized boolean reprogramar(long cirugiaId, int quirofanoId, LocalDateTime nuevoInicio) {
        int i = indice(cirugiaId);
        if (i < 0 || cirugias.get(i).estado() != EstadoCirugia.PROGRAMADA) {
            return false;
        }
        CirugiaDTO c = cirugias.get(i);
        CirugiaDTO movida = new CirugiaDTO(c.cirugiaId(), c.fichaInternaId(), c.pacienteId(), c.nombrePaciente(),
                c.numeroAfiliado(), c.fechaNacimiento(), c.cirujanoId(), c.nombreCirujano(), quirofanoId,
                c.nombreQuirofano(), c.especialidad(), nuevoInicio, c.duracionMin(), c.limpiezaMin(), c.gravedad(),
                c.urgencia(), c.adultoMayor(), c.ordenLlegada(), c.estado(), c.creadaEn());
        if (choca(movida, cirugiaId)) {
            return false;
        }
        cirugias.set(i, movida);
        return true;
    }

    @Override
    public synchronized Optional<CirugiaDTO> porId(long cirugiaId) {
        int i = indice(cirugiaId);
        return i < 0 ? Optional.empty() : Optional.of(cirugias.get(i));
    }

    @Override
    public synchronized boolean cambiarEstado(long cirugiaId, EstadoCirugia desde, EstadoCirugia hasta) {
        int i = indice(cirugiaId);
        if (i < 0 || cirugias.get(i).estado() != desde) {
            return false;
        }
        CirugiaDTO c = cirugias.get(i);
        cirugias.set(i, new CirugiaDTO(c.cirugiaId(), c.fichaInternaId(), c.pacienteId(), c.nombrePaciente(),
                c.numeroAfiliado(), c.fechaNacimiento(), c.cirujanoId(), c.nombreCirujano(), c.quirofanoId(),
                c.nombreQuirofano(), c.especialidad(), c.inicio(), c.duracionMin(), c.limpiezaMin(), c.gravedad(),
                c.urgencia(), c.adultoMayor(), c.ordenLlegada(), hasta, c.creadaEn()));
        return true;
    }

    @Override
    public synchronized List<CirugiaDTO> deEspecialidadEnFecha(TipoEspecialidad especialidad, LocalDate fecha) {
        return filtrar(c -> c.especialidad() == especialidad && c.estado() != EstadoCirugia.CANCELADA
                && c.inicio().isBefore(fecha.plusDays(1).atStartOfDay())
                && c.finConLimpieza().isAfter(fecha.atStartOfDay()));
    }

    @Override
    public synchronized List<CirugiaDTO> delCirujanoEnFecha(long cirujanoId, LocalDate fecha) {
        return filtrar(c -> c.cirujanoId() == cirujanoId && c.estado() != EstadoCirugia.CANCELADA
                && c.inicio().toLocalDate().equals(fecha));
    }

    @Override
    public synchronized List<CirugiaDTO> delPaciente(long pacienteId) {
        return filtrar(c -> c.pacienteId() == pacienteId);
    }

    @Override
    public synchronized List<CirugiaDTO> programadasEntre(TipoEspecialidad especialidad, LocalDateTime desde,
                                                          LocalDateTime hasta) {
        return filtrar(c -> c.especialidad() == especialidad && c.estado() == EstadoCirugia.PROGRAMADA
                && !c.inicio().isBefore(desde) && !c.inicio().isAfter(hasta));
    }

    @Override
    public synchronized int contarProgramadasDesde(int quirofanoId, LocalDateTime desde) {
        return filtrar(c -> c.quirofanoId() == quirofanoId && c.estado() == EstadoCirugia.PROGRAMADA
                && !c.inicio().isBefore(desde)).size();
    }

    private boolean choca(CirugiaDTO n, long ignorar) {
        if (n.estado() == EstadoCirugia.CANCELADA) {
            return false;
        }
        for (CirugiaDTO o : cirugias) {
            if (o.cirugiaId() == ignorar || o.estado() == EstadoCirugia.CANCELADA) {
                continue;
            }
            boolean sala = o.quirofanoId() == n.quirofanoId() && o.inicio().isBefore(n.finConLimpieza())
                    && n.inicio().isBefore(o.finConLimpieza());
            boolean persona = (o.cirujanoId() == n.cirujanoId() || o.pacienteId() == n.pacienteId())
                    && o.inicio().isBefore(n.fin()) && n.inicio().isBefore(o.fin());
            if (sala || persona) {
                return true;
            }
        }
        return false;
    }

    private int indice(long id) {
        for (int i = 0; i < cirugias.size(); i++) {
            if (cirugias.get(i).cirugiaId() == id) {
                return i;
            }
        }
        return -1;
    }

    private List<CirugiaDTO> filtrar(Predicate<CirugiaDTO> condicion) {
        List<CirugiaDTO> lista = new ArrayList<>();
        for (CirugiaDTO c : cirugias) {
            if (condicion.test(c)) {
                lista.add(c);
            }
        }
        lista.sort(Comparator.comparing(CirugiaDTO::inicio));
        return lista;
    }
}
