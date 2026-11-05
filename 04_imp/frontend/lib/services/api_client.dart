import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/error_dto.dart';

class ApiException implements Exception {
  const ApiException(this.error, {this.statusCode});

  final ErrorDto error;
  final int? statusCode;

  @override
  String toString() => '${error.codigo}: ${error.mensaje}';
}

class ApiClient {
  ApiClient({
    http.Client? client,
    String? baseUrl,
    this.timeout = const Duration(seconds: 10),
  })  : _client = client ?? http.Client(),
        baseUrl = baseUrl ??
            const String.fromEnvironment(
              'API_BASE_URL',
              defaultValue: 'http://localhost:8080/api/v1',
            );

  final http.Client _client;
  final String baseUrl;
  final Duration timeout;

  Future<Map<String, dynamic>> post(
    String path,
    Map<String, dynamic> body,
  ) async {
    final response = await _request(
      () => _client.post(
        _uri(path),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(body),
      ),
    );
    final decoded = _decode(response);
    if (decoded is! Map<String, dynamic>) {
      throw const ApiException(
        ErrorDto(codigo: 'RESPUESTA_INVALIDA', mensaje: 'Respuesta JSON invalida.'),
      );
    }
    return decoded;
  }

  Future<dynamic> get(String path) async {
    final response = await _request(
      () => _client.get(_uri(path)),
    );
    return _decode(response);
  }

  Uri _uri(String path) {
    final normalizedBase = baseUrl.endsWith('/')
        ? baseUrl.substring(0, baseUrl.length - 1)
        : baseUrl;
    final normalizedPath = path.startsWith('/') ? path : '/$path';
    return Uri.parse('$normalizedBase$normalizedPath');
  }

  Future<http.Response> _request(Future<http.Response> Function() request) async {
    try {
      final response = await request().timeout(timeout);
      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw ApiException(_errorFromResponse(response), statusCode: response.statusCode);
      }
      return response;
    } on ApiException {
      rethrow;
    } on TimeoutException {
      throw const ApiException(
        ErrorDto(codigo: 'RED_TIMEOUT', mensaje: 'El servidor no respondio a tiempo.'),
      );
    } on Exception {
      throw const ApiException(
        ErrorDto(codigo: 'RED_NO_DISPONIBLE', mensaje: 'No se pudo conectar con el servidor.'),
      );
    }
  }

  dynamic _decode(http.Response response) {
    if (response.body.trim().isEmpty) return <String, dynamic>{};
    try {
      return jsonDecode(response.body);
    } on FormatException {
      throw const ApiException(
        ErrorDto(codigo: 'RESPUESTA_INVALIDA', mensaje: 'El servidor devolvio JSON invalido.'),
      );
    }
  }

  ErrorDto _errorFromResponse(http.Response response) {
    try {
      final body = jsonDecode(response.body);
      if (body is Map<String, dynamic>) return ErrorDto.fromJson(body);
    } on FormatException {
      // Se usa el mensaje generico cuando el backend no devuelve ErrorDTO.
    }
    return ErrorDto(
      codigo: 'HTTP_${response.statusCode}',
      mensaje: 'La solicitud fue rechazada por el servidor.',
    );
  }
}