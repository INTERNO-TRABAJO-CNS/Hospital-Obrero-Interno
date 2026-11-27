package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** AdminPacienteRepository en memoria, sobre los Fakes de pacientes, afiliados y usuarios. */
public class FakeAdminPacienteRepository implements AdminPacienteRepository {

    private final FakePacienteRepository pacientes;
    private final FakeAfiliadoRepository afiliados;
    private final FakeUsuarioRepository usuarios;
    public final Set<Long> deBaja = new HashSet<>();
    public final Map<Long, Integer> citasPendientes = new HashMap<>();

    public FakeAdminPacienteRepository(FakePacienteRepository p, FakeAfiliadoRepository a, FakeUsuarioRepository u) {
        this.pacientes = p;
        this.afiliados = a;
        this.usuarios = u;
    }

    @Override
    public List<PacienteAdminDTO> listar() {
        return pacientes.pacientes.stream().map(this::fila)
                .sorted(Comparator.comparing(PacienteAdminDTO::apellidos)).toList();
    }

    @Override
    public Optional<PacienteAdminDTO> porId(long pacienteId) {
        return pacientes.porId(pacienteId).map(this::fila);
    }

    @Override
    public Optional<AfiliadoDTO> afiliadoPorNumero(String numeroAfiliado) {
        return Optional.ofNullable(afiliados.afiliados.get(numeroAfiliado));
    }

    @Override
    public boolean guardarAfiliado(AfiliadoDTO a) {
        boolean ciDeOtro = afiliados.afiliados.values().stream()
                .anyMatch(x -> !x.numeroAfiliado().equals(a.numeroAfiliado()) && x.ci().equalsIgnoreCase(a.ci()));
        if (ciDeOtro) {
            return false;
        }
        afiliados.con(new AfiliadoDTO(a.numeroAfiliado(), a.ci(), a.nombres(), a.apellidos(), a.fechaNacimiento(),
                true));
        return true;
    }

    @Override
    public void actualizarDatos(long pacienteId, String nombres, String apellidos, LocalDate fecha) {
        PacienteDTO p = pacientes.porId(pacienteId).orElseThrow();
        pacientes.store(new PacienteDTO(p.pacienteId(), p.numeroAfiliado(), p.ci(), nombres, apellidos, fecha,
                p.titular()));
    }

    @Override
    public void cambiarActivo(long pacienteId, boolean activo) {
        if (activo) {
            deBaja.remove(pacienteId);
        } else {
            deBaja.add(pacienteId);
        }
    }

    @Override
    public int contarCitasPendientes(long pacienteId) {
        return citasPendientes.getOrDefault(pacienteId, 0);
    }

    private PacienteAdminDTO fila(PacienteDTO p) {
        String usuario = usuarios.listar().stream()
                .filter(u -> u.rol() == RolUsuario.PACIENTE && u.personaId() == p.pacienteId())
                .map(UsuarioDTO::usuario).findFirst().orElse(null);
        return new PacienteAdminDTO(p.pacienteId(), p.numeroAfiliado(), p.ci(), p.nombres(), p.apellidos(),
                p.fechaNacimiento(), p.titular(), !deBaja.contains(p.pacienteId()), usuario);
    }
}
