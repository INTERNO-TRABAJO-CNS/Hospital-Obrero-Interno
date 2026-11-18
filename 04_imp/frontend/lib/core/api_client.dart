import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

/// Error devuelto por el backend ({codigo, mensaje}) o por la conexion.
class ApiException implements Exception {
  const ApiException(this.codigo, this.mensaje, {this.estado});

  /// Codigo del backend (ej. NO_ENCONTRADA) o SIN_CONEXION / SIN_RESPUESTA / ERROR_HTTP / RESPUESTA_INVALIDA.
  final String codigo;
  final String mensaje;

  /// Estado HTTP, si hubo respuesta.
  final int? estado;

  @override
  String toString() => 'ApiException($codigo, $mensaje)';
}

/// Convierte una respuesta con [convertir]. Si el JSON no tiene la forma esperada (por ejemplo un
/// rol o una especialidad que esta version no conoce) lanza RESPUESTA_INVALIDA en vez de adivinar.
T leerRespuesta<T>(T Function() convertir) {
  try {
    return convertir();
  } on FormatException catch (e) {
    throw ApiException('RESPUESTA_INVALIDA', 'El servidor envió datos que esta versión no reconoce (${e.message}).');
  }
}

/// Cliente HTTP unico de la app. Todas las pantallas llaman al backend por aqui.
///
/// La URL base se cambia al ejecutar: `flutter run --dart-define=API_URL=http://192.168.1.10:8080`.
/// (En el emulador de Android, localhost es `http://10.0.2.2:8080`.)
class ApiClient {
  ApiClient({http.Client? cliente, String? urlBase})
      : _cliente = cliente ?? http.Client(),
        urlBase = _sinBarraFinal(urlBase ?? urlBaseConfigurada);

  static const String urlBaseConfigurada = String.fromEnvironment('API_URL', defaultValue: 'http://localhost:8080');

  static const Duration _espera = Duration(seconds: 15);

  final http.Client _cliente;
  final String urlBase;

  /// Token de la sesion (lo entrega `POST /api/v1/auth/login`). Viaja como `Authorization: Bearer`.
  String? token;

  /// Se llama cuando una peticion que llevaba token recibe 401 (sesion vencida o cerrada):
  /// la app olvida la sesion y vuelve al login.
  void Function(ApiException error)? alPerderSesion;

  Future<Map<String, dynamic>> get(String ruta, {Map<String, String>? consulta}) {
    return _enviar(() => _cliente.get(_uri(ruta, consulta), headers: _cabeceras()));
  }

  Future<Map<String, dynamic>> post(String ruta, [Map<String, dynamic>? cuerpo]) {
    return _enviar(() => _cliente.post(
          _uri(ruta),
          headers: _cabeceras(),
          body: jsonEncode(cuerpo ?? <String, dynamic>{}),
        ));
  }

  Future<Map<String, dynamic>> put(String ruta, [Map<String, dynamic>? cuerpo]) {
    return _enviar(() => _cliente.put(
          _uri(ruta),
          headers: _cabeceras(),
          body: jsonEncode(cuerpo ?? <String, dynamic>{}),
        ));
  }

  Future<Map<String, dynamic>> delete(String ruta) {
    return _enviar(() => _cliente.delete(_uri(ruta), headers: _cabeceras()));
  }

  Map<String, String> _cabeceras() => {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  Uri _uri(String ruta, [Map<String, String>? consulta]) {
    final camino = ruta.startsWith('/') ? ruta : '/$ruta';
    final uri = Uri.parse('$urlBase$camino');
    if (consulta == null || consulta.isEmpty) {
      return uri;
    }
    return uri.replace(queryParameters: consulta);
  }

  Future<Map<String, dynamic>> _enviar(Future<http.Response> Function() peticion) async {
    final conToken = token != null;
    final http.Response respuesta;
    try {
      respuesta = await peticion().timeout(_espera);
    } on TimeoutException {
      throw const ApiException('SIN_RESPUESTA', 'El servidor no respondió a tiempo');
    } catch (_) {
      throw const ApiException('SIN_CONEXION', 'No se pudo conectar con el servidor');
    }
    try {
      return _procesar(respuesta);
    } on ApiException catch (e) {
      if (e.estado == 401 && conToken) {
        alPerderSesion?.call(e);
      }
      rethrow;
    }
  }

  Map<String, dynamic> _procesar(http.Response respuesta) {
    final texto = utf8.decode(respuesta.bodyBytes);
    dynamic datos;
    if (texto.isNotEmpty) {
      try {
        datos = jsonDecode(texto);
      } on FormatException {
        datos = null;
      }
    }

    final exito = respuesta.statusCode >= 200 && respuesta.statusCode < 300;
    if (exito) {
      if (datos is Map) {
        return Map<String, dynamic>.from(datos);
      }
      if (datos is List) {
        return {'datos': datos};
      }
      return <String, dynamic>{};
    }

    final cuerpo = datos is Map ? datos : const {};
    throw ApiException(
      cuerpo['codigo']?.toString() ?? 'ERROR_HTTP',
      cuerpo['mensaje']?.toString() ?? 'Error ${respuesta.statusCode}',
      estado: respuesta.statusCode,
    );
  }

  static String _sinBarraFinal(String url) => url.endsWith('/') ? url.substring(0, url.length - 1) : url;
}
