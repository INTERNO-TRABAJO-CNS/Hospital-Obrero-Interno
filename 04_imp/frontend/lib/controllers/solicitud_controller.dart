import 'package:flutter/material.dart';

import '../core/api_client.dart';
import '../models/auth/paciente.dart';
import '../models/catalogo/especialidad.dart';
import '../models/medico/ficha_interna.dart' show edadPediatria;
import '../models/solicitud/solicitud.dart';
import '../services/paciente_service.dart';
import '../services/solicitud_service.dart';

/// Edad cumplida en [dia].
int edadEn(DateTime nacimiento, DateTime dia) {
  var edad = dia.year - nacimiento.year;
  if (dia.month < nacimiento.month || (dia.month == nacimiento.month && dia.day < nacimiento.day)) {
    edad--;
  }
  return edad;
}

/// CU-04: solicitar cita (especialidad + dia) para el titular o uno de sus beneficiarios.
class SolicitudController extends ChangeNotifier {
  SolicitudController({required SolicitudService service, PacienteService? pacientes})
      : _service = service,
        _pacientes = pacientes;

  final SolicitudService _service;
  final PacienteService? _pacientes;

  bool cargando = false;
  String? error;
  ResultadoSolicitud? resultado;

  List<ResultadoSolicitud> solicitudes = [];

  /// Dias habiles que se pueden elegir (hoy + 15 dias habiles, sin feriados).
  List<DateTime> dias = [];

  /// El titular y sus beneficiarios (RN-05). Vacio si no se pudo cargar: se pide para el titular.
  List<Paciente> grupo = [];

  bool cargandoOpciones = false;
  String? errorOpciones;

  /// Carga los dias habiles y, si se conoce la afiliacion, el grupo familiar.
  Future<void> cargarOpciones(String? numeroAfiliado) async {
    cargandoOpciones = true;
    errorOpciones = null;
    notifyListeners();
    try {
      dias = await _service.diasDisponibles();
      final servicio = _pacientes;
      if (servicio != null && numeroAfiliado != null && numeroAfiliado.trim().isNotEmpty) {
        grupo = await servicio.buscar(numeroAfiliado);
      }
    } on ApiException catch (e) {
      errorOpciones = e.mensaje;
    } catch (_) {
      errorOpciones = 'No se pudieron cargar los días disponibles. Intente nuevamente.';
    } finally {
      cargandoOpciones = false;
      notifyListeners();
    }
  }

  /// Pediatria solo atiende a menores de 10 anios el dia de la cita (T100-26). Sin dia elegido, se mide hoy.
  static bool puedePediatria(Paciente? paciente, DateTime? dia) {
    if (paciente == null) {
      return true;
    }
    return edadEn(paciente.fechaNacimiento, dia ?? DateTime.now()) < edadPediatria;
  }

  Future<ResultadoSolicitud?> solicitar(Especialidad especialidad, DateTime dia, {int? pacienteId}) async {
    cargando = true;
    error = null;
    resultado = null;
    notifyListeners();

    try {
      resultado = await _service.solicitar(especialidad, dia, pacienteId: pacienteId);
      return resultado;
    } on ApiException catch (e) {
      error = e.mensaje;
      return null;
    } catch (_) {
      error = 'No se pudo enviar la solicitud. Intente nuevamente.';
      return null;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }

  Future<ResultadoSolicitud?> consultar(int solicitudId) async {
    cargando = true;
    error = null;
    notifyListeners();

    try {
      resultado = await _service.consultar(solicitudId);
      return resultado;
    } on ApiException catch (e) {
      error = e.mensaje;
      return null;
    } catch (_) {
      error = 'No se pudo consultar la solicitud.';
      return null;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }

  Future<bool> listar() async {
    cargando = true;
    error = null;
    notifyListeners();

    try {
      solicitudes = await _service.listar();
      return true;
    } on ApiException catch (e) {
      error = e.mensaje;
      return false;
    } catch (_) {
      error = 'No se pudieron cargar tus citas. Intente nuevamente.';
      return false;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }
}
