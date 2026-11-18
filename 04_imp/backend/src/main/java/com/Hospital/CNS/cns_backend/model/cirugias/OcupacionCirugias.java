package com.Hospital.CNS.cns_backend.model.cirugias;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.fichasinternas.AsignadorFichaInterna;

/** Para asignar fichas internas: un paciente o un especialista que esta en cirugia no esta libre. */
public class OcupacionCirugias implements AsignadorFichaInterna.OcupacionExtra {

    private final CirugiaRepository cirugias;

    public OcupacionCirugias(CirugiaRepository cirugias) {
        this.cirugias = cirugias;
    }

    @Override
    public List<LocalDateTime[]> delPaciente(long pacienteId, LocalDate dia) {
        List<LocalDateTime[]> lista = new ArrayList<>();
        for (CirugiaDTO c : cirugias.delPaciente(pacienteId)) {
            if (c.estado() != EstadoCirugia.CANCELADA && c.inicio().toLocalDate().equals(dia)) {
                lista.add(new LocalDateTime[] {c.inicio(), c.fin()});
            }
        }
        return lista;
    }

    @Override
    public List<LocalDateTime[]> delMedico(long medicoId, LocalDate dia) {
        List<LocalDateTime[]> lista = new ArrayList<>();
        for (CirugiaDTO c : cirugias.delCirujanoEnFecha(medicoId, dia)) {
            lista.add(new LocalDateTime[] {c.inicio(), c.fin()});
        }
        return lista;
    }
}
