import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/comun/fechas_iso.dart';
import '../models/comun/texto.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/medico/agenda_medico.dart';
import '../models/medico/ficha_interna.dart';
import '../models/medico/medico_actual.dart';
import '../services/fichas_internas_service.dart';
import '../services/medico_service.dart';
import 'aviso_seguro.dart';
import 'medico_controller.dart';

/// CU-21: "Ticket interno" (nueva ficha interna desde una cita de hoy o por urgencia).
class TicketInternoController extends ChangeNotifier with AvisoSeguro {
  TicketInternoController({
    required this.medico,
    required this.agenda,
    required FichasInternasService fichas,
    required HistorialPacienteService historial,
  })  : _fichas = fichas,
        _historial = historial;

  final MedicoActual medico;
  final AgendaMedicoController agenda;
  final FichasInternasService _fichas;
  final HistorialPacienteService _historial;

  bool urgencia = false;
  int? citaId;
  PacienteBuscado? paciente;
  String? errorBusqueda;
  Especialidad? especialidad;
  Gravedad gravedad = Gravedad.media;
  String motivo = '';
  bool enviando = false;
  ResultadoFicha? resultado;
  String? error;

  Future<void> iniciar(int? citaInicial) async {
    if (!agenda.esHoy || agenda.agenda == null) {
      await agenda.volverAHoy();
    }
    if (citaInicial != null) {
      await elegirCita(citaInicial);
    }
    notificar();
  }

  /// Citas de hoy desde las que se puede derivar (confirmadas o atendidas).
  List<FichaAgenda> get citasDerivables => [
        for (final f in agenda.agenda?.fichas ?? const <FichaAgenda>[])
          if (f.estado == EstadoCita.asistenciaConfirmada || f.estado == EstadoCita.atendida) f
      ];

  FichaAgenda? get cita =>
      (agenda.agenda?.fichas ?? const <FichaAgenda>[]).where((f) => f.citaId == citaId).firstOrNull;

  Future<void> elegirCita(int id) async {
    citaId = id;
    paciente = null;
    notificar();
    final f = cita;
    if (f == null) {
      return;
    }
    try {
      final h = await _historial.historial(f.pacienteId);
      paciente = PacienteBuscado(
          pacienteId: f.pacienteId,
          nombre: f.nombrePaciente,
          numeroAfiliado: h.numeroAfiliado ?? '',
          edad: h.edad ?? 0);
    } on ApiException {
      paciente = PacienteBuscado(pacienteId: f.pacienteId, nombre: f.nombrePaciente, numeroAfiliado: '', edad: 0);
    }
    notificar();
  }

  void cambiarUrgencia(bool valor) {
    urgencia = valor;
    resultado = null;
    if (valor) {
      paciente = null;
      notificar();
    } else {
      if (especialidad == Especialidad.terapiaIntensiva) {
        especialidad = null;
      }
      notificar();
      if (citaId != null) {
        elegirCita(citaId!);
      }
    }
  }

  Future<void> buscar(String numeroAfiliado) async {
    errorBusqueda = null;
    paciente = await _fichas.buscarPaciente(numeroAfiliado);
    if (paciente == null) {
      errorBusqueda = 'No se encontró un paciente con ese N° de afiliado.';
    }
    notificar();
  }

  void elegirEspecialidad(Especialidad e) {
    if (e == Especialidad.terapiaIntensiva && !urgencia) {
      return;
    }
    especialidad = e;
    notificar();
  }

  void elegirGravedad(Gravedad g) {
    gravedad = g;
    notificar();
  }

  void escribirMotivo(String texto) {
    motivo = texto;
    notificar();
  }

  /// "Sin cita (urgencia)" o "Ficha N° 3 · hoy 09:30 · Consultorio Familiar".
  String get datoCita {
    if (urgencia) {
      return 'Sin cita (urgencia)';
    }
    final c = cita;
    return c == null ? '' : 'Ficha N° ${c.numeroFicha} · hoy ${horaCorta(c.hora)} · ${medico.especialidad.nombre}';
  }

  String get datoPaciente {
    final p = paciente;
    if (p == null) {
      return '';
    }
    return [p.numeroAfiliado, p.nombre, '${p.edad} años'].where((s) => s.isNotEmpty).join(' · ');
  }

  bool get listo {
    final m = motivo.trim().length;
    return paciente != null && especialidad != null && m > 0 && m <= maxMotivo && (urgencia || cita != null);
  }

  bool get rechazado => resultado?.rechazada ?? false;
  FichaInterna? get ficha => resultado?.ficha;

  Future<void> generar() async {
    final p = paciente;
    final e = especialidad;
    if (!listo || p == null || e == null || enviando) {
      return;
    }
    enviando = true;
    error = null;
    notificar();
    final c = cita;
    final origen = !urgencia && c != null
        ? CitaOrigen(
            citaId: c.citaId,
            numeroFicha: c.numeroFicha,
            fecha: agenda.agenda!.fecha,
            hora: c.hora,
            estado: c.estado.codigo)
        : null;
    try {
      resultado = await _fichas.generar(
        SolicitudFicha(
          citaOrigenId: origen?.citaId,
          pacienteId: p.pacienteId,
          nombrePaciente: p.nombre,
          numeroAfiliado: p.numeroAfiliado,
          edad: p.edad,
          especialidad: e,
          urgencia: urgencia,
          gravedad: gravedad,
          motivo: motivo,
        ),
        medico,
        origen,
      );
      if (!resultado!.rechazada) {
        await agenda.cargar();
      }
    } on ApiException catch (ex) {
      error = ex.mensaje;
    } finally {
      enviando = false;
      notificar();
    }
  }
}

/// CU-23: agenda de fichas internas del especialista.
class FichasEspecialistaController extends ChangeNotifier with AvisoSeguro {
  FichasEspecialistaController(this.medico, this._fichas, {DateTime Function()? reloj})
      : _reloj = reloj ?? DateTime.now,
        fecha = hoyIso((reloj ?? DateTime.now)());

  final MedicoActual medico;
  final FichasInternasService _fichas;
  final DateTime Function() _reloj;

  String fecha;
  List<FichaInterna> fichas = const [];
  FichaInterna? elegida;
  String observaciones = '';
  bool cargando = false;
  String? error;

  String get hoy => hoyIso(_reloj());

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      fichas = await _fichas.agendaEspecialista(medico, fecha);
      elegida = fichas.where((f) => f.ticket == elegida?.ticket).firstOrNull ??
          fichas.where((f) => f.estado == EstadoFicha.asignada).firstOrNull;
    } on ApiException catch (e) {
      error = e.mensaje;
    } finally {
      cargando = false;
      notificar();
    }
  }

  Future<void> cambiarFecha(String nueva) {
    fecha = nueva;
    return cargar();
  }

  void elegir(FichaInterna f) {
    elegida = f;
    notificar();
  }

  void escribirObservaciones(String texto) {
    observaciones = texto;
    notificar();
  }

  String? get consultorio => fichas.where((f) => f.consultorio != null).firstOrNull?.consultorio;

  /// "Jueves 12 de noviembre · 7 fichas internas · Consultorio 12".
  String get resumen {
    final n = fichas.length;
    return [
      fechaTituloIso(fecha),
      '$n ${n == 1 ? 'ficha interna' : 'fichas internas'}',
      if (consultorio != null) 'Consultorio $consultorio',
    ].join(' · ');
  }

  bool get opera => medico.especialidad.operaEnQuirofano;

  /// "No se presentó" se habilita después de la hora de la ficha.
  bool yaPasoLaHora(FichaInterna f) {
    if (f.fecha == null || f.hora == null) {
      return false;
    }
    final ahora = _reloj();
    final hoy = hoyIso(ahora);
    return f.fecha!.compareTo(hoy) < 0 || (f.fecha == hoy && horaActual(ahora).compareTo(f.hora!) >= 0);
  }

  Future<void> atendida(FichaInterna f) async {
    error = null;
    try {
      await _fichas.marcarAtendida(f.ticket, f.ticket == elegida?.ticket ? observaciones : '');
      observaciones = '';
      await cargar();
    } on ApiException catch (e) {
      error = e.mensaje;
      notificar();
    }
  }

  Future<void> noSePresento(FichaInterna f) async {
    error = null;
    try {
      await _fichas.noSePresento(f.ticket);
      await cargar();
    } on ApiException catch (e) {
      error = e.mensaje;
      notificar();
    }
  }
}

/// CU-25: cola de prioridad de una especialidad (solo lectura).
class ColaPrioridadController extends ChangeNotifier with AvisoSeguro {
  ColaPrioridadController(this.medico, this._fichas);

  final MedicoActual medico;
  final FichasInternasService _fichas;

  Especialidad especialidad = Especialidad.gastroenterologia;
  List<FichaInterna> fichas = const [];
  bool cargando = false;

  /// El médico general ve la tabla general; el pediatra, la pediátrica.
  TablaFicha get tabla => medico.tabla;

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      fichas = await _fichas.cola(especialidad, tabla);
    } finally {
      cargando = false;
      notificar();
    }
  }

  Future<void> elegir(Especialidad e) {
    especialidad = e;
    return cargar();
  }

  /// Se resalta la última ficha que derivó este médico (como en el Figma).
  int? get ultimaMia {
    final mias = [
      for (final f in fichas)
        if (mismoNombre(f.derivadoPor, medico.nombre)) f
    ]..sort((a, b) => b.llegada.compareTo(a.llegada));
    return mias.firstOrNull?.ticket;
  }
}
