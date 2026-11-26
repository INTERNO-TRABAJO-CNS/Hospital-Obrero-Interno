import '../catalogo/especialidad.dart';
import '../enfermeria/ficha_dia.dart';
import 'agenda_medico.dart';

/// La receta tiene de 1 a 2000 caracteres (la regla la aplica el backend: RECETA_INVALIDA).
const int maxReceta = 2000;

/// `GET /api/v1/citas/{id}/atencion`: datos autocompletados de la atención.
class DatosAtencion {
  const DatosAtencion({
    required this.citaId,
    required this.pacienteId,
    required this.nombrePaciente,
    required this.numeroAfiliado,
    required this.numeroFicha,
    required this.fecha,
    required this.hora,
    required this.consulta,
    required this.consultorio,
    required this.medico,
    required this.puedeDerivar,
    this.estado,
    this.edad,
    this.titular,
    this.nombreConsultorio,
  });

  final int citaId;
  final int pacienteId;
  final String nombrePaciente;
  final String numeroAfiliado;
  final int numeroFicha;
  final String fecha;
  final String hora;

  /// Especialidad de la cita: básica (general, odontología, pediatría) o interna.
  final Especialidad consulta;
  final int consultorio;
  final String medico;
  final bool puedeDerivar;
  final EstadoCita? estado;

  /// null si el backend no conoce el dato.
  final int? edad;
  final bool? titular;
  final String? nombreConsultorio;

  DatosAtencion conEstado(EstadoCita nuevo) => DatosAtencion(
        citaId: citaId,
        pacienteId: pacienteId,
        nombrePaciente: nombrePaciente,
        numeroAfiliado: numeroAfiliado,
        numeroFicha: numeroFicha,
        fecha: fecha,
        hora: hora,
        consulta: consulta,
        consultorio: consultorio,
        medico: medico,
        puedeDerivar: puedeDerivar,
        estado: nuevo,
        edad: edad,
        titular: titular,
        nombreConsultorio: nombreConsultorio,
      );

  factory DatosAtencion.fromJson(Map<String, dynamic> json) {
    final estado = json['estado'] as String?;
    return DatosAtencion(
      citaId: (json['citaId'] as num).toInt(),
      pacienteId: (json['pacienteId'] as num).toInt(),
      nombrePaciente: json['nombrePaciente'] as String? ?? '',
      numeroAfiliado: json['numeroAfiliado'] as String? ?? '',
      numeroFicha: (json['numeroFicha'] as num).toInt(),
      fecha: json['fecha'] as String? ?? '',
      hora: json['hora'] as String? ?? '',
      consulta: Especialidad.desdeCodigo(json['consulta'] as String? ?? ''),
      consultorio: (json['consultorio'] as num?)?.toInt() ?? 0,
      medico: json['medico'] as String? ?? '',
      puedeDerivar: json['puedeDerivar'] as bool? ?? false,
      estado: estado == null ? null : estadoCitaDesdeCodigo(estado),
      edad: (json['edad'] as num?)?.toInt(),
      titular: json['titular'] as bool?,
      nombreConsultorio: json['nombreConsultorio'] as String?,
    );
  }
}
