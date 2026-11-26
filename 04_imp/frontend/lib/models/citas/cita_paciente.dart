import '../catalogo/especialidad.dart';
import '../enfermeria/ficha_dia.dart';

/// Una cita del paciente (CU-08), como la devuelve `GET /api/v1/citas/mias` (`CitaDTO`).
class CitaPaciente {
  const CitaPaciente({
    required this.citaId,
    required this.numeroFicha,
    required this.inicio,
    required this.especialidad,
    required this.medico,
    required this.consultorio,
    required this.estado,
  });

  factory CitaPaciente.fromJson(Map<String, dynamic> json) {
    final fecha = json['fecha'] as String;
    final hora = json['hora'] as String;
    return CitaPaciente(
      citaId: (json['citaId'] as num).toInt(),
      numeroFicha: (json['numeroFicha'] as num).toInt(),
      inicio: DateTime.parse('${fecha}T${hora.length == 5 ? '$hora:00' : hora}'),
      especialidad: Especialidad.desdeCodigo(json['especialidad'] as String),
      medico: (json['nombreMedico'] as String?)?.trim() ?? '',
      consultorio: '${json['consultorio']}',
      estado: EstadoCita.desdeCodigo(json['estado'] as String),
    );
  }

  final int citaId;
  final int numeroFicha;

  /// Fecha y hora de la cita.
  final DateTime inicio;
  final Especialidad especialidad;
  final String medico;
  final String consultorio;
  final EstadoCita estado;

  /// Hasta esta hora enfermeria puede confirmar la llegada; despues la cita se cancela sola.
  DateTime get limiteConfirmacion => inicio.subtract(const Duration(minutes: minutosAntesParaConfirmar));

  /// Solo una cita PROGRAMADA se cancela o reprograma (CU-10 [1a], CU-11).
  bool get sePuedeModificar => estado == EstadoCita.programada;

  /// Programada o confirmada, de hoy en adelante.
  bool esProxima(DateTime ahora) {
    final hoy = DateTime(ahora.year, ahora.month, ahora.day);
    final vigente = estado == EstadoCita.programada || estado == EstadoCita.asistenciaConfirmada;
    return vigente && !inicio.isBefore(hoy);
  }

  CitaPaciente copiar({EstadoCita? estado}) => CitaPaciente(
        citaId: citaId,
        numeroFicha: numeroFicha,
        inicio: inicio,
        especialidad: especialidad,
        medico: medico,
        consultorio: consultorio,
        estado: estado ?? this.estado,
      );
}
