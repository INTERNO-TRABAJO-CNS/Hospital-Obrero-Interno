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
  test('envia el token como Authorization: Bearer y arma la URL', () async {
    late http.BaseRequest enviada;
    final api = ApiClient(
      urlBase: 'http://servidor:8080/',
      cliente: MockClient((peticion) async {
        enviada = peticion;
        return _json({'ok': true}, 200);
      }),
    )..token = 'abc123';

    final respuesta = await api.get('/api/citas', consulta: {'fecha': '2026-11-09'});

    expect(respuesta['ok'], true);
    expect(enviada.url.toString(), 'http://servidor:8080/api/citas?fecha=2026-11-09');
    expect(enviada.headers['Authorization'], 'Bearer abc123');
    expect(enviada.headers.keys.where((k) => k.toLowerCase().startsWith('x-')), isEmpty,
        reason: 'los headers X-Usuario-Id/X-Rol/X-Persona-Id ya no existen');
  });

  test('sin sesion no manda Authorization', () async {
    late http.BaseRequest enviada;
    final api = ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((peticion) async {
        enviada = peticion;
        return _json({'ok': true}, 200);
      }),
    );

    await api.post('/api/v1/auth/login', {'usuario': 'x'});

    expect(enviada.headers.containsKey('Authorization'), isFalse);
  });

  test('un 401 con token avisa que la sesion se perdio', () async {
    ApiException? perdida;
    final api = ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((_) async => _json({'codigo': 'SESION_VENCIDA', 'mensaje': 'La sesion vencio'}, 401)),
    )
      ..token = 'viejo'
      ..alPerderSesion = (e) => perdida = e;

    await expectLater(api.get('/api/v1/medico/yo'), throwsA(isA<ApiException>()));

    expect(perdida?.codigo, 'SESION_VENCIDA');
  });

  test('un 401 sin token (login fallido) no cuenta como sesion perdida', () async {
    var avisos = 0;
    final api = ApiClient(
      urlBase: 'http://servidor:8080',
      cliente: MockClient((_) async => _json({'codigo': 'CREDENCIALES_INVALIDAS', 'mensaje': 'x'}, 401)),
    )..alPerderSesion = (_) => avisos++;

    await expectLater(api.post('/api/v1/auth/login'), throwsA(isA<ApiException>()));

    expect(avisos, 0);
  });

  test('leerRespuesta convierte un dato desconocido en RESPUESTA_INVALIDA', () {
    expect(
      () => leerRespuesta(() => throw const FormatException('rol desconocido "X"')),
      throwsA(isA<ApiException>().having((e) => e.codigo, 'codigo', 'RESPUESTA_INVALIDA')),
    );
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
