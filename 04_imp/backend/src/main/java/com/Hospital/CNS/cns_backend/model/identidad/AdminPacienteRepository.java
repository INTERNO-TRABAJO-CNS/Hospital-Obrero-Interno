package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Consultas y cambios de pacientes que solo hace el administrador. Implementacion: PgAdminPacienteRepository. */
public interface AdminPacienteRepository {

    /** Todos los pacientes (activos y dados de baja), por apellidos y nombres. */
    List<PacienteAdminDTO> listar();

    Optional<PacienteAdminDTO> porId(long pacienteId);

    /** El afiliado con ese numero, este activo o no. */
    Optional<AfiliadoDTO> afiliadoPorNumero(String numeroAfiliado);

    /** Inserta o actualiza (por numero) el afiliado y lo deja activo. false si su CI ya es de otro afiliado. */
    boolean guardarAfiliado(AfiliadoDTO afiliado);

    /** Cambia nombres, apellidos y fecha del paciente (y el nombre que muestra su cuenta, si tiene). */
    void actualizarDatos(long pacienteId, String nombres, String apellidos, LocalDate fechaNacimiento);

    void cambiarActivo(long pacienteId, boolean activo);

    /** Citas del paciente que todavia no pasaron (PROGRAMADA o ASISTENCIA_CONFIRMADA). */
    int contarCitasPendientes(long pacienteId);
}
