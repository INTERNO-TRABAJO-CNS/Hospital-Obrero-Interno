import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/comun/fechas_iso.dart';
import '../models/medico/agenda_medico.dart';
import '../models/medico/cirugia.dart';
import '../models/medico/ficha_interna.dart';
import '../models/medico/historial_paciente.dart';
import '../services/cirugias_service.dart';
import '../services/fichas_internas_service.dart';
import '../services/medico_service.dart';
import 'aviso_seguro.dart';

enum TipoEvento {
  atencion('ATENCIÓN'),
  cita('CITA'),
  fichaInterna('FICHA INTERNA'),
  cirugia('CIRUGÍA');

  const TipoEvento(this.etiqueta);

  final String etiqueta;
}

enum FiltroHistorial {
  todo('Todo', TipoEvento.values),
  atenciones('Atenciones y recetas', [TipoEvento.atencion]),
  fichas('Fichas internas', [TipoEvento.fichaInterna]),
  cirugias('Cirugías', [TipoEvento.cirugia]);

  const FiltroHistorial(this.texto, this.tipos);

  final String texto;
  final List<TipoEvento> tipos;
}

/// Un evento de la línea de tiempo (Figma "HISTORIAL PACIENTE").
class EventoHistorial {
  EventoHistorial({
    required this.clave,
    required this.tipo,
    required this.fecha,
    required this.hora,
    required this.titulo,
    required this.especialidad,
    required this.estado,
    List<String>? recetas,
    List<String>? lineas,
  })  : recetas = recetas ?? [],
        lineas = lineas ?? [];

  final String clave;
  TipoEvento tipo;
  final String fecha;
  final String hora;

  /// "Consultorio Familiar · Carlos Ramos".
  final String titulo;
  final String especialidad;
  final String? estado;
  final List<String> recetas;

  /// Líneas de detalle bajo el título.
  List<String> lineas;
}

/// Arma la línea de tiempo (más reciente primero) con citas, atenciones, fichas internas y cirugías.
List<EventoHistorial> lineaDeTiempo(HistorialPaciente h, List<FichaInterna> fichas, List<Cirugia> cirugias) {
  final eventos = <String, EventoHistorial>{};
  for (final c in h.citasPasadas) {
    eventos['cita-${c.citaId}'] = EventoHistorial(
      clave: 'cita-${c.citaId}',
      tipo: c.estado == 'ATENDIDA' ? TipoEvento.atencion : TipoEvento.cita,
      fecha: c.fecha,
      hora: c.hora,
      titulo: '${nombreEspecialidad(c.especialidad)} · ${c.medico}',
      especialidad: c.especialidad,
      estado: c.estado,
    );
  }
  for (final a in h.atenciones) {
    final citaId = a.recetas.isEmpty ? null : a.recetas.first.citaId;
    final clave = citaId != null ? 'cita-$citaId' : 'atencion-${a.fecha}-${a.hora}';
    final recetas = [for (final r in a.recetas) r.medicacion];
    final existente = eventos[clave];
    if (existente != null) {
      existente.tipo = TipoEvento.atencion;
      existente.recetas.addAll(recetas);
    } else {
      eventos[clave] = EventoHistorial(
        clave: clave,
        tipo: TipoEvento.atencion,
        fecha: a.fecha,
        hora: a.hora,
        titulo: '${nombreEspecialidad(a.especialidad)} · ${a.medico}',
        especialidad: a.especialidad,
        estado: 'ATENDIDA',
        recetas: recetas,
      );
    }
  }
  for (final e in eventos.values) {
    e.lineas = e.recetas.isNotEmpty
        ? [for (final r in e.recetas) 'Receta: $r']
        : e.estado != null
            ? ['Cita ${nombreEstadoCita(e.estado!).toLowerCase()}']
            : [];
  }
  for (final f in fichas) {
    final llegada = f.llegada;
    final lineas = [
      f.estado == EstadoFicha.enEspera
          ? 'En espera: sin fecha todavía'
          : 'Asignada: ${f.fecha != null ? fechaMesIso(f.fecha!, conAnio: false) : ''} · ${f.hora ?? ''} · '
              '${f.medico ?? ''} · Consultorio ${f.consultorio ?? ''}',
      'Motivo: ${f.motivo}',
      'Gravedad: ${f.gravedad.codigo} (solo la ven el médico general y el pediatra)',
      if (f.observaciones != null) 'Observaciones: ${f.observaciones}',
    ];
    eventos['ficha-${f.ticket}'] = EventoHistorial(
      clave: 'ficha-${f.ticket}',
      tipo: TipoEvento.fichaInterna,
      fecha: isoDe(llegada),
      hora: horaActual(llegada),
      titulo: '${f.especialidad.nombre} · derivada por ${f.derivadoPor}',
      especialidad: f.especialidad.codigo,
      estado: f.estado.codigo,
      lineas: lineas,
    );
  }
  for (final c in cirugias) {
    eventos['cirugia-${c.numero}'] = EventoHistorial(
      clave: 'cirugia-${c.numero}',
      tipo: TipoEvento.cirugia,
      fecha: c.fecha,
      hora: c.inicio,
      titulo: '${c.especialidad.nombre} · ${c.cirujano}',
      especialidad: c.especialidad.codigo,
      estado: c.estado.codigo,
      lineas: ['Quirófano ${c.quirofano} · ${c.inicio} – ${c.fin} · limpieza hasta ${c.limpiezaHasta}'],
    );
  }
  return eventos.values.toList()..sort((x, y) => (y.fecha + y.hora).compareTo(x.fecha + x.hora));
}

/// CU-14: historial del paciente (solo lectura) con atenciones, fichas internas y cirugías.
class HistorialPacienteController extends ChangeNotifier with AvisoSeguro {
  HistorialPacienteController(this._servicio, {FichasInternasService? fichas, CirugiasService? cirugias})
      : _fichas = fichas,
        _cirugias = cirugias;

  final HistorialPacienteService _servicio;
  final FichasInternasService? _fichas;
  final CirugiasService? _cirugias;

  HistorialPaciente? historial;
  List<EventoHistorial> eventos = const [];
  FiltroHistorial filtro = FiltroHistorial.todo;
  bool cargando = false;
  String? error;
  bool sinPermiso = false;

  List<EventoHistorial> get filtrados => [
        for (final e in eventos)
          if (filtro.tipos.contains(e.tipo)) e
      ];

  /// Las 3 atenciones con receta o cirugías más recientes (panel de la atención).
  List<EventoHistorial> get recientes => eventos
      .where((e) => (e.tipo == TipoEvento.atencion && e.recetas.isNotEmpty) || e.tipo == TipoEvento.cirugia)
      .take(3)
      .toList();

  Future<void> cargar(int pacienteId) async {
    cargando = true;
    error = null;
    sinPermiso = false;
    historial = null;
    eventos = const [];
    filtro = FiltroHistorial.todo;
    notificar();
    try {
      final h = await _servicio.historial(pacienteId);
      final fichas = await _fichas?.delPaciente(pacienteId) ?? const <FichaInterna>[];
      final cirugias = await _cirugias?.delPaciente(pacienteId) ?? const <Cirugia>[];
      historial = h;
      eventos = lineaDeTiempo(h, fichas, cirugias);
    } on ApiException catch (e) {
      sinPermiso = e.estado == 403;
      error = e.mensaje;
    } finally {
      cargando = false;
      notificar();
    }
  }

  void filtrar(FiltroHistorial nuevo) {
    filtro = nuevo;
    notificar();
  }
}
