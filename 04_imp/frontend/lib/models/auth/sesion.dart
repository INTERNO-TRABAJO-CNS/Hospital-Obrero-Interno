/// Roles del sistema. Debe coincidir con `RolUsuario` del backend.
enum Rol {
  paciente('PACIENTE'),
  medico('MEDICO'),
  enfermeria('ENFERMERIA'),
  administrador('ADMINISTRADOR');

  const Rol(this.codigo);

  final String codigo;

  static Rol desdeCodigo(String codigo) =>
      values.firstWhere((r) => r.codigo == codigo, orElse: () => paciente);
}

/// Resultado de `POST /api/v1/auth/login`. Mientras no exista Spring Security (S3-08)
/// estos datos viajan en los headers X-Usuario-Id, X-Rol y X-Persona-Id.
class Sesion {
  const Sesion({required this.usuarioId, required this.rol, required this.personaId, required this.nombre});

  final int usuarioId;
  final Rol rol;

  /// pacienteId para el rol PACIENTE.
  final int personaId;
  final String nombre;

  /// Primer nombre para el saludo.
  String get primerNombre {
    final partes = nombre.trim().split(RegExp(r'\s+'));
    return partes.isEmpty || partes.first.isEmpty ? '' : partes.first;
  }

  factory Sesion.fromJson(Map<String, dynamic> json) => Sesion(
        usuarioId: (json['usuarioId'] as num).toInt(),
        rol: Rol.desdeCodigo(json['rol'] as String),
        personaId: (json['personaId'] as num).toInt(),
        nombre: (json['nombre'] as String?) ?? '',
      );
}
