import 'dart:async';

import 'package:flutter/material.dart' show TimeOfDay;
import 'package:web_socket_channel/web_socket_channel.dart';

import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/catalogo/horario.dart';
import '../models/comun/fechas.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/supervision/supervision.dart';
import '../models/supervision/supervision_api.dart';
import 'catalogo_service.dart' show listaDe;
import 'enfermeria_service.dart';
import 'quirofano_service.dart';

/// Supervision en tiempo real del administrador (solo lectura).
abstract class SupervisionService {
  /// Citas comunes de hoy; con [especialidad], solo las de esa especialidad basica.
  Future<ResumenCitas> citasComunes({Especialidad? especialidad});

  /// Fichas internas de hoy de una especialidad interna en su tabla General o de Pediatria.
  Future<ResumenFichasInternas> fichasInternas(Especialidad especialidad, TablaInterna tabla);

  /// Ocupacion de todos los quirofanos activos hoy.
  Future<ResumenQuirofanos> quirofanos();

  /// Avisa cada vez que una cita cambia (WebSocket /ws/supervision). Termina si se corta la conexion.
  Stream<void> cambios();

  /// `true` si la pestaña de citas comunes muestra datos de prueba (sin backend).
  bool get citasDePrueba;
}

/// CU-19 con el backend: la pestaña de citas comunes sale de GET /api/v1/admin/supervision y se
/// actualiza sola por el WebSocket /ws/supervision (RNF-08). Fichas internas y quirofanos todavia no
/// tienen supervision en el backend: se piden a [respaldo] (datos de prueba).
class ApiSupervisionService implements SupervisionService {
  ApiSupervisionService(
    this._api, {
    required SupervisionService respaldo,
    DateTime Function()? reloj,
    WebSocketChannel Function(Uri uri)? conectar,
  })  : _respaldo = respaldo,
        _reloj = reloj ?? DateTime.now,
        _conectar = conectar ?? WebSocketChannel.connect;

  final ApiClient _api;
  final SupervisionService _respaldo;
  final DateTime Function() _reloj;
  final WebSocketChannel Function(Uri uri) _conectar;

  static const String _base = '/api/v1/admin/supervision';

  @override
  bool get citasDePrueba => false;

  @override
  Future<ResumenCitas> citasComunes({Especialidad? especialidad}) async {
    // Sin fecha: el backend usa el dia de hoy en la hora del hospital.
    final r = await _api.get(_base);
    final medicos = [
      for (final m in listaDe(r, ResumenMedicoDia.desdeJson))
        if (especialidad == null || m.especialidad == especialidad) m
    ];
    final fichas = await Future.wait([
      for (final m in medicos)
        m.total == 0 ? Future.value(const <FichaSupervision>[]) : _fichasDe(m.medicoId),
    ]);

    final ahora = horaTexto(_reloj());
    final filas = <FilaAgendaMedico>[];
    for (var i = 0; i < medicos.length; i++) {
      final m = medicos[i];
      final lista = fichas[i];
      final empezadas = lista.where((f) => f.hora.compareTo(ahora) <= 0);
      final actual = lista.isEmpty ? null : (empezadas.isEmpty ? lista.first : empezadas.last);
      filas.add(FilaAgendaMedico(
        medico: m.nombreMedico,
        especialidadOFicha: m.especialidad.nombre,
        fichasOPaciente: '${m.fichas} / ${m.cupoMaximo}',
        horarioOConsultorio: m.horaInicio == null ? '—' : '${m.horaInicio} – ${m.horaFin}',
        estado: actual?.estado ?? 'SIN FICHAS',
      ));
      for (final f in lista.where((f) => f.estado == EstadoCita.asistenciaConfirmada.codigo)) {
        filas.add(FilaAgendaMedico(
          medico: m.nombreMedico,
          especialidadOFicha: 'Ficha ${f.numeroFicha} · ${f.hora}',
          fichasOPaciente: f.paciente,
          horarioOConsultorio: '${f.consultorio}',
          estado: f.estado,
          detalle: true,
        ));
      }
    }
    int sumar(int Function(ResumenMedicoDia m) campo) => medicos.fold(0, (n, m) => n + campo(m));
    return ResumenCitas(
      fichasHoy: sumar((m) => m.total),
      atendidas: sumar((m) => m.atendidas),
      confirmadas: sumar((m) => m.confirmadas),
      canceladas: sumar((m) => m.canceladas),
      filas: filas,
    );
  }

  Future<List<FichaSupervision>> _fichasDe(int medicoId) async {
    final r = await _api.get('$_base/medicos/$medicoId/fichas');
    return listaDe(r, FichaSupervision.desdeJson);
  }

  @override
  Future<ResumenFichasInternas> fichasInternas(Especialidad especialidad, TablaInterna tabla) =>
      _respaldo.fichasInternas(especialidad, tabla);

  @override
  Future<ResumenQuirofanos> quirofanos() => _respaldo.quirofanos();

  @override
  Stream<void> cambios() {
    WebSocketChannel? canal;
    StreamSubscription<dynamic>? escucha;
    late final StreamController<void> avisos;
    avisos = StreamController<void>(
      onListen: () {
        try {
          canal = _conectar(uriWebSocket(_api.urlBase, '/ws/supervision', _api.token));
        } catch (_) {
          avisos.close();
          return;
        }
        escucha = canal!.stream.listen(
          (_) => avisos.add(null),
          onError: (Object _) => avisos.close(),
          onDone: avisos.close,
          cancelOnError: true,
        );
      },
      onCancel: () async {
        await escucha?.cancel();
        await canal?.sink.close();
      },
    );
    return avisos.stream;
  }
}

/// `http://host:8080` -> `ws://host:8080/ws/...?token=...` (`https` -> `wss`). El navegador no deja
/// mandar el header Authorization al abrir un WebSocket: el token va en la URL.
Uri uriWebSocket(String urlBase, String ruta, String? token) {
  final base = Uri.parse(urlBase);
  final camino = base.path.endsWith('/') ? base.path.substring(0, base.path.length - 1) : base.path;
  return base.replace(
    scheme: base.scheme == 'https' ? 'wss' : 'ws',
    path: '$camino$ruta',
    queryParameters: token == null ? null : {'token': token},
  );
}

/// De prueba: arma la supervision con los datos de las otras pantallas de prueba (fichas del dia
/// de enfermeria y catalogo de quirofanos).
class FakeSupervisionService implements SupervisionService {
  FakeSupervisionService({
    required EnfermeriaService enfermeria,
    required QuirofanoService quirofanos,
    DateTime Function()? reloj,
  })  : _enfermeria = enfermeria,
        _quirofanos = quirofanos,
        _reloj = reloj ?? DateTime.now;

  final EnfermeriaService _enfermeria;
  final QuirofanoService _quirofanos;
  final DateTime Function() _reloj;

  /// Cupo diario de los medicos de las fichas de prueba.
  static const Map<String, int> _cupos = {'Dr. Ramos': 7, 'Dr. Mamani': 5, 'Dra. Quispe': 6};

  @override
  bool get citasDePrueba => true;

  /// Sin backend no hay WebSocket: la pantalla se refresca sola cada cierto tiempo.
  @override
  Stream<void> cambios() => const Stream.empty();

  @override
  Future<ResumenCitas> citasComunes({Especialidad? especialidad}) async {
    final ahora = _reloj();
    final fichas = [
      for (final f in await _enfermeria.fichasDeHoy())
        if (especialidad == null || f.especialidad == especialidad) f
    ];
    final porMedico = <String, List<FichaDelDia>>{};
    for (final f in fichas) {
      porMedico.putIfAbsent(f.medico, () => []).add(f);
    }
    final filas = <FilaAgendaMedico>[];
    for (final MapEntry(key: medico, value: lista) in porMedico.entries) {
      lista.sort((a, b) => a.inicio.compareTo(b.inicio));
      final empezadas = lista.where((f) => !f.inicio.isAfter(ahora));
      final actual = empezadas.isEmpty ? lista.first : empezadas.last;
      filas.add(FilaAgendaMedico(
        medico: medico,
        especialidadOFicha: lista.first.especialidad.nombre,
        fichasOPaciente: '${lista.length} / ${_cupos[medico] ?? 7}',
        horarioOConsultorio: '${horaTexto(lista.first.inicio)} – ${horaTexto(lista.last.inicio)}',
        estado: actual.estado.codigo,
      ));
      for (final f in lista.where((f) => f.estado == EstadoCita.asistenciaConfirmada)) {
        filas.add(FilaAgendaMedico(
          medico: medico,
          especialidadOFicha: 'Ficha ${f.numeroFicha} · ${horaTexto(f.inicio)}',
          fichasOPaciente: f.paciente,
          horarioOConsultorio: f.consultorio,
          estado: f.estado.codigo,
          detalle: true,
        ));
      }
    }
    int contar(EstadoCita e) => fichas.where((f) => f.estado == e).length;
    return ResumenCitas(
      fichasHoy: fichas.length,
      atendidas: contar(EstadoCita.atendida),
      confirmadas: contar(EstadoCita.asistenciaConfirmada),
      canceladas: contar(EstadoCita.cancelada),
      filas: filas,
    );
  }

  @override
  Future<ResumenFichasInternas> fichasInternas(Especialidad especialidad, TablaInterna tabla) async {
    final fecha = soloFecha(_reloj());
    if (especialidad == Especialidad.gastroenterologia && tabla == TablaInterna.general) {
      return ResumenFichasInternas(fecha: fecha, fichas: _gastroGeneral(fecha));
    }
    return ResumenFichasInternas(fecha: fecha, fichas: _generar(especialidad, tabla));
  }

  /// Las filas del Figma "ADMIN SUPERVISION FICHAS INTERNAS".
  List<FichaInternaSupervision> _gastroGeneral(DateTime hoy) {
    final pasado = diaHabilDesde(DateTime(hoy.year, hoy.month, hoy.day + 2));
    return [
      const FichaInternaSupervision(
          hora: '08:00',
          paciente: 'Pedro Huanca',
          especialista: 'Dr. Vargas',
          derivadoPor: 'Dr. Ramos',
          estado: EstadoFichaInterna.atendida),
      const FichaInternaSupervision(
          hora: '08:30',
          paciente: 'Elena Rojas',
          especialista: 'Dra. Arce',
          derivadoPor: 'Dr. Ramos',
          estado: EstadoFichaInterna.atendida),
      const FichaInternaSupervision(
          hora: '09:00',
          paciente: 'Carmen Ticona',
          especialista: 'Dr. Vargas',
          derivadoPor: 'Dr. Ramos',
          estado: EstadoFichaInterna.cancelada,
          detalle: 'no se presentó'),
      const FichaInternaSupervision(
          hora: '10:20',
          paciente: 'Marta Ticona',
          especialista: 'Dr. Vargas',
          derivadoPor: 'Dr. Ramos',
          estado: EstadoFichaInterna.asignada),
      const FichaInternaSupervision(
          hora: '12:30',
          paciente: 'Juan Mamani',
          especialista: 'Dr. Vargas',
          urgencia: true,
          derivadoPor: 'Dr. Ramos',
          estado: EstadoFichaInterna.asignada),
      FichaInternaSupervision(
          paciente: 'Jorge Limachi',
          especialista: 'Dra. Arce',
          derivadoPor: 'Dra. Salas',
          estado: EstadoFichaInterna.desplazada,
          detalle: 'pasó al ${diaMesCorto(pasado)} 11:00'),
      const FichaInternaSupervision(
          paciente: 'Ana Choque', derivadoPor: 'Dr. Ramos', estado: EstadoFichaInterna.enEspera),
    ];
  }

  static const List<String> _adultos = [
    'Rosa Mamani',
    'Victor Choque',
    'Lidia Quispe',
    'Hernán Torrez',
    'Gladys Apaza',
    'Mario Condori',
    'Nelly Cruz',
  ];
  static const List<String> _ninos = [
    'Camila Flores',
    'Diego Rojas',
    'Valeria Copa',
    'Joaquín Huanca',
    'Luciana Paz',
    'Mateo Vargas',
    'Abril Ticona',
  ];
  static const Map<Especialidad, List<String>> _especialistas = {
    Especialidad.cardiologia: ['Dr. Paredes'],
    Especialidad.nefrologia: ['Dra. Rocha'],
    Especialidad.neumologia: ['Dr. Ibáñez'],
    Especialidad.endocrinologia: ['Dra. Gutiérrez'],
    Especialidad.gastroenterologia: ['Dr. Vargas', 'Dra. Arce'],
    Especialidad.hematologia: ['Dr. Torrico'],
    Especialidad.infectologia: ['Dra. Mendoza'],
    Especialidad.dermatologia: ['Dra. Salinas'],
    Especialidad.medicinaTrabajo: ['Dr. Aguilar'],
    Especialidad.terapiaIntensiva: ['Dr. Lima'],
  };
  static const List<EstadoFichaInterna> _estados = [
    EstadoFichaInterna.atendida,
    EstadoFichaInterna.asignada,
    EstadoFichaInterna.atendida,
    EstadoFichaInterna.asignada,
    EstadoFichaInterna.enEspera,
    EstadoFichaInterna.cancelada,
  ];

  /// Filas de prueba para las otras tablas (siempre las mismas para cada especialidad y tabla).
  List<FichaInternaSupervision> _generar(Especialidad e, TablaInterna tabla) {
    final semilla = e.index * 2 + tabla.index;
    final cantidad = 3 + semilla % 4;
    final pacientes = tabla == TablaInterna.pediatria ? _ninos : _adultos;
    final especialistas = _especialistas[e] ?? const ['Dr. Vargas'];
    final derivan = tabla == TablaInterna.pediatria ? const ['Dra. Quispe'] : const ['Dr. Ramos', 'Dra. Salas'];
    // Terapia Intensiva solo atiende urgencias.
    final soloUrgencias = e == Especialidad.terapiaIntensiva;
    return [
      for (var i = 0; i < cantidad; i++)
        () {
          final estado = _estados[(semilla + i) % _estados.length];
          final sinHora = estado == EstadoFichaInterna.enEspera;
          final minutos = 8 * 60 + i * e.duracionMax;
          return FichaInternaSupervision(
            hora: sinHora ? null : horaATexto(TimeOfDay(hour: minutos ~/ 60, minute: minutos % 60)),
            paciente: pacientes[(semilla + i) % pacientes.length],
            especialista: sinHora ? null : especialistas[i % especialistas.length],
            urgencia: soloUrgencias || (semilla + i) % 5 == 0,
            derivadoPor: derivan[i % derivan.length],
            estado: estado,
            detalle: estado == EstadoFichaInterna.cancelada ? 'no se presentó' : null,
          );
        }(),
    ];
  }

  /// Cirugias de prueba de cada quirofano (las del Figma "ADMIN SUPERVISION QUIROFANOS").
  static const Map<String, List<CirugiaDelDia>> _cirugias = {
    'G-1': [
      CirugiaDelDia(
          paciente: 'Ana Quispe', inicio: TimeOfDay(hour: 7, minute: 30), fin: TimeOfDay(hour: 8, minute: 30)),
      CirugiaDelDia(
          paciente: 'Marco Ríos', inicio: TimeOfDay(hour: 10, minute: 0), fin: TimeOfDay(hour: 11, minute: 30)),
      CirugiaDelDia(
          paciente: 'Luis Gómez', inicio: TimeOfDay(hour: 15, minute: 0), fin: TimeOfDay(hour: 16, minute: 0)),
    ],
    'G-2': [
      CirugiaDelDia(paciente: 'Lidia Paz', inicio: TimeOfDay(hour: 9, minute: 0), fin: TimeOfDay(hour: 10, minute: 0)),
      CirugiaDelDia(
          paciente: 'Juan Mamani',
          inicio: TimeOfDay(hour: 13, minute: 30),
          fin: TimeOfDay(hour: 14, minute: 30),
          urgente: true),
      CirugiaDelDia(
          paciente: 'Raúl Choque', inicio: TimeOfDay(hour: 16, minute: 0), fin: TimeOfDay(hour: 17, minute: 30)),
    ],
    'H-1': [
      CirugiaDelDia(
          paciente: 'Rosa Flores', inicio: TimeOfDay(hour: 8, minute: 0), fin: TimeOfDay(hour: 10, minute: 0)),
    ],
    'D-1': [
      CirugiaDelDia(paciente: 'Teo Cruz', inicio: TimeOfDay(hour: 9, minute: 0), fin: TimeOfDay(hour: 9, minute: 45)),
    ],
  };

  @override
  Future<ResumenQuirofanos> quirofanos() async {
    // En el orden del catalogo (G-1, G-2, H-1, D-1), como el Figma.
    final activos = [
      for (final q in await _quirofanos.listar())
        if (q.activo) q
    ];
    return ResumenQuirofanos(
      fecha: soloFecha(_reloj()),
      desplazamientos: 1,
      quirofanos: [
        for (final q in activos)
          QuirofanoDelDia(
            codigo: q.nombre,
            especialidad: q.especialidad,
            apertura: q.apertura,
            cierre: q.cierre,
            limpiezaMin: q.limpiezaMin,
            cirugias: _cirugias[q.nombre] ?? const [],
          ),
      ],
    );
  }
}
