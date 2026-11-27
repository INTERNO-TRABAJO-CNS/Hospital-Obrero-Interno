/// Fecha sin atencion (espejo de DiaNoHabilDTO del backend). Carga manual.
class DiaNoHabil {
  const DiaNoHabil({this.id = 0, required this.fecha, required this.descripcion, this.activo = true});

  factory DiaNoHabil.desdeJson(Map<String, dynamic> json) => DiaNoHabil(
        id: (json['diaNoHabilId'] as num?)?.toInt() ?? 0,
        fecha: DateTime.parse(json['fecha'] as String),
        descripcion: json['descripcion'] as String? ?? '',
        activo: json['activo'] as bool? ?? false,
      );

  final int id;

  /// Solo importa la fecha (sin hora).
  final DateTime fecha;
  final String descripcion;
  final bool activo;

  Map<String, dynamic> aJson() => {
        'diaNoHabilId': id,
        'fecha': fechaATexto(fecha),
        'descripcion': descripcion,
        'activo': activo,
      };

  DiaNoHabil copiar({int? id, bool? activo}) =>
      DiaNoHabil(id: id ?? this.id, fecha: fecha, descripcion: descripcion, activo: activo ?? this.activo);
}

/// DateTime -> "2026-12-25".
String fechaATexto(DateTime fecha) =>
    '${fecha.year.toString().padLeft(4, '0')}-${fecha.month.toString().padLeft(2, '0')}-'
    '${fecha.day.toString().padLeft(2, '0')}';
