/// Consultorio del catalogo (espejo de ConsultorioDTO del backend).
class Consultorio {
  const Consultorio({this.id = 0, required this.nombre, this.activo = true});

  factory Consultorio.desdeJson(Map<String, dynamic> json) => Consultorio(
        id: (json['consultorioId'] as num?)?.toInt() ?? 0,
        nombre: json['nombre'] as String? ?? '',
        activo: json['activo'] as bool? ?? false,
      );

  final int id;
  final String nombre;
  final bool activo;

  Map<String, dynamic> aJson() => {'consultorioId': id, 'nombre': nombre, 'activo': activo};

  Consultorio copiar({int? id, String? nombre, bool? activo}) =>
      Consultorio(id: id ?? this.id, nombre: nombre ?? this.nombre, activo: activo ?? this.activo);
}
