import '../catalogo/especialidad.dart';

/// Nombre de una especialidad a partir de su código ("CONSULTORIO_FAMILIAR" -> "Consultorio Familiar").
/// Un código desconocido se muestra tal cual (es solo texto en pantalla).
String nombreEspecialidad(String? codigo) {
  if (codigo == null || codigo.isEmpty) {
    return '';
  }
  for (final e in Especialidad.values) {
    if (e.codigo == codigo) {
      return e.nombre;
    }
  }
  return codigo;
}

class Receta {
  const Receta({required this.citaId, required this.medicacion, required this.fechaHora});

  final int citaId;
  final String medicacion;
  final String fechaHora;

  factory Receta.fromJson(Map<String, dynamic> json) => Receta(
        citaId: (json['citaId'] as num).toInt(),
        medicacion: json['medicacion'] as String? ?? '',
        fechaHora: json['fechaHora'] as String? ?? '',
      );
}

/// Una atención pasada con sus recetas.
class AtencionPasada {
  const AtencionPasada({
    required this.fecha,
    required this.hora,
    required this.especialidad,
    required this.medico,
    required this.recetas,
  });

  final String fecha;
  final String hora;
  final String especialidad;
  final String medico;
  final List<Receta> recetas;

  factory AtencionPasada.fromJson(Map<String, dynamic> json) => AtencionPasada(
        fecha: json['fecha'] as String? ?? '',
        hora: json['hora'] as String? ?? '',
        especialidad: json['especialidad'] as String? ?? '',
        medico: json['medico'] as String? ?? '',
        recetas: [
          for (final r in (json['recetas'] as List? ?? const [])) Receta.fromJson(Map<String, dynamic>.from(r as Map))
        ],
      );
}

class CitaPasada {
  const CitaPasada({
    required this.citaId,
    required this.fecha,
    required this.hora,
    required this.especialidad,
    required this.medico,
    required this.numeroFicha,
    required this.estado,
  });

  final int citaId;
  final String fecha;
  final String hora;
  final String especialidad;
  final String medico;
  final int numeroFicha;
  final String estado;

  factory CitaPasada.fromJson(Map<String, dynamic> json) => CitaPasada(
        citaId: (json['citaId'] as num).toInt(),
        fecha: json['fecha'] as String? ?? '',
        hora: json['hora'] as String? ?? '',
        especialidad: json['especialidad'] as String? ?? '',
        medico: json['medico'] as String? ?? '',
        numeroFicha: (json['numeroFicha'] as num?)?.toInt() ?? 0,
        estado: json['estado'] as String? ?? '',
      );
}

/// `GET /api/v1/medico/pacientes/{id}/historial` (solo pacientes de la agenda del médico).
class HistorialPaciente {
  const HistorialPaciente({
    required this.pacienteId,
    required this.nombrePaciente,
    required this.citasPasadas,
    required this.atenciones,
    this.numeroAfiliado,
    this.titular,
    this.edad,
  });

  final int pacienteId;
  final String nombrePaciente;
  final List<CitaPasada> citasPasadas;
  final List<AtencionPasada> atenciones;
  final String? numeroAfiliado;
  final bool? titular;
  final int? edad;

  int get totalRecetas => atenciones.fold(0, (n, a) => n + a.recetas.length);

  factory HistorialPaciente.fromJson(Map<String, dynamic> json) => HistorialPaciente(
        pacienteId: (json['pacienteId'] as num).toInt(),
        nombrePaciente: json['nombrePaciente'] as String? ?? '',
        citasPasadas: [
          for (final c in (json['citasPasadas'] as List? ?? const []))
            CitaPasada.fromJson(Map<String, dynamic>.from(c as Map))
        ],
        atenciones: [
          for (final a in (json['atenciones'] as List? ?? const []))
            AtencionPasada.fromJson(Map<String, dynamic>.from(a as Map))
        ],
        numeroAfiliado: json['numeroAfiliado'] as String?,
        titular: json['titular'] as bool?,
        edad: (json['edad'] as num?)?.toInt(),
      );
}

/// `GET /api/v1/medico/pacientes?afiliado=`: buscador del historial.
class PacienteDeAgenda {
  const PacienteDeAgenda({
    required this.pacienteId,
    required this.nombre,
    required this.numeroAfiliado,
    required this.titular,
    required this.edad,
  });

  final int pacienteId;
  final String nombre;
  final String numeroAfiliado;
  final bool titular;
  final int edad;

  factory PacienteDeAgenda.fromJson(Map<String, dynamic> json) => PacienteDeAgenda(
        pacienteId: (json['pacienteId'] as num).toInt(),
        nombre: json['nombre'] as String? ?? '',
        numeroAfiliado: json['numeroAfiliado'] as String? ?? '',
        titular: json['titular'] as bool? ?? false,
        edad: (json['edad'] as num?)?.toInt() ?? 0,
      );
}
