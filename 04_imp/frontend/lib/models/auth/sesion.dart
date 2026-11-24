/// Roles del sistema. Debe coincidir con `RolUsuario` del backend.
enum Rol {
  paciente('PACIENTE', 'Paciente'),
  medico('MEDICO', 'Médico'),
  enfermeria('ENFERMERIA', 'Enfermería'),
  administrador('ADMINISTRADOR', 'Administrador');

  const Rol(this.codigo, this.etiqueta);

  final String codigo;

  /// Texto para la pildora de la barra superior ("Enfermería · María Flores").
  final String etiqueta;

  /// Un rol desconocido es un error de contrato: no se adivina (antes caia en PACIENTE y
  /// mostraba la pantalla equivocada).
  static Rol desdeCodigo(String codigo) =>
      values.firstWhere((r) => r.codigo == codigo, orElse: () => throw FormatException('rol desconocido "$codigo"'));
}

/// Resultado de `POST /api/v1/auth/login` (SesionDTO del backend). El [token] viaja en cada
/// peticion como `Authorization: Bearer <token>` hasta [venceEn].
class Sesion {
  const Sesion({
    required this.usuarioId,
    required this.rol,
    required this.personaId,
    required this.nombre,
    this.token = '',
    this.venceEn,
    this.numeroAfiliado,
  });

  final int usuarioId;
  final Rol rol;

  /// pacienteId para el rol PACIENTE; medicoId para MEDICO.
  final int personaId;
  final String nombre;

  /// Token opaco de la sesion. Vacio solo en datos de prueba.
  final String token;
  final DateTime? venceEn;

  /// Solo para el rol PACIENTE: afiliacion con la que se registran sus beneficiarios.
  final String? numeroAfiliado;

  /// Primer nombre para el saludo.
  String get primerNombre {
    final partes = nombre.trim().split(RegExp(r'\s+'));
    return partes.isEmpty || partes.first.isEmpty ? '' : partes.first;
  }

  /// "Enfermería · María Flores", como la pildora del Figma.
  String get etiqueta => nombre.trim().isEmpty ? rol.etiqueta : '${rol.etiqueta} · ${nombre.trim()}';

  factory Sesion.fromJson(Map<String, dynamic> json) {
    final token = json['token'] as String?;
    if (token == null || token.isEmpty) {
      throw const FormatException('la sesión no trae token');
    }
    return Sesion(
      usuarioId: (json['usuarioId'] as num).toInt(),
      rol: Rol.desdeCodigo(json['rol'] as String? ?? ''),
      personaId: (json['personaId'] as num).toInt(),
      nombre: (json['nombre'] as String?) ?? '',
      token: token,
      venceEn: DateTime.tryParse(json['venceEn'] as String? ?? ''),
      numeroAfiliado: json['numeroAfiliado'] as String?,
    );
  }
}
