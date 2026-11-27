import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/personal/paciente_admin.dart';
import '../services/pacientes_admin_service.dart';
import 'aviso_seguro.dart';

/// Pacientes del administrador: lista con búsqueda, alta, edición, baja y reactivación.
class PacientesAdminController extends ChangeNotifier with AvisoSeguro {
  PacientesAdminController(this._servicio);

  final PacientesAdminService _servicio;

  List<PacienteAdmin> pacientes = const [];
  bool cargando = false;
  String? errorCarga;

  /// Texto de búsqueda: nombre, apellido, CI o número de afiliado.
  String busqueda = '';

  List<PacienteAdmin> get filtrados {
    final q = busqueda.trim().toLowerCase();
    if (q.isEmpty) {
      return pacientes;
    }
    return [
      for (final p in pacientes)
        if (p.nombreCompleto.toLowerCase().contains(q) ||
            p.ci.toLowerCase().contains(q) ||
            p.numeroAfiliado.toLowerCase().contains(q))
          p,
    ];
  }

  void buscar(String texto) {
    busqueda = texto;
    notificar();
  }

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      pacientes = await _servicio.listar();
      errorCarga = null;
    } on ApiException catch (e) {
      errorCarga = mensajePaciente(e);
    } finally {
      cargando = false;
      notificar();
    }
  }

  /// `null` si salió bien o el mensaje para mostrar.
  Future<String?> guardar(DatosPacienteAdmin datos, {int? pacienteId}) {
    final invalido = datos.validar(nuevo: pacienteId == null);
    if (invalido != null) {
      return Future.value(invalido);
    }
    return _ejecutar(() => pacienteId == null ? _servicio.crear(datos) : _servicio.modificar(pacienteId, datos));
  }

  Future<String?> darDeBaja(int pacienteId) => _ejecutar(() => _servicio.darDeBaja(pacienteId));

  Future<String?> reactivar(int pacienteId) => _ejecutar(() => _servicio.reactivar(pacienteId));

  Future<String?> _ejecutar(Future<void> Function() accion) async {
    try {
      await accion();
      pacientes = await _servicio.listar();
      return null;
    } on ApiException catch (e) {
      return mensajePaciente(e);
    } finally {
      notificar();
    }
  }
}

/// Texto para el usuario según el código del backend.
String mensajePaciente(ApiException e) {
  switch (e.codigo) {
    case 'YA_REGISTRADO':
      return 'Ese paciente ya está registrado (misma afiliación y CI, o la afiliación ya tiene titular).';
    case 'USUARIO_DUPLICADO':
      return 'Ese nombre de usuario ya está en uso.';
    case 'AFILIADO_NO_ENCONTRADO':
      return 'Esa afiliación no tiene un titular activo. Registre primero al titular.';
    case 'CAMBIO_AFECTA_CITAS':
      return 'El paciente tiene citas programadas: no se puede dar de baja.';
    case 'ACCESO_DENEGADO':
      return 'Solo el administrador gestiona los pacientes.';
    case 'SIN_CONEXION':
    case 'SIN_RESPUESTA':
      return 'No se pudo conectar con el servidor.';
    default:
      return e.mensaje;
  }
}
