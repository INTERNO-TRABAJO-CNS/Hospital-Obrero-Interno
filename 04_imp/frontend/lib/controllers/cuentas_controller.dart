import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/personal/cuenta.dart';
import '../services/cuentas_service.dart';
import 'aviso_seguro.dart';

/// Cuentas del personal (medicos, enfermeria y administradores).
class CuentasController extends ChangeNotifier with AvisoSeguro {
  CuentasController(this._servicio);

  final CuentasService _servicio;

  List<Cuenta> cuentas = const [];
  bool cargando = false;
  String? errorCarga;

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      cuentas = await _servicio.listar();
      errorCarga = null;
    } on ApiException catch (e) {
      errorCarga = mensajeCuenta(e);
    } finally {
      cargando = false;
      notificar();
    }
  }

  /// Valida el formato y crea la cuenta; `null` si salio bien o el mensaje para mostrar.
  Future<String?> crear(NuevaCuenta datos) async {
    final invalido = datos.validar();
    if (invalido != null) {
      return invalido;
    }
    return _ejecutar(() => _servicio.crear(datos));
  }

  Future<String?> darDeBaja(int usuarioId) => _ejecutar(() => _servicio.darDeBaja(usuarioId));

  Future<String?> _ejecutar(Future<void> Function() accion) async {
    try {
      await accion();
      cuentas = await _servicio.listar();
      return null;
    } on ApiException catch (e) {
      return mensajeCuenta(e);
    } finally {
      notificar();
    }
  }
}

/// Texto para el usuario segun el codigo del backend.
String mensajeCuenta(ApiException e) {
  switch (e.codigo) {
    case 'USUARIO_DUPLICADO':
      return 'Ese nombre de usuario ya está en uso.';
    case 'NO_PERMITIDO':
      return 'No puede dar de baja su propia cuenta.';
    case 'ACCESO_DENEGADO':
      return 'Solo el administrador gestiona las cuentas del personal.';
    case 'SIN_CONEXION':
    case 'SIN_RESPUESTA':
      return 'No se pudo conectar con el servidor.';
    default:
      return e.mensaje;
  }
}
