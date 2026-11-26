import 'dart:convert';

import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/enfermeria/ficha_dia.dart';
import 'package:cns_app/models/historial/cambio_cita.dart';
import 'package:cns_app/models/solicitud/solicitud.dart';
import 'package:cns_app/services/citas_paciente_service.dart';
import 'package:cns_app/services/enfermeria_service.dart';
import 'package:cns_app/services/historial_cambios_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

http.Response _json(Object? cuerpo, [int estado = 200]) =>
    http.Response(jsonEncode(cuerpo), estado, headers: {'content-type': 'application/json; charset=utf-8'});

Map<String, dynamic> _citaDto({int id = 7, String estado = 'PROGRAMADA', String hora = '10:00:00'}) => {
      'citaId': id,
      'pacienteId': 120,
      'nombrePaciente': 'Luis Quispe',
      'numeroAfiliado': '4601-03',
      'medicoId': 3,
      'nombreMedico': 'Dr. Ramos',
      'especialidad': 'CONSULTORIO_FAMILIAR',
      'consultorio': 4,
      'fecha': '2026-11-26',
      'hora': hora,
      'numeroFicha': 3,
      'estado': estado,
    };

/// Cliente con token que guarda cada peticion.
(ApiClient, List<http.Request>) _cliente(http.Response Function(http.Request) responder) {
  final peticiones = <http.Request>[];
  final api = ApiClient(
    urlBase: 'http://api',
    cliente: MockClient((r) async {
      peticiones.add(r);
      return responder(r);
    }),
  )..token = 'tok';
  return (api, peticiones);
}

void main() {
  group('ApiCitasPacienteService (CU-08/CU-10)', () {
    test('misCitas lee la lista con el token', () async {
      final (api, peticiones) = _cliente((_) => _json([_citaDto(), _citaDto(id: 8, estado: 'ATENDIDA')]));

      final citas = await ApiCitasPacienteService(api).misCitas();

      expect(peticiones.single.url.path, '/api/v1/citas/mias');
      expect(peticiones.single.headers['Authorization'], 'Bearer tok');
      expect(citas.map((c) => c.citaId), [7, 8]);
      expect(citas.first.inicio, DateTime(2026, 11, 26, 10));
      expect(citas.first.especialidad, Especialidad.consultorioFamiliar);
      expect(citas.first.limiteConfirmacion, DateTime(2026, 11, 26, 9, 30));
      expect(citas.last.estado, EstadoCita.atendida);
      expect(citas.last.sePuedeModificar, isFalse);
    });

    test('reprogramar devuelve la nueva solicitud', () async {
      final (api, peticiones) = _cliente((_) => _json({
            'solicitudId': 40,
            'estado': 'PENDIENTE',
            'mensaje': 'Su solicitud fue registrada.',
            'especialidad': 'ODONTOLOGIA',
            'diaSolicitado': '2026-11-27',
            'prioridad': 2,
            'ordenLlegada': 99,
            'citaId': null,
          }));

      final s = await ApiCitasPacienteService(api).reprogramar(7);

      expect(peticiones.single.method, 'POST');
      expect(peticiones.single.url.path, '/api/v1/citas/7/reprogramar');
      expect(s.prioridad, 2);
      expect(s.estado, EstadoSolicitudApp.pendiente);
      expect(s.diaSolicitado, DateTime(2026, 11, 27));
    });

    test('el rechazo del backend llega como ApiException con su codigo', () async {
      final (api, _) = _cliente((_) =>
          _json({'codigo': 'ESTADO_NO_PERMITE', 'mensaje': 'Solo se puede reprogramar una cita PROGRAMADA'}, 409));

      expect(
        () => ApiCitasPacienteService(api).reprogramar(7),
        throwsA(isA<ApiException>().having((e) => e.codigo, 'codigo', 'ESTADO_NO_PERMITE')),
      );
    });
  });

  group('ApiEnfermeriaService (CU-09/CU-12)', () {
    test('fichasDeHoy convierte las citas y las ordena por hora', () async {
      final (api, peticiones) =
          _cliente((_) => _json([_citaDto(id: 2, hora: '11:00:00'), _citaDto(id: 1, hora: '08:30:00')]));

      final fichas = await ApiEnfermeriaService(api).fichasDeHoy();

      expect(peticiones.single.url.path, '/api/v1/fichas/hoy');
      expect(fichas.map((f) => f.citaId), [1, 2]);
      expect(fichas.first.paciente, 'Luis Quispe');
      expect(fichas.first.consultorio, '4');
    });

    test('confirmar llegada no envia el usuario: lo saca el backend del token', () async {
      var confirmada = false;
      final (api, peticiones) = _cliente((r) {
        if (r.method == 'POST') {
          confirmada = true;
          return _json({'resultado': 'CONFIRMADA', 'mensaje': 'Asistencia confirmada'});
        }
        return _json([_citaDto(estado: confirmada ? 'ASISTENCIA_CONFIRMADA' : 'PROGRAMADA')]);
      });

      final ficha = await ApiEnfermeriaService(api).confirmarLlegada(7, usuario: 'Enfermería · M. Flores');

      final post = peticiones.firstWhere((p) => p.method == 'POST');
      expect(post.url.path, '/api/v1/citas/7/confirmar-asistencia');
      expect(post.body, '{}');
      expect(ficha.estado, EstadoCita.asistenciaConfirmada);
    });

    test('FUERA_DE_PLAZO llega con su mensaje', () async {
      final (api, _) = _cliente((_) => _json({'codigo': 'FUERA_DE_PLAZO', 'mensaje': 'Pasó el plazo'}, 422));

      expect(
        () => ApiEnfermeriaService(api).confirmarLlegada(7, usuario: 'x'),
        throwsA(isA<ApiException>().having((e) => e.codigo, 'codigo', 'FUERA_DE_PLAZO')),
      );
    });
  });

  group('ApiHistorialCambiosService (CU-18)', () {
    final fila = {
      'cambioId': 1,
      'citaId': 7,
      'numeroFicha': 3,
      'medico': 'Dr. Ramos',
      'paciente': 'Luis Quispe',
      'fechaCita': '2026-11-04',
      'accion': 'CANCELADA',
      'usuario': 'Sistema',
      'fechaHora': '2026-11-04T09:30:00',
      'motivo': 'No confirmó asistencia',
    };

    test('envia los filtros y traduce la accion al texto del Figma', () async {
      final (api, peticiones) = _cliente((_) => _json([fila]));

      final cambios = await ApiHistorialCambiosService(api).buscar(
          FiltroCambios(cita: '#7', usuario: ' sis ', desde: DateTime(2026, 11, 1), hasta: DateTime(2026, 11, 5)));

      expect(peticiones.single.url.path, '/api/v1/admin/cambios');
      expect(peticiones.single.url.queryParameters,
          {'cita': '7', 'usuario': 'sis', 'desde': '2026-11-01', 'hasta': '2026-11-05'});
      expect(cambios.single.accion, 'Cancelada (automática)');
      expect(cambios.single.cita, 'Ficha 3 · Dr. Ramos (#7)');
      expect(cambios.single.fecha, DateTime(2026, 11, 4, 9, 30));
    });

    test('"Cita" con texto (no numero) filtra en la app', () async {
      final otra = {...fila, 'cambioId': 2, 'citaId': 8, 'numeroFicha': 5, 'medico': 'Dra. Quispe'};
      final (api, peticiones) = _cliente((_) => _json([fila, otra]));

      final cambios = await ApiHistorialCambiosService(api).buscar(const FiltroCambios(cita: 'quispe'));

      expect(peticiones.single.url.queryParameters.containsKey('cita'), isFalse);
      expect(cambios.single.cita, contains('Ficha 5'));
    });

    test('textoAccion: confirmacion, atencion y cancelacion por paciente', () {
      expect(textoAccion('ASISTENCIA_CONFIRMADA', 'Enfermería · M. Flores'), 'Asistencia confirmada');
      expect(textoAccion('ATENDIDA', 'Médico · Dr. Ramos'), 'Atención finalizada');
      expect(textoAccion('CANCELADA', 'Paciente · Ana'), 'Cancelada por paciente');
      expect(textoAccion('CREADA', 'SISTEMA'), 'Cita registrada');
    });
  });
}
