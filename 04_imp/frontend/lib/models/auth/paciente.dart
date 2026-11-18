/// Paciente registrado (titular o beneficiario). Debe coincidir con `PacienteDTO` del backend.
class Paciente {
  const Paciente({
    required this.pacienteId,
    required this.numeroAfiliado,
    required this.ci,
    required this.nombres,
    required this.apellidos,
    required this.fechaNacimiento,
    required this.titular,
  });

  final int pacienteId;
  final String numeroAfiliado;
  final String ci;
  final String nombres;
  final String apellidos;
  final DateTime fechaNacimiento;
  final bool titular;

  String get nombreCompleto => '$nombres $apellidos'.trim();

  factory Paciente.fromJson(Map<String, dynamic> json) => Paciente(
        pacienteId: (json['pacienteId'] as num).toInt(),
        numeroAfiliado: json['numeroAfiliado'] as String,
        ci: json['ci'] as String,
        nombres: json['nombres'] as String,
        apellidos: json['apellidos'] as String,
        fechaNacimiento: DateTime.parse(json['fechaNacimiento'] as String),
        titular: json['titular'] as bool,
      );
}

/// Cuerpo de `POST /api/v1/pacientes/titular`: el nombre y la fecha de nacimiento
/// salen de la tabla interna de afiliados.
class RegistroTitular {
  const RegistroTitular({required this.numeroAfiliado, required this.ci, required this.usuario, required this.clave});

  final String numeroAfiliado;
  final String ci;
  final String usuario;
  final String clave;

  Map<String, dynamic> toJson() => {
        'numeroAfiliado': numeroAfiliado.trim(),
        'ci': ci.trim(),
        'usuario': usuario.trim(),
        'clave': clave,
      };
}

/// Cuerpo de `POST /api/v1/pacientes/beneficiario`.
class RegistroBeneficiario {
  const RegistroBeneficiario({
    required this.numeroAfiliadoTitular,
    required this.ci,
    required this.nombres,
    required this.apellidos,
    required this.fechaNacimiento,
  });

  final String numeroAfiliadoTitular;
  final String ci;
  final String nombres;
  final String apellidos;
  final DateTime fechaNacimiento;

  Map<String, dynamic> toJson() => {
        'numeroAfiliadoTitular': numeroAfiliadoTitular.trim(),
        'ci': ci.trim(),
        'nombres': nombres.trim(),
        'apellidos': apellidos.trim(),
        'fechaNacimiento': fechaIso(fechaNacimiento),
      };
}

/// 2018-10-05
String fechaIso(DateTime f) =>
    '${f.year.toString().padLeft(4, '0')}-${f.month.toString().padLeft(2, '0')}-${f.day.toString().padLeft(2, '0')}';
