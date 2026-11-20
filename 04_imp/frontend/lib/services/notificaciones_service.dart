import 'dart:async';
import 'dart:convert';

import 'package:web_socket_channel/web_socket_channel.dart';

import '../models/notificacion/notificacion_dto.dart';
import '../core/api_client.dart';

abstract interface class NotificacionesService {
  Future<List<NotificacionDto>> delPaciente(int pacienteId);

  Stream<NotificacionDto> escuchar(int pacienteId);
}

class RestNotificacionesService implements NotificacionesService {
  RestNotificacionesService(this._client, {String? websocketBaseUrl})
      : _websocketBaseUrl = websocketBaseUrl ??
            const String.fromEnvironment(
              'WS_BASE_URL',
              defaultValue: 'ws://localhost:8080',
            );

  final ApiClient _client;
  final String _websocketBaseUrl;

  @override
  Future<List<NotificacionDto>> delPaciente(int pacienteId) async {
    final json = await _client.get('/api/v1/notificaciones?pacienteId=$pacienteId');
    final datos = json['datos'];
    if (datos is! List<dynamic>) {
      throw const ApiException(
        'RESPUESTA_INVALIDA',
        'Lista de notificaciones invalida.',
      );
    }
    return datos
        .whereType<Map<String, dynamic>>()
        .map(NotificacionDto.fromJson)
        .toList();
  }

  @override
  Stream<NotificacionDto> escuchar(int pacienteId) {
    final base = _websocketBaseUrl.endsWith('/')
        ? _websocketBaseUrl.substring(0, _websocketBaseUrl.length - 1)
        : _websocketBaseUrl;
    final channel = WebSocketChannel.connect(
      Uri.parse('$base/ws/notificaciones?pacienteId=$pacienteId'),
    );
    return channel.stream.map((event) {
      final json = jsonDecode(event as String);
      if (json is! Map<String, dynamic>) {
        throw const FormatException('Notificacion WebSocket invalida.');
      }
      return NotificacionDto.fromJson(json);
    });
  }
}

class FakeNotificacionesService implements NotificacionesService {
  FakeNotificacionesService({List<NotificacionDto>? iniciales})
      : _iniciales = iniciales ?? <NotificacionDto>[];

  final List<NotificacionDto> _iniciales;

  @override
  Future<List<NotificacionDto>> delPaciente(int pacienteId) async {
    return List.unmodifiable(_iniciales);
  }

  @override
  Stream<NotificacionDto> escuchar(int pacienteId) => const Stream.empty();
}