import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/auth/paciente.dart';
import '../models/auth/sesion.dart';
import '../services/auth_service.dart';
import '../services/paciente_service.dart';

/// Controlador de la sesion (MVC): login, logout, registro del titular y del beneficiario.
///
/// Solo valida formato y llama a los services; las reglas las aplica el backend.
/// Cada accion devuelve `null` si salio bien o el mensaje para mostrar.
class SesionController extends ChangeNotifier {
  SesionController({required AuthService auth, required PacienteService pacientes, ApiClient? api})
      : _auth = auth,
        _pacientes = pacientes,
        _api = api;

  final AuthService _auth;
  final PacienteService _pacientes;

  /// Cliente compartido: al entrar se le pone el token de la sesion.
  final ApiClient? _api;

  Sesion? sesion;
  bool cargando = false;

  /// Afiliacion que quedo del registro del titular (por si el login no la trae).
  String? _afiliadoRegistrado;

  bool get autenticado => sesion != null;

  /// Afiliacion del titular que inicio sesion (para registrar beneficiarios): la manda el login
  /// para el rol PACIENTE o queda del registro.
  String? get numeroAfiliado => sesion?.numeroAfiliado ?? _afiliadoRegistrado;

  Future<String?> entrar(String usuario, String clave) async {
    if (usuario.trim().isEmpty || clave.isEmpty) {
      return 'Ingrese usuario y contraseña.';
    }
    return _ejecutar(() async {
      final nueva = await _auth.login(usuario, clave);
      sesion = nueva;
      _api?.token = nueva.token.isEmpty ? null : nueva.token;
    });
  }

  /// Crea la cuenta del titular y entra con ella.
  Future<String?> registrarTitular(RegistroTitular datos) async {
    if (datos.numeroAfiliado.trim().isEmpty || datos.ci.trim().isEmpty || datos.usuario.trim().isEmpty) {
      return 'Complete número de afiliado, CI y usuario.';
    }
    if (datos.clave.length < 6) {
      return 'La contraseña debe tener al menos 6 caracteres.';
    }
    final error = await _ejecutar(() async {
      final titular = await _pacientes.registrarTitular(datos);
      _afiliadoRegistrado = titular.numeroAfiliado;
    });
    if (error != null) {
      return error;
    }
    return entrar(datos.usuario, datos.clave);
  }

  Future<String?> registrarBeneficiario(RegistroBeneficiario datos) {
    if (datos.ci.trim().isEmpty || datos.nombres.trim().isEmpty || datos.apellidos.trim().isEmpty) {
      return Future.value('Complete CI, nombres y apellidos.');
    }
    if (datos.fechaNacimiento.isAfter(DateTime.now())) {
      return Future.value('La fecha de nacimiento no puede ser futura.');
    }
    return _ejecutar(() => _pacientes.registrarBeneficiario(datos));
  }

  /// Cierra la sesion en el backend y la olvida en la app. Si no hay conexion igual se olvida:
  /// el token vence solo.
  Future<void> salir() async {
    if (sesion != null) {
      try {
        await _auth.logout();
      } on ApiException {
        // Sin conexion o sesion ya vencida: no hay nada mas que hacer en el backend.
      }
    }
    olvidar();
  }

  /// Borra la sesion sin llamar al backend (cuando el backend ya respondio 401).
  void olvidar() {
    sesion = null;
    _afiliadoRegistrado = null;
    _api?.token = null;
    notifyListeners();
  }

  Future<String?> _ejecutar(Future<void> Function() accion) async {
    cargando = true;
    notifyListeners();
    try {
      await accion();
      return null;
    } on ApiException catch (e) {
      return e.mensaje;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }
}
