package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** PacienteRepository en memoria. Simula UNIQUE(afiliado, ci) y un solo titular por afiliacion. */
public class FakePacienteRepository implements PacienteRepository {

    public final List<PacienteDTO> pacientes = new ArrayList<>();
    private long ultimoId = 0;

    @Override
    public synchronized long nextIdentity() {
        return ++ultimoId;
    }

    @Override
    public synchronized boolean store(PacienteDTO p) {
        for (PacienteDTO otro : pacientes) {
            if (otro.pacienteId() == p.pacienteId()) {
                continue;
            }
            boolean mismaPersona = otro.numeroAfiliado().equals(p.numeroAfiliado()) && otro.ci().equalsIgnoreCase(p.ci());
            boolean otroTitular = p.titular() && otro.titular() && otro.numeroAfiliado().equals(p.numeroAfiliado());
            if (mismaPersona || otroTitular) {
                return false;
            }
        }
        pacientes.removeIf(x -> x.pacienteId() == p.pacienteId());
        pacientes.add(p);
        ultimoId = Math.max(ultimoId, p.pacienteId());
        return true;
    }

    @Override
    public synchronized Optional<PacienteDTO> porId(long pacienteId) {
        return pacientes.stream().filter(p -> p.pacienteId() == pacienteId).findFirst();
    }

    @Override
    public synchronized Optional<PacienteDTO> porAfiliadoYCi(String numeroAfiliado, String ci) {
        return pacientes.stream()
                .filter(p -> p.numeroAfiliado().equals(numeroAfiliado) && p.ci().equalsIgnoreCase(ci))
                .findFirst();
    }

    @Override
    public synchronized List<PacienteDTO> deLaAfiliacion(String numeroAfiliado) {
        return pacientes.stream()
                .filter(p -> p.numeroAfiliado().equals(numeroAfiliado))
                .sorted(Comparator.comparing(PacienteDTO::titular).reversed().thenComparing(PacienteDTO::apellidos))
                .toList();
    }
}
