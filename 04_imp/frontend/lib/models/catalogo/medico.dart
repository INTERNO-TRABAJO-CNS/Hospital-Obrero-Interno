import 'especialidad.dart';

/// Cupo diario permitido por medico (regla del hospital).
const int cupoMinimo = 5;
const int cupoMaximo = 7;

/// Medico del catalogo (espejo de MedicoDTO del backend).
class Medico {
  const Medico({
    this.id = 0,
    required this.nombres,
    required this.apellidos,
    required this.especialidad,
    required this.duracionCitaMin,
    required this.cupoMaximoDia,
    this.activo = true,
  });

  factory Medico.desdeJson(Map<String, dynamic> json) => Medico(
        id: (json['medicoId'] as num?)?.toInt() ?? 0,
        nombres: json['nombres'] as String? ?? '',
        apellidos: json['apellidos'] as String? ?? '',
        especialidad: Especialidad.desdeCodigo(json['especialidad'] as String? ?? ''),
        duracionCitaMin: (json['duracionCitaMin'] as num?)?.toInt() ?? 0,
        cupoMaximoDia: (json['cupoMaximoDia'] as num?)?.toInt() ?? 0,
        activo: json['activo'] as bool? ?? false,
      );

  /// 0 mientras no esta guardado.
  final int id;
  final String nombres;
  final String apellidos;
  final Especialidad especialidad;
  final int duracionCitaMin;
  final int cupoMaximoDia;
  final bool activo;

  String get nombreCompleto => '$nombres $apellidos'.trim();

  Map<String, dynamic> aJson() => {
        'medicoId': id,
        'nombres': nombres,
        'apellidos': apellidos,
        'especialidad': especialidad.codigo,
        'duracionCitaMin': duracionCitaMin,
        'cupoMaximoDia': cupoMaximoDia,
        'activo': activo,
      };

  Medico copiar({int? id, bool? activo}) => Medico(
        id: id ?? this.id,
        nombres: nombres,
        apellidos: apellidos,
        especialidad: especialidad,
        duracionCitaMin: duracionCitaMin,
        cupoMaximoDia: cupoMaximoDia,
        activo: activo ?? this.activo,
      );
}
