import '../catalogo/especialidad.dart';

/// Estados de una cita comun. Debe coincidir con `EstadoCita` del backend.
/// PROGRAMADA -> ASISTENCIA_CONFIRMADA -> ATENDIDA, o CANCELADA.
enum EstadoCita {
  programada('PROGRAMADA'),
  asistenciaConfirmada('ASISTENCIA_CONFIRMADA'),
  atendida('ATENDIDA'),
  cancelada('CANCELADA');

  const EstadoCita(this.codigo);

  final String codigo;
}

/// Enfermeria confirma la llegada hasta 30 minutos antes de la cita (texto del Figma
/// "ENFERMERIA CONFIRMAR ASISTENCIA"); pasado ese tiempo la cita se cancela sola.
const int minutosAntesParaConfirmar = 30;

/// Una ficha (cita comun) del dia, como la ve enfermeria.
class FichaDelDia {
  const FichaDelDia({
    required this.citaId,
    required this.numeroFicha,
    required this.inicio,
    required this.paciente,
    required this.numeroAfiliado,
    required this.especialidad,
    required this.medico,
    required this.consultorio,
    required this.estado,
  });

  final int citaId;
  final int numeroFicha;

  /// Fecha y hora de la cita.
  final DateTime inicio;
  final String paciente;
  final String numeroAfiliado;
  final Especialidad especialidad;

  /// "Dr. Ramos".
  final String medico;

  /// "4".
  final String consultorio;
  final EstadoCita estado;

  /// Ultimo momento para confirmar la llegada.
  DateTime get limiteConfirmacion => inicio.subtract(const Duration(minutes: minutosAntesParaConfirmar));

  FichaDelDia copiar({EstadoCita? estado}) => FichaDelDia(
        citaId: citaId,
        numeroFicha: numeroFicha,
        inicio: inicio,
        paciente: paciente,
        numeroAfiliado: numeroAfiliado,
        especialidad: especialidad,
        medico: medico,
        consultorio: consultorio,
        estado: estado ?? this.estado,
      );
}
