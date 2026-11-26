import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/auth/paciente.dart';
import '../services/paciente_service.dart';
import 'aviso_seguro.dart';

/// Resultado de registrar desde enfermeria: el paciente y si ya existia (CU-03 [1a]: no se duplica).
class RegistroPaciente {
  const RegistroPaciente(this.paciente, {required this.yaExistia});

  final Paciente paciente;
  final bool yaExistia;
}

/// CU-03 (S3-12): enfermeria / admision busca al paciente por su numero de afiliado (y CI) y, si no existe,
/// lo registra con la afiliacion del titular. Nunca crea un duplicado.
class PacientesEnfermeriaController extends ChangeNotifier with AvisoSeguro {
  PacientesEnfermeriaController(this._servicio, {DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now;

  final PacienteService _servicio;
  final DateTime Function() _reloj;

  List<Paciente> resultados = const [];
  Paciente? elegido;
  bool cargando = false;
  bool registrando = false;

  /// true despues de la primera busqueda (para mostrar "no existe" en vez de la pantalla vacia).
  bool buscado = false;
  String? error;

  /// Ultima busqueda: con esto se completa el formulario de registro.
  String afiliadoBuscado = '';
  String ciBuscado = '';

  Future<void> buscar(String afiliado, {String ci = ''}) async {
    if (afiliado.trim().isEmpty) {
      error = 'Ingrese el número de afiliado';
      notificar();
      return;
    }
    afiliadoBuscado = afiliado.trim();
    ciBuscado = ci.trim();
    cargando = true;
    error = null;
    notificar();
    try {
      resultados = await _servicio.buscar(afiliadoBuscado, ci: ciBuscado);
      elegido = resultados.length == 1 ? resultados.first : null;
    } on ApiException catch (e) {
      resultados = const [];
      elegido = null;
      error = e.mensaje;
    } finally {
      buscado = true;
      cargando = false;
      notificar();
    }
  }

  void elegir(Paciente p) {
    elegido = p;
    notificar();
  }

  /// [3a]: lo que falta o esta mal antes de enviar (null si esta completo).
  String? validar({
    required String afiliado,
    required String ci,
    required String nombres,
    required String apellidos,
    required DateTime? nacimiento,
  }) {
    final faltan = [
      if (afiliado.trim().isEmpty) 'afiliación del titular',
      if (ci.trim().isEmpty) 'CI',
      if (nombres.trim().isEmpty) 'nombres',
      if (apellidos.trim().isEmpty) 'apellidos',
      if (nacimiento == null) 'fecha de nacimiento',
    ];
    if (faltan.isNotEmpty) {
      return 'Complete: ${faltan.join(', ')}.';
    }
    if (ci.trim().length > 20) {
      return 'El CI no puede pasar de 20 caracteres.';
    }
    if (nacimiento!.isAfter(_reloj())) {
      return 'La fecha de nacimiento no puede ser futura.';
    }
    return null;
  }

  /// Registra al paciente. Si el backend responde YA_REGISTRADO, busca y devuelve el existente
  /// (se abre su ficha, no se crea otro). Otros rechazos lanzan [ApiException].
  Future<RegistroPaciente> registrar(RegistroBeneficiario datos) async {
    registrando = true;
    notificar();
    try {
      final nuevo = await _servicio.registrarBeneficiario(datos);
      _mostrar([nuevo], nuevo, datos);
      return RegistroPaciente(nuevo, yaExistia: false);
    } on ApiException catch (e) {
      if (e.codigo != 'YA_REGISTRADO') {
        rethrow;
      }
      final existentes = await _servicio.buscar(datos.numeroAfiliadoTitular, ci: datos.ci);
      if (existentes.isEmpty) {
        rethrow;
      }
      _mostrar(existentes, existentes.first, datos);
      return RegistroPaciente(existentes.first, yaExistia: true);
    } finally {
      registrando = false;
      notificar();
    }
  }

  void _mostrar(List<Paciente> lista, Paciente p, RegistroBeneficiario datos) {
    afiliadoBuscado = datos.numeroAfiliadoTitular.trim();
    ciBuscado = datos.ci.trim();
    resultados = lista;
    elegido = p;
    buscado = true;
    error = null;
  }

  /// Edad cumplida a hoy.
  int edadDe(Paciente p) {
    final hoy = _reloj();
    final n = p.fechaNacimiento;
    var edad = hoy.year - n.year;
    if (hoy.month < n.month || (hoy.month == n.month && hoy.day < n.day)) {
      edad--;
    }
    return edad;
  }
}
