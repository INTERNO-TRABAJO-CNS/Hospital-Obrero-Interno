import 'dart:async';

import 'package:cns_app/controllers/notificaciones_controller.dart';
import 'package:cns_app/models/notificacion/notificacion_dto.dart';
import 'package:cns_app/services/notificaciones_service.dart';
import 'package:cns_app/views/paciente/notificaciones_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

class StreamingNotificacionesService implements NotificacionesService {
  StreamingNotificacionesService(this.iniciales);

  final List<NotificacionDto> iniciales;
  final StreamController<NotificacionDto> streamController =
      StreamController<NotificacionDto>.broadcast();

  @override
  Future<List<NotificacionDto>> delPaciente(int pacienteId) async => iniciales;

  @override
  Stream<NotificacionDto> escuchar(int pacienteId) => streamController.stream;

  Future<void> dispose() => streamController.close();
}

void main() {
  testWidgets('muestra la bandeja de notificaciones', (tester) async {
    final service = StreamingNotificacionesService([
      NotificacionDto(
        pacienteId: 7,
        citaId: 8,
        tipo: 'CITA_REGISTRADA',
        mensaje: 'Su cita fue registrada.',
        creadaEn: DateTime(2026, 11, 10),
      ),
    ]);
    final controller = NotificacionesController(service, 7);

    await tester.pumpWidget(
      MaterialApp(home: NotificacionesView(controller: controller)),
    );
    await tester.pumpAndSettle();

    expect(find.text('Su cita fue registrada.'), findsOneWidget);
    expect(find.text('CITA_REGISTRADA'), findsOneWidget);
    expect(find.text('1'), findsOneWidget);
    await service.dispose();
  });

  testWidgets('muestra snackbar cuando llega una notificacion', (tester) async {
    final service = StreamingNotificacionesService([]);
    final controller = NotificacionesController(service, 7);

    await tester.pumpWidget(
      MaterialApp(home: NotificacionesView(controller: controller)),
    );
    await tester.pumpAndSettle();

    service.streamController.add(
      NotificacionDto(
        pacienteId: 7,
        citaId: 9,
        tipo: 'AVISO_T45',
        mensaje: 'Su cita sera cancelada.',
        creadaEn: DateTime(2026, 11, 10),
      ),
    );
    await tester.pump(const Duration(milliseconds: 100));

    expect(find.text('Su cita sera cancelada.'), findsWidgets);
    expect(find.byKey(const Key('notificacion-9-0')), findsOneWidget);
    await service.dispose();
  });
}