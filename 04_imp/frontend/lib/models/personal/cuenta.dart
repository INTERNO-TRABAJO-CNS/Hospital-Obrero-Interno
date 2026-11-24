import 'dart:convert';

import '../auth/sesion.dart';

/// Reglas de formato de las cuentas (las mismas de `ReglasCuenta` del backend).
const int claveMinima = 6;

/// BCrypt solo usa los primeros 72 bytes de la contrasena.
const int claveMaximaBytes = 72;
const int usuarioMaximo = 60;
const int nombreMaximo = 200;

/// Cuenta del personal tal como la ve el administrador (CuentaDTO: nunca trae la contrasena).
class Cuenta {
  const Cuenta({
    required this.usuarioId,
    required this.usuario,
    required this.rol,
    required this.personaId,
    required this.nombre,
    required this.activo,
  });

  factory Cuenta.desdeJson(Map<String, dynamic> json) => Cuenta(
        usuarioId: (json['usuarioId'] as num).toInt(),
        usuario: json['usuario'] as String? ?? '',
        rol: Rol.desdeCodigo(json['rol'] as String? ?? ''),
        personaId: (json['personaId'] as num?)?.toInt() ?? 0,
        nombre: json['nombre'] as String? ?? '',
        activo: json['activo'] as bool? ?? false,
      );

  final int usuarioId;
  final String usuario;
  final Rol rol;
  final int personaId;
  final String nombre;
  final bool activo;

  Cuenta copiar({bool? activo}) => Cuenta(
        usuarioId: usuarioId,
        usuario: usuario,
        rol: rol,
        personaId: personaId,
        nombre: nombre,
        activo: activo ?? this.activo,
      );
}

/// Cuerpo de `POST /api/v1/admin/usuarios` (NuevaCuentaDTO). Para MEDICO, [personaId] es el
/// medicoId y el nombre puede ir vacio (lo toma del catalogo).
class NuevaCuenta {
  const NuevaCuenta({
    required this.usuario,
    required this.clave,
    required this.rol,
    this.personaId,
    this.nombre = '',
  });

  final String usuario;
  final String clave;
  final Rol rol;
  final int? personaId;
  final String nombre;

  Map<String, dynamic> aJson() => {
        'usuario': usuario.trim(),
        'clave': clave,
        'rol': rol.codigo,
        'personaId': personaId,
        'nombre': nombre.trim(),
      };

  /// Validacion de formato antes de llamar al backend; `null` si esta bien.
  String? validar() {
    if (usuario.trim().isEmpty) {
      return 'Ingrese el nombre de usuario.';
    }
    if (usuario.trim().length > usuarioMaximo) {
      return 'El usuario no puede pasar de $usuarioMaximo caracteres.';
    }
    if (rol == Rol.paciente) {
      return 'Las cuentas de pacientes se crean desde el registro del titular.';
    }
    if (rol == Rol.medico && personaId == null) {
      return 'Elija el médico del catálogo.';
    }
    if (rol != Rol.medico && nombre.trim().isEmpty) {
      return 'Ingrese el nombre completo.';
    }
    if (nombre.trim().length > nombreMaximo) {
      return 'El nombre no puede pasar de $nombreMaximo caracteres.';
    }
    if (clave.length < claveMinima) {
      return 'La contraseña debe tener al menos $claveMinima caracteres.';
    }
    if (utf8.encode(clave).length > claveMaximaBytes) {
      return 'La contraseña es demasiado larga (máximo $claveMaximaBytes bytes).';
    }
    return null;
  }
}
