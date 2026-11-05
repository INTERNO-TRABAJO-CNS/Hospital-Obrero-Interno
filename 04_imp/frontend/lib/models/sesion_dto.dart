class SesionDto {
  const SesionDto({
    required this.usuarioId,
    required this.nombreUsuario,
    required this.rol,
    required this.permisos,
  });

  final int usuarioId;
  final String nombreUsuario;
  final String rol;
  final List<String> permisos;

  factory SesionDto.fromJson(Map<String, dynamic> json) {
    return SesionDto(
      usuarioId: (json['usuarioId'] as num?)?.toInt() ?? 0,
      nombreUsuario: json['nombreUsuario'] as String? ?? '',
      rol: json['rol'] as String? ?? '',
      permisos: (json['permisos'] as List<dynamic>? ?? const [])
          .map((permiso) => permiso.toString())
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'usuarioId': usuarioId,
      'nombreUsuario': nombreUsuario,
      'rol': rol,
      'permisos': permisos,
    };
  }
}