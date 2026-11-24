import '../catalogo/especialidad.dart';

// Respuestas del backend de supervision (CU-19): GET /api/v1/admin/supervision y .../medicos/{id}/fichas.

/// Un medico del resumen por especialidad (ResumenEspecialidadDTO).
class ResumenMedicoDia {
  const ResumenMedicoDia({
    required this.especialidad,
    required this.medicoId,
    required this.nombreMedico,
    this.horaInicio,
    this.horaFin,
    required this.cupoMaximo,
    required this.fichas,
    required this.programadas,
    required this.confirmadas,
    required this.atendidas,
    required this.canceladas,
  });

  factory ResumenMedicoDia.desdeJson(Map<String, dynamic> json) => ResumenMedicoDia(
        especialidad: Especialidad.desdeCodigo(json['especialidad'] as String),
        medicoId: (json['medicoId'] as num).toInt(),
        nombreMedico: json['nombreMedico'] as String? ?? '',
        horaInicio: _hora(json['horaInicio']),
        horaFin: _hora(json['horaFin']),
        cupoMaximo: (json['cupoMaximo'] as num?)?.toInt() ?? 0,
        fichas: (json['fichas'] as num?)?.toInt() ?? 0,
        programadas: (json['programadas'] as num?)?.toInt() ?? 0,
        confirmadas: (json['confirmadas'] as num?)?.toInt() ?? 0,
        atendidas: (json['atendidas'] as num?)?.toInt() ?? 0,
        canceladas: (json['canceladas'] as num?)?.toInt() ?? 0,
      );

  final Especialidad especialidad;
  final int medicoId;
  final String nombreMedico;

  /// "08:00"; `null` si ese dia no tiene horario.
  final String? horaInicio;
  final String? horaFin;
  final int cupoMaximo;

  /// Fichas que ocupan cupo (todas menos las canceladas).
  final int fichas;
  final int programadas;
  final int confirmadas;
  final int atendidas;
  final int canceladas;

  /// Todas las fichas del dia, incluidas las canceladas.
  int get total => fichas + canceladas;
}

/// Una ficha del dia de un medico (FichaSupervisionDTO).
class FichaSupervision {
  const FichaSupervision({
    required this.citaId,
    required this.numeroFicha,
    required this.hora,
    required this.estado,
    required this.consultorio,
    required this.paciente,
  });

  factory FichaSupervision.desdeJson(Map<String, dynamic> json) => FichaSupervision(
        citaId: (json['citaId'] as num).toInt(),
        numeroFicha: (json['numeroFicha'] as num).toInt(),
        hora: _hora(json['hora']) ?? '',
        estado: json['estado'] as String,
        consultorio: (json['consultorio'] as num?)?.toInt() ?? 0,
        paciente: json['nombrePaciente'] as String? ?? '',
      );

  final int citaId;
  final int numeroFicha;

  /// "09:30".
  final String hora;

  /// PROGRAMADA, ASISTENCIA_CONFIRMADA, ATENDIDA o CANCELADA.
  final String estado;
  final int consultorio;
  final String paciente;
}

/// "08:00:00" o "08:00" -> "08:00".
String? _hora(Object? valor) {
  if (valor is! String || valor.isEmpty) {
    return null;
  }
  return valor.length >= 5 ? valor.substring(0, 5) : valor;
}
