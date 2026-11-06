import 'dart:convert';

import 'package:cns_app/core/api_client.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

http.Response _json(Object cuerpo, int estado) => http.Response(
      jsonEncode(cuerpo),
      estado,
      headers: {'content-type': 'application/json; charset=utf-8'},
    );

void main() {
  test('envia los headers temporales de usuario y arma la URL', () async {
    late http.BaseRequest enviada;
    final api = ApiClient(
      urlBase: 'http://servidor:8080/',
      cliente: MockClient((peticion) async {
        enviada = peticion;
        return _json({'ok': true}, 200);
      }),
    )
      ..usuarioId = 7
      ..rol = 'PACIENTE'
      ..personaId = 42;

    final respuesta = await api.get('/api/citas', consulta: {'fecha': '2026-11-09'});

    expect(respuesta['ok'], true);
    expect(enviada.url.toString(), 'http://servidor:8080/api/citas?fecha=2026-11-09');
    expect(enviada.headers['X-Usuario-Id'], '7');
    expect(enviada.headers['X-Rol'], 'PACIENTE');
    expect(enviada.headers['X-Persona-Id'], '42');
  });

  test('una lista JSON se devuelve dentro de la clave datos', () async {
    final api = ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((_) async => _json([1, 2, 3], 200)),
    );

    final respuesta = await api.get('/api/citas');

    expect(respuesta['datos'], [1, 2, 3]);
  });

  test('un error del backend se convierte en ApiException(codigo, mensaje)', () async {
    final api = ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient(
        (_) async => _json({'codigo': 'NO_ENCONTRADA', 'mensaje': 'La cita no existe'}, 404),
      ),
    );

    expect(
      () => api.post('/api/citas/9/cancelar'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.codigo, 'codigo', 'NO_ENCONTRADA')
            .having((e) => e.mensaje, 'mensaje', 'La cita no existe')
            .having((e) => e.estado, 'estado', 404),
      ),
    );
  });

  test('si no hay conexion lanza SIN_CONEXION', () async {
    final api = ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((_) async => throw http.ClientException('sin red')),
    );

    expect(
      () => api.get('/api/citas'),
      throwsA(isA<ApiException>().having((e) => e.codigo, 'codigo', 'SIN_CONEXION')),
    );
  });
}
