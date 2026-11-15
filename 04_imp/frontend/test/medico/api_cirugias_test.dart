import 'dart:convert';

import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/medico/cirugia.dart';
import 'package:cns_app/models/medico/ficha_interna.dart';
import 'package:cns_app/models/medico/medico_actual.dart';
import 'package:cns_app/models/quirofano/quirofano.dart';
import 'package:cns_app/services/cirugias_service.dart';
import 'package:cns_app/services/quirofano_service.dart';
import 'package:flutter/material.dart' show TimeOfDay;
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

http.Response _json(Object? cuerpo, int estado) =>
    http.Response(jsonEncode(cuerpo), estado, headers: {'content-type': 'application/json; charset=utf-8'});

Map<String, dynamic> _cirugia(int numero, {String inicio = '10:00'}) => {
      'numero': numero,
      'fichaTicket': 1024,
      'pacienteId': 113,
      'nombrePaciente': 'Marco Ríos',
      'numeroAfiliado': 'AF-10003',
      'edad': 24,
      'especialidad': 'GASTROENTEROLOGIA',
      'quirofano': 'G-1',
      'cirujano': 'Raul Vargas',
      'fecha': '2026-11-17',
      'inicio': inicio,
      'fin': '11:30',
      'limpiezaHasta': '12:10',
      'duracionMin': 90,
      'urgente': false,
      'gravedad': 'BAJA',
      'estado': 'PROGRAMADA',
    };

const _vargas = MedicoActual(
    medicoId: 17,
    nombre: 'Raul Vargas',
    especialidad: Especialidad.gastroenterologia,
    puedeDerivar: false,
    operaEnQuirofano: true);

void main() {
  test('proponer lee la propuesta con su desplazamiento y confirmar devuelve lo que vio el especialista', () async {
    final enviadas = <http.Request>[];
    final s = ApiCirugiasService(ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((p) async {
        enviadas.add(p);
        if (p.url.path.endsWith('/propuesta')) {
          return _json({
            'quirofanoId': 3,
            'quirofano': 'G-2',
            'especialidad': 'GASTROENTEROLOGIA',
            'fecha': '2026-11-16',
            'inicio': '12:30',
            'fin': '13:30',
            'limpiezaHasta': '14:00',
            'limpiezaMin': 30,
            'desplaza': {'cirugia': _cirugia(57, inicio: '12:30'), 'nuevaFecha': '2026-11-17', 'nuevoInicio': '07:00'},
          }, 200);
        }
        return _json(_cirugia(58, inicio: '12:30'), 201);
      }),
    ));
    const datos = DatosCirugia(fichaTicket: 1024, duracionMin: 60, gravedad: Gravedad.alta, urgente: true);

    final p = await s.proponer(datos, _vargas);
    expect(p.quirofanoId, 3);
    expect(p.desplaza!.cirugia.numero, 57);

    final c = await s.confirmar(datos, p, _vargas);

    expect(c.numero, 58);
    expect(jsonDecode(enviadas.last.body), {
      'fichaTicket': 1024,
      'duracionMin': 60,
      'gravedad': 'ALTA',
      'urgente': true,
      'quirofanoId': 3,
      'fecha': '2026-11-16',
      'inicio': '12:30',
      'desplazaNumero': 57,
      'desplazaFecha': '2026-11-17',
      'desplazaInicio': '07:00',
    });
  });

  test('el choque al confirmar llega con su código para pedir una nueva propuesta', () async {
    final s = ApiCirugiasService(ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((_) async => _json({'codigo': 'CHOQUE_DE_HORARIO', 'mensaje': 'Otro especialista…'}, 409)),
    ));
    const datos = DatosCirugia(fichaTicket: 1024, duracionMin: 60, gravedad: Gravedad.alta, urgente: false);
    const p = PropuestaCirugia(
        quirofano: 'G-1',
        especialidad: Especialidad.gastroenterologia,
        fecha: '2026-11-17',
        inicio: '07:00',
        fin: '08:00',
        limpiezaHasta: '08:40',
        limpiezaMin: 40,
        quirofanoId: 1);

    expect(
        () => s.confirmar(datos, p, _vargas),
        throwsA(isA<ApiException>()
            .having((e) => e.codigo, 'codigo', 'CHOQUE_DE_HORARIO')
            .having((e) => e.estado, 'estado', 409)));
  });

  test('quirófanos del admin: crear manda POST y editar manda PUT', () async {
    final metodos = <String>[];
    final s = ApiQuirofanoService(ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((p) async {
        metodos.add('${p.method} ${p.url.path}');
        return _json({
          'quirofanoId': 5,
          'nombre': 'D-2',
          'especialidad': 'DERMATOLOGIA',
          'apertura': '08:00',
          'cierre': '16:00',
          'limpiezaMin': 30,
          'activo': true,
          'cirugiasProgramadas': 0,
        }, 200);
      }),
    ));
    const nuevo = Quirofano(
        nombre: 'D-2',
        especialidad: Especialidad.dermatologia,
        apertura: TimeOfDay(hour: 8, minute: 0),
        cierre: TimeOfDay(hour: 16, minute: 0),
        limpiezaMin: 30);

    final creado = await s.guardar(nuevo);
    await s.guardar(creado);

    expect(creado.id, 5);
    expect(creado.horario, '08:00 – 16:00');
    expect(metodos, ['POST /api/v1/admin/quirofanos', 'PUT /api/v1/admin/quirofanos/5']);
  });
}
