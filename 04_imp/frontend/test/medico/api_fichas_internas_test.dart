import 'dart:convert';

import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/medico/ficha_interna.dart';
import 'package:cns_app/models/medico/medico_actual.dart';
import 'package:cns_app/services/fichas_internas_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

http.Response _json(Object? cuerpo, int estado) =>
    http.Response(jsonEncode(cuerpo), estado, headers: {'content-type': 'application/json; charset=utf-8'});

/// Una ficha como la devuelve el backend (FichaInternaVistaDTO).
Map<String, dynamic> _ficha({String? gravedad = 'MEDIA', String estado = 'ASIGNADA'}) => {
      'ticket': 1033,
      'citaOrigenId': 503,
      'pacienteId': 102,
      'nombrePaciente': 'Elena Copa',
      'numeroAfiliado': 'AF-10006',
      'edad': 69,
      'especialidad': 'GASTROENTEROLOGIA',
      'tabla': 'GENERAL',
      'urgencia': false,
      'gravedad': gravedad,
      'motivo': 'Dolor abdominal',
      'estado': estado,
      'fecha': '2026-11-17',
      'hora': '08:00',
      'duracionMin': 25,
      'medico': 'Raul Vargas',
      'consultorio': 'Consultorio 12',
      'derivadoPor': 'Carlos Ramos',
      'especialidadOrigen': 'CONSULTORIO_FAMILIAR',
      'llegada': '2026-11-16T09:31:12',
      'observaciones': null,
    };

const _medico = MedicoActual(
    medicoId: 19,
    nombre: 'Carlos Ramos',
    especialidad: Especialidad.consultorioFamiliar,
    puedeDerivar: true,
    operaEnQuirofano: false);

const _solicitud = SolicitudFicha(
  citaOrigenId: 503,
  pacienteId: 102,
  nombrePaciente: 'Elena Copa',
  numeroAfiliado: 'AF-10006',
  edad: 69,
  especialidad: Especialidad.gastroenterologia,
  urgencia: false,
  gravedad: Gravedad.media,
  motivo: 'Dolor abdominal',
);

const _cita = CitaOrigen(citaId: 503, numeroFicha: 3, fecha: '2026-11-16', hora: '09:30:00', estado: 'ATENDIDA');

ApiFichasInternasService _servicio(Future<http.Response> Function(http.Request p) responder) =>
    ApiFichasInternasService(ApiClient(urlBase: 'http://servidor:8080', cliente: MockClient(responder)));

void main() {
  test('generar envía la cita (no el paciente) y lee la ficha asignada', () async {
    late http.Request enviada;
    final s = _servicio((p) async {
      enviada = p;
      return _json(_ficha(), 201);
    });

    final r = await s.generar(_solicitud, _medico, _cita);

    expect(enviada.url.path, '/api/v1/medico/fichas-internas');
    expect(jsonDecode(enviada.body), {
      'citaOrigenId': 503,
      'pacienteId': null,
      'especialidad': 'GASTROENTEROLOGIA',
      'gravedad': 'MEDIA',
      'urgencia': false,
      'motivo': 'Dolor abdominal',
    });
    expect(r.rechazada, isFalse);
    expect(r.ficha!.ticket, 1033);
    expect(r.ficha!.consultorio, 'Consultorio 12');
    expect(r.ficha!.llegada, DateTime(2026, 11, 16, 9, 31, 12));
  });

  test('un rechazo del backend (422 con código) se muestra como «Ticket rechazado»', () async {
    final s = _servicio((_) async => _json(
        {'codigo': 'PACIENTE_CON_FICHA_ACTIVA', 'mensaje': 'El paciente ya tiene una ficha interna activa'}, 409));

    final r = await s.generar(_solicitud, _medico, _cita);

    expect(r.rechazada, isTrue);
    expect(r.codigo, 'PACIENTE_CON_FICHA_ACTIVA');
  });

  test('sin conexión no es un rechazo: el error sube', () async {
    final s = _servicio((_) async => throw http.ClientException('caido'));

    expect(() => s.generar(_solicitud, _medico, _cita), throwsA(isA<ApiException>()));
  });

  test('una cita sin derivación (404) es null; el especialista recibe la ficha sin gravedad', () async {
    final s = _servicio((p) async {
      if (p.url.path.contains('por-cita')) {
        return _json({'codigo': 'NO_ENCONTRADA', 'mensaje': 'No'}, 404);
      }
      return _json([_ficha(gravedad: null)], 200);
    });

    expect(await s.deCita(504), isNull);
    final agenda = await s.agendaEspecialista(_medico, '2026-11-17');
    expect(agenda.single.gravedad, isNull);
  });

  test('buscar por afiliado devuelve titular y beneficiarios', () async {
    final s = _servicio((p) async => _json([
          {'pacienteId': 1, 'nombre': 'Juan', 'numeroAfiliado': 'AF-10003', 'edad': 40, 'titular': true},
          {'pacienteId': 2, 'nombre': 'Sofia', 'numeroAfiliado': 'AF-10003', 'edad': 6, 'titular': false},
        ], 200));

    final r = await s.buscarPacientes('AF-10003');

    expect(r.map((p) => p.nombre), ['Juan', 'Sofia']);
  });
}
