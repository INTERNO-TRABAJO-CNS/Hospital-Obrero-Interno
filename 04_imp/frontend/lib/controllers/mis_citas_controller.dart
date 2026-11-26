import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/citas/cita_paciente.dart';
import '../models/solicitud/solicitud.dart';
import '../services/citas_paciente_service.dart';
import '../services/solicitud_service.dart';
import 'aviso_seguro.dart';

/// Pestañas de "Mis citas" en escritorio (Figma "WEB PACIENTE MIS CITAS").
enum FiltroCitas {
  proximas('Próximas'),
  pasadas('Pasadas'),
  todas('Todas');

  const FiltroCitas(this.titulo);

  final String titulo;
}

/// CU-08/CU-10: lista de citas propias, la elegida para el detalle, y reprogramar.
/// Tambien trae las solicitudes que siguen buscando cupo (por ejemplo, la que deja una reprogramacion).
class MisCitasController extends ChangeNotifier with AvisoSeguro {
  MisCitasController(this._citas, {SolicitudService? solicitudes, DateTime Function()? reloj})
      : _solicitudes = solicitudes,
        _reloj = reloj ?? DateTime.now;

  final CitasPacienteService _citas;
  final SolicitudService? _solicitudes;
  final DateTime Function() _reloj;

  List<CitaPaciente> citas = const [];
  List<ResultadoSolicitud> enCurso = const [];
  bool cargando = false;
  String? error;
  FiltroCitas filtro = FiltroCitas.proximas;
  int? _elegidaId;

  DateTime get ahora => _reloj();

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      citas = await _citas.misCitas();
      error = null;
    } on ApiException catch (e) {
      error = e.mensaje;
    } finally {
      enCurso = await _solicitudesEnCurso();
      cargando = false;
      notificar();
    }
  }

  /// Si no se pueden leer las solicitudes, la pantalla igual muestra las citas.
  Future<List<ResultadoSolicitud>> _solicitudesEnCurso() async {
    final servicio = _solicitudes;
    if (servicio == null) {
      return const [];
    }
    try {
      return [
        for (final s in await servicio.listar())
          if (s.estado == EstadoSolicitudApp.pendiente ||
              s.estado == EstadoSolicitudApp.enEspera ||
              s.estado == EstadoSolicitudApp.sinCupo)
            s
      ];
    } on ApiException {
      return enCurso;
    } on FormatException {
      return enCurso;
    }
  }

  void cambiarFiltro(FiltroCitas nuevo) {
    filtro = nuevo;
    notificar();
  }

  /// Citas de la pestaña elegida.
  List<CitaPaciente> get visibles {
    final momento = ahora;
    return switch (filtro) {
      FiltroCitas.todas => citas,
      FiltroCitas.proximas => [
          for (final c in citas)
            if (c.esProxima(momento)) c
        ]..sort((a, b) => a.inicio.compareTo(b.inicio)),
      FiltroCitas.pasadas => [
          for (final c in citas)
            if (!c.esProxima(momento)) c
        ],
    };
  }

  /// La cita del panel de detalle (escritorio): la elegida o la primera de la pestaña.
  CitaPaciente? get elegida {
    final lista = visibles;
    for (final c in lista) {
      if (c.citaId == _elegidaId) {
        return c;
      }
    }
    return lista.isEmpty ? null : lista.first;
  }

  void elegir(CitaPaciente cita) {
    _elegidaId = cita.citaId;
    notificar();
  }

  /// Cancela y crea la nueva solicitud. Lanza [ApiException] si el backend la rechaza.
  Future<ResultadoSolicitud> reprogramar(CitaPaciente cita) async {
    try {
      return await _citas.reprogramar(cita.citaId);
    } finally {
      await cargar();
    }
  }
}
