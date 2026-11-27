import 'dart:async';
import 'dart:convert';

import 'package:cns_app/controllers/supervision_controller.dart';
import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/reportes/reporte.dart';
import 'package:cns_app/models/supervision/supervision.dart';
import 'package:cns_app/services/reportes_service.dart';
import 'package:cns_app/services/supervision_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

http.Response _json(Object? cuerpo, [int estado = 200]) =>
    http.Response(jsonEncode(cuerpo), estado, headers: {'content-type': 'application/json; charset=utf-8'});

/// Lunes 23/11/2026, 09:45.
DateTime _reloj() => DateTime(2026, 11, 23, 9, 45);

/// Respaldo para las pestañas que todavia no tienen backend.
class _SinBackend implements SupervisionService {
  @override
  bool get citasDePrueba => true;
  @override
  Stream<void> cambios() => const Stream.empty();
  @override
  Future<ResumenCitas> citasComunes({Especialidad? especialidad}) => throw UnimplementedError();
  @override
  Future<ResumenFichasInternas> fichasInternas(Especialidad especialidad, TablaInterna tabla) async =>
      ResumenFichasInternas(fecha: _reloj(), fichas: const []);
  @override
  Future<ResumenQuirofanos> quirofanos() async =>
      ResumenQuirofanos(fecha: _reloj(), quirofanos: const [], desplazamientos: 0);
}

/// WebSocket de prueba: lo que se agrega a [entrada] llega como mensaje del servidor.
class _CanalFalso implements WebSocketChannel {
  final StreamController<dynamic> entrada = StreamController<dynamic>();
  bool cerrado = false;

  @override
  Stream<dynamic> get stream => entrada.stream;

  @override
  WebSocketSink get sink => _SinkFalso(this);

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _SinkFalso implements WebSocketSink {
  _SinkFalso(this._canal);

  final _CanalFalso _canal;

  @override
  Future<void> close([int? closeCode, String? closeReason]) async => _canal.cerrado = true;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

/// Respuestas del backend de supervision de hoy.
http.Response _supervision(http.Request r) {
  switch (r.url.path) {
    case '/api/v1/admin/supervision':
      return _json([
        {
          'especialidad': 'CONSULTORIO_FAMILIAR',
          'medicoId': 1,
          'nombreMedico': 'Carlos Ramos',
          'horaInicio': '08:00:00',
          'horaFin': '11:45:00',
          'cupoMaximo': 7,
          'fichas': 4,
          'programadas': 1,
          'confirmadas': 1,
          'atendidas': 2,
          'canceladas': 0,
        },
        {
          'especialidad': 'PEDIATRIA',
          'medicoId': 3,
          'nombreMedico': 'Lucia Quispe',
          'horaInicio': null,
          'horaFin': null,
          'cupoMaximo': 7,
          'fichas': 0,
          'programadas': 0,
          'confirmadas': 0,
          'atendidas': 0,
          'canceladas': 1,
        },
      ]);
    case '/api/v1/admin/supervision/medicos/1/fichas':
      return _json([
        {'citaId': 10, 'numeroFicha': 1, 'hora': '08:00:00', 'estado': 'ATENDIDA', 'consultorio': 4, 'nombrePaciente': 'Luis Paz'},
        {'citaId': 11, 'numeroFicha': 2, 'hora': '08:45:00', 'estado': 'ATENDIDA', 'consultorio': 4, 'nombrePaciente': 'Rosa Cruz'},
        {
          'citaId': 12,
          'numeroFicha': 3,
          'hora': '09:30:00',
          'estado': 'ASISTENCIA_CONFIRMADA',
          'consultorio': 4,
          'nombrePaciente': 'Benjamin Sirpa'
        },
        {'citaId': 13, 'numeroFicha': 4, 'hora': '10:15:00', 'estado': 'PROGRAMADA', 'consultorio': 4, 'nombrePaciente': 'Ana Copa'},
      ]);
    case '/api/v1/admin/supervision/medicos/3/fichas':
      return _json([
        {'citaId': 20, 'numeroFicha': 1, 'hora': '08:30:00', 'estado': 'CANCELADA', 'consultorio': 2, 'nombrePaciente': 'Mia Rojas'},
      ]);
  }
  return _json({'codigo': 'NO_ENCONTRADA', 'mensaje': 'No existe'}, 404);
}

void main() {
  group('ApiSupervisionService (CU-19)', () {
    ApiSupervisionService servicio({WebSocketChannel Function(Uri)? conectar}) => ApiSupervisionService(
          ApiClient(cliente: MockClient((r) async => _supervision(r)), urlBase: 'http://cns.test:8080')..token = 'abc',
          respaldo: _SinBackend(),
          reloj: _reloj,
          conectar: conectar,
        );

    test('arma la agenda por médico con el resumen y las fichas del backend', () async {
      final r = await servicio().citasComunes();

      expect(r.fichasHoy, 5);
      expect(r.atendidas, 2);
      expect(r.confirmadas, 1);
      expect(r.canceladas, 1);

      final ramos = r.filas.first;
      expect(ramos.medico, 'Carlos Ramos');
      expect(ramos.especialidadOFicha, 'Consultorio Familiar');
      expect(ramos.fichasOPaciente, '4 / 7');
      expect(ramos.horarioOConsultorio, '08:00 – 11:45');
      expect(ramos.estado, 'ASISTENCIA_CONFIRMADA', reason: 'a las 09:45 se atiende la ficha de las 09:30');

      final detalle = r.filas[1];
      expect(detalle.detalle, isTrue);
      expect(detalle.especialidadOFicha, 'Ficha 3 · 09:30');
      expect(detalle.fichasOPaciente, 'Benjamin Sirpa');
      expect(detalle.horarioOConsultorio, '4');

      final quispe = r.filas.last;
      expect(quispe.horarioOConsultorio, '—', reason: 'sin horario ese día');
      expect(quispe.estado, 'CANCELADA');
    });

    test('filtra por especialidad básica', () async {
      final r = await servicio().citasComunes(especialidad: Especialidad.pediatria);

      expect(r.filas.map((f) => f.medico), ['Lucia Quispe']);
      expect(r.fichasHoy, 1);
    });

    test('las citas comunes no son datos de prueba', () {
      expect(servicio().citasDePrueba, isFalse);
    });

    test('cambios(): cada mensaje del WebSocket avisa un cambio y se conecta con el token', () async {
      final canal = _CanalFalso();
      Uri? conectado;
      final s = servicio(conectar: (uri) {
        conectado = uri;
        return canal;
      });

      var avisos = 0;
      final escucha = s.cambios().listen((_) => avisos++);
      canal.entrada.add('{"tipo":"CITA_CAMBIO","cita":{"citaId":12}}');
      canal.entrada.add('{"tipo":"CITA_CAMBIO","cita":{"citaId":13}}');
      await Future<void>.delayed(Duration.zero);

      expect(avisos, 2);
      expect(conectado.toString(), 'ws://cns.test:8080/ws/supervision?token=abc');

      await escucha.cancel();
      expect(canal.cerrado, isTrue);
    });
  });

  test('uriWebSocket: https usa wss', () {
    expect(uriWebSocket('https://cns.bo/', '/ws/supervision', 't').toString(), 'wss://cns.bo/ws/supervision?token=t');
    expect(uriWebSocket('http://localhost:8080', '/ws/fichas', null).toString(), 'ws://localhost:8080/ws/fichas');
  });

  group('SupervisionController', () {
    test('recarga las citas comunes cuando llega un cambio por WebSocket', () async {
      final cambios = StreamController<void>();
      var pedidos = 0;
      final s = _ServicioContado(cambios.stream, () => pedidos++);
      final c = SupervisionController(s, intervalo: const Duration(hours: 1));

      c.iniciar();
      await Future<void>.delayed(Duration.zero);
      expect(pedidos, 1);

      cambios.add(null);
      await Future<void>.delayed(Duration.zero);
      expect(pedidos, 2);
      expect(c.datosDePrueba, isFalse);

      c.elegirPestana(PestanaSupervision.quirofanos);
      expect(c.datosDePrueba, isTrue, reason: 'quirófanos todavía no tiene backend');
      c.dispose();
      await cambios.close();
    });
  });

  group('ApiReportesService (CU-19/CU-20)', () {
    final pedidas = <Uri>[];

    ApiReportesService servicio(Map<String, Object?> respuestas) => ApiReportesService(
          ApiClient(
              cliente: MockClient((r) async {
                pedidas.add(r.url);
                return _json(respuestas[r.url.path]);
              }),
              urlBase: 'http://cns.test:8080'),
          respaldo: FakeReportesService(reloj: _reloj),
          reloj: _reloj,
        );

    setUp(pedidas.clear);

    test('por médico: marca en rojo bajo la meta y pide la semana de lunes a viernes', () async {
      final r = await servicio({
        '/api/v1/admin/reportes/cumplimiento': [
          {'medicoId': 1, 'nombreMedico': 'Carlos Ramos', 'dias': 1, 'fichasAtendidas': 7, 'promedioDiario': 7.0, 'bajoMeta': false},
          {'medicoId': 2, 'nombreMedico': 'Juan Mamani', 'dias': 2, 'fichasAtendidas': 9, 'promedioDiario': 4.5, 'bajoMeta': true},
        ],
        '/api/v1/admin/reportes/indicadores': {
          'solicitudes': 86,
          'sinCupo': 3,
          'canceladas': 9,
          'canceladasAutomaticas': 4,
          'fichasPorMedicoDia': 5.5,
          'minutosConfirmacionFin': 38.4,
          'atencionesMedidas': 20,
        },
      }).reporte(TipoReporte.porMedico);

      expect(r.titulo, 'Fichas atendidas por día (meta mínima 5, máximo 7)');
      expect(r.filas.map((f) => f.texto), ['7 fichas', '4.5 fichas · bajo la meta']);
      expect(r.filas.last.alerta, isTrue);
      expect(r.indicadores.map((i) => i.valor),
          ['86 solicitudes', '9 (4 automáticas)', '5.5 fichas/médico/día', '3 solicitudes']);
      expect(r.nota, contains('38 min'));
      expect(pedidas.first.queryParameters, {'desde': '2026-11-23', 'hasta': '2026-11-27'});
    });

    test('por médico sin datos: no inventa la carga de trabajo (CU-20 3a)', () async {
      final r = await servicio({
        '/api/v1/admin/reportes/cumplimiento': [],
        '/api/v1/admin/reportes/indicadores': {
          'solicitudes': 0,
          'sinCupo': 0,
          'canceladas': 0,
          'canceladasAutomaticas': 0,
          'fichasPorMedicoDia': null,
          'minutosConfirmacionFin': null,
        },
      }).reporte(TipoReporte.porMedico);

      expect(r.filas, isEmpty);
      expect(r.indicadores[2].valor, 'Sin datos');
      expect(r.nota, isNot(contains('min)')));
    });

    test('por estado: porcentajes y canceladas automáticas', () async {
      final r = await servicio({
        '/api/v1/admin/reportes/estado': [
          {'estado': 'PROGRAMADA', 'cantidad': 8, 'automaticas': 0},
          {'estado': 'ASISTENCIA_CONFIRMADA', 'cantidad': 8, 'automaticas': 0},
          {'estado': 'ATENDIDA', 'cantidad': 61, 'automaticas': 0},
          {'estado': 'CANCELADA', 'cantidad': 9, 'automaticas': 4},
        ],
      }).reporte(TipoReporte.porEstado);

      expect(r.filas.map((f) => f.etiqueta), ['Atendidas', 'Asist. confirmada', 'Programadas', 'Canceladas']);
      expect(r.filas.last.texto, '9 fichas (4 automáticas)');
      expect(r.indicadores.map((i) => i.valor), ['86 fichas', '71 %', '10 %', '4']);
    });

    test('por especialidad: nombres y la más pedida', () async {
      final r = await servicio({
        '/api/v1/admin/reportes/especialidad': [
          {'grupo': 'CONSULTORIO_FAMILIAR', 'cantidad': 41},
          {'grupo': 'ODONTOLOGIA', 'cantidad': 19},
          {'grupo': 'PEDIATRIA', 'cantidad': 26},
        ],
        '/api/v1/admin/reportes/indicadores': {'solicitudes': 86, 'sinCupo': 3, 'canceladas': 0, 'canceladasAutomaticas': 0},
      }).reporte(TipoReporte.porEspecialidad);

      expect(r.filas.map((f) => f.etiqueta), ['Consultorio Familiar', 'Pediatría', 'Odontología']);
      expect(r.indicadores.map((i) => i.valor), ['86 solicitudes', '3 solicitudes', 'Consultorio Familiar', 'Odontología']);
    });

    test('por consultorio', () async {
      final r = await servicio({
        '/api/v1/admin/reportes/consultorio': [
          {'grupo': 'Consultorio 1', 'cantidad': 9},
          {'grupo': 'Consultorio 4', 'cantidad': 22},
        ],
      }).reporte(TipoReporte.porConsultorio);

      expect(r.indicadores.map((i) => i.valor), ['2', 'Consultorio 4', '31 fichas', '15.5']);
    });

    test('solo fichas internas y quirófanos sigue con datos de prueba', () {
      final s = servicio(const {});
      expect(s.esDePrueba(TipoReporte.porMedico), isFalse);
      expect(s.esDePrueba(TipoReporte.internasYQuirofanos), isTrue);
    });

    test('un error del backend llega como ApiException', () async {
      final s = ApiReportesService(
        ApiClient(
            cliente: MockClient((_) async => _json({'codigo': 'ACCESO_DENEGADO', 'mensaje': 'Solo el administrador'}, 403)),
            urlBase: 'http://cns.test:8080'),
        respaldo: FakeReportesService(reloj: _reloj),
        reloj: _reloj,
      );
      await expectLater(s.reporte(TipoReporte.porEstado),
          throwsA(isA<ApiException>().having((e) => e.codigo, 'codigo', 'ACCESO_DENEGADO')));
    });
  });
}

/// Servicio que cuenta cuantas veces se piden las citas comunes.
class _ServicioContado extends _SinBackend {
  _ServicioContado(this._cambios, this._alPedir);

  final Stream<void> _cambios;
  final void Function() _alPedir;

  @override
  bool get citasDePrueba => false;

  @override
  Stream<void> cambios() => _cambios;

  @override
  Future<ResumenCitas> citasComunes({Especialidad? especialidad}) async {
    _alPedir();
    return const ResumenCitas(fichasHoy: 0, atendidas: 0, confirmadas: 0, canceladas: 0, filas: []);
  }
}
