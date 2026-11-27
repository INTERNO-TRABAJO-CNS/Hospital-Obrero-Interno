import '../enfermeria/ficha_dia.dart';

/// Nombre legible de cada estado de cita ("Asistencia confirmada").
String nombreEstadoCita(String codigo) => switch (codigo) {
      'PROGRAMADA' => 'Programada',
      'ASISTENCIA_CONFIRMADA' => 'Asistencia confirmada',
      'ATENDIDA' => 'Atendida',
      'CANCELADA' => 'Cancelada',
      _ => codigo,
    };

EstadoCita estadoCitaDesdeCodigo(String codigo) => EstadoCita.values
    .firstWhere((e) => e.codigo == codigo, orElse: () => throw FormatException('estado de cita desconocido "$codigo"'));

/// Una fila de `GET /api/v1/medico/agenda`.
class FichaAgenda {
  const FichaAgenda({
    required this.citaId,
    required this.numeroFicha,
    required this.hora,
    required this.nombrePaciente,
    required this.estado,
    required this.pacienteId,
  });

  final int citaId;
  final int numeroFicha;

  /// "08:40:00".
  final String hora;
  final String nombrePaciente;
  final EstadoCita estado;
  final int pacienteId;

  factory FichaAgenda.fromJson(Map<String, dynamic> json) => FichaAgenda(
        citaId: (json['citaId'] as num).toInt(),
        numeroFicha: (json['numeroFicha'] as num).toInt(),
        hora: json['hora'] as String? ?? '',
        nombrePaciente: json['nombrePaciente'] as String? ?? '',
        estado: estadoCitaDesdeCodigo(json['estado'] as String? ?? ''),
        pacienteId: (json['pacienteId'] as num).toInt(),
      );
}

/// Agenda de un día. [cantidad] cuenta también las canceladas (decisión del 10/11).
class AgendaDia {
  const AgendaDia({required this.fecha, required this.cantidad, required this.fichas});

  /// "2026-11-12".
  final String fecha;
  final int cantidad;
  final List<FichaAgenda> fichas;

  factory AgendaDia.fromJson(Map<String, dynamic> json) => AgendaDia(
        fecha: json['fecha'] as String? ?? '',
        cantidad: (json['cantidad'] as num?)?.toInt() ?? 0,
        fichas: [
          for (final f in (json['fichas'] as List? ?? const []))
            FichaAgenda.fromJson(Map<String, dynamic>.from(f as Map))
        ],
      );
}
