import 'dart:convert';

import '../comun/fechas_iso.dart';
import 'cuenta.dart';

/// Paciente tal como lo ve el administrador en Catálogos / Gestionar > Pacientes (PacienteAdminDTO).
/// [usuario] es la cuenta del titular para entrar a la app, o null si no tiene (los beneficiarios nunca tienen).
class PacienteAdmin {
  const PacienteAdmin({
    required this.pacienteId,
    required this.numeroAfiliado,
    required this.ci,
    required this.nombres,
    required this.apellidos,
    required this.fechaNacimiento,
    required this.titular,
    required this.activo,
    this.usuario,
  });

  factory PacienteAdmin.desdeJson(Map<String, dynamic> json) => PacienteAdmin(
        pacienteId: (json['pacienteId'] as num).toInt(),
        numeroAfiliado: json['numeroAfiliado'] as String? ?? '',
        ci: json['ci'] as String? ?? '',
        nombres: json['nombres'] as String? ?? '',
        apellidos: json['apellidos'] as String? ?? '',
        fechaNacimiento: DateTime.parse(json['fechaNacimiento'] as String),
        titular: json['titular'] as bool? ?? false,
        activo: json['activo'] as bool? ?? true,
        usuario: json['usuario'] as String?,
      );

  final int pacienteId;
  final String numeroAfiliado;
  final String ci;
  final String nombres;
  final String apellidos;
  final DateTime fechaNacimiento;
  final bool titular;
  final bool activo;
  final String? usuario;

  String get nombreCompleto => '$nombres $apellidos'.trim();

  /// Edad en años cumplidos a la fecha [hoy].
  int edad(DateTime hoy) {
    var anios = hoy.year - fechaNacimiento.year;
    if (hoy.month < fechaNacimiento.month || (hoy.month == fechaNacimiento.month && hoy.day < fechaNacimiento.day)) {
      anios--;
    }
    return anios;
  }

  PacienteAdmin copiar({String? nombres, String? apellidos, DateTime? fechaNacimiento, bool? activo}) => PacienteAdmin(
        pacienteId: pacienteId,
        numeroAfiliado: numeroAfiliado,
        ci: ci,
        nombres: nombres ?? this.nombres,
        apellidos: apellidos ?? this.apellidos,
        fechaNacimiento: fechaNacimiento ?? this.fechaNacimiento,
        titular: titular,
        activo: activo ?? this.activo,
        usuario: usuario,
      );
}

/// Datos del formulario "Nuevo / editar paciente". Al crear un titular, si [usuario] tiene texto también se crea
/// su cuenta con [clave]. Al editar solo cambian nombres, apellidos y fecha de nacimiento.
class DatosPacienteAdmin {
  const DatosPacienteAdmin({
    required this.titular,
    required this.numeroAfiliado,
    required this.ci,
    required this.nombres,
    required this.apellidos,
    required this.fechaNacimiento,
    this.usuario = '',
    this.clave = '',
  });

  final bool titular;
  final String numeroAfiliado;
  final String ci;
  final String nombres;
  final String apellidos;
  final DateTime? fechaNacimiento;
  final String usuario;
  final String clave;

  bool get conCuenta => titular && usuario.trim().isNotEmpty;

  Map<String, dynamic> aJson() => {
        'titular': titular,
        'numeroAfiliado': numeroAfiliado.trim(),
        'ci': ci.trim(),
        'nombres': nombres.trim(),
        'apellidos': apellidos.trim(),
        'fechaNacimiento': fechaNacimiento == null ? null : isoDe(fechaNacimiento!),
        'usuario': conCuenta ? usuario.trim() : null,
        'clave': conCuenta ? clave : null,
      };

  /// Mismas reglas que AdminPacienteService; `null` si todo está bien. [nuevo]: también revisa afiliación, CI y
  /// cuenta (al editar no cambian).
  String? validar({required bool nuevo, DateTime? hoy}) {
    if (nuevo && (numeroAfiliado.trim().isEmpty || ci.trim().isEmpty)) {
      return 'Ingrese el número de afiliado y el CI.';
    }
    if (nombres.trim().isEmpty || apellidos.trim().isEmpty) {
      return 'Ingrese nombres y apellidos.';
    }
    final fecha = fechaNacimiento;
    final ahora = hoy ?? DateTime.now();
    if (fecha == null) {
      return 'Elija la fecha de nacimiento.';
    }
    if (fecha.isAfter(DateTime(ahora.year, ahora.month, ahora.day))) {
      return 'La fecha de nacimiento no puede ser futura.';
    }
    if (nuevo && conCuenta) {
      if (usuario.trim().length > usuarioMaximo) {
        return 'El usuario no puede pasar de $usuarioMaximo caracteres.';
      }
      if (clave.length < claveMinima) {
        return 'La contraseña debe tener al menos $claveMinima caracteres.';
      }
      if (utf8.encode(clave).length > claveMaximaBytes) {
        return 'La contraseña es demasiado larga (máximo $claveMaximaBytes bytes).';
      }
    }
    return null;
  }
}
