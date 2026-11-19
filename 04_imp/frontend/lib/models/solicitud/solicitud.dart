import '../catalogo/especialidad.dart';

enum EstadoSolicitudApp {
  pendiente,
  enEspera,
  asignada,
  sinCupo;

  static EstadoSolicitudApp desdeJson(String valor) {
    switch (valor) {
      case 'PENDIENTE':
        return EstadoSolicitudApp.pendiente;
      case 'EN_ESPERA':
        return EstadoSolicitudApp.enEspera;
      case 'ASIGNADA':
        return EstadoSolicitudApp.asignada;
      case 'SIN_CUPO':
        return EstadoSolicitudApp.sinCupo;
      default:
        throw FormatException('Estado de solicitud desconocido: $valor');
    }
  }
}

class ResultadoSolicitud {
  const ResultadoSolicitud({
    required this.solicitudId,
    required this.estado,
    required this.mensaje,
    required this.especialidad,
    required this.diaSolicitado,
    required this.prioridad,
    required this.ordenLlegada,
    this.citaId,
  });

  final int solicitudId;
  final EstadoSolicitudApp estado;
  final String mensaje;
  final Especialidad especialidad;
  final DateTime diaSolicitado;
  final int prioridad;
  final int ordenLlegada;
  final int? citaId;

  factory ResultadoSolicitud.fromJson(Map<String, dynamic> json) {
    final codigoEspecialidad = json['especialidad'];

    if (codigoEspecialidad is! String) {
      throw const FormatException('Especialidad inválida');
    }

    final especialidad = Especialidad.values.where(
      (e) => e.codigo == codigoEspecialidad,
    );

    if (especialidad.isEmpty) {
      throw FormatException(
        'Especialidad desconocida: $codigoEspecialidad',
      );
    }

    final estado = json['estado'];

    if (estado is! String) {
      throw const FormatException('Estado de solicitud inválido');
    }

    return ResultadoSolicitud(
      solicitudId: json['solicitudId'] as int,
      estado: EstadoSolicitudApp.desdeJson(estado),
      mensaje: json['mensaje'] as String,
      especialidad: especialidad.first,
      diaSolicitado: DateTime.parse(json['diaSolicitado'] as String),
      prioridad: json['prioridad'] as int,
      ordenLlegada: json['ordenLlegada'] as int,
      citaId: json['citaId'] as int?,
    );
  }
}