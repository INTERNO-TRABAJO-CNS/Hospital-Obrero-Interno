import 'package:cns_app/controllers/solicitud_controller.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/solicitud/solicitud.dart';
import 'package:cns_app/services/cancelacion_service.dart';
import 'package:cns_app/services/solicitud_service.dart';
import 'package:cns_app/views/paciente/mis_citas_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

class _SolicitudServiceFake implements SolicitudService {
  _SolicitudServiceFake(this.solicitudes);

  final List<ResultadoSolicitud> solicitudes;

  @override
  Future<List<ResultadoSolicitud>> listar() async => solicitudes;

  @override
  Future<ResultadoSolicitud> consultar(int solicitudId) async =>
      throw UnimplementedError();

  @override
  Future<ResultadoSolicitud> solicitar(Especialidad especialidad) async =>
      throw UnimplementedError();
}

class _CancelacionServiceFake implements CancelacionService {
  @override
  Future<void> cancelar(int citaId) async {}
}

void main() {
  testWidgets('muestra la cita cancelada y oculta la acción de cancelar', (tester) async {
    final controller = SolicitudController(
      service: _SolicitudServiceFake([
        ResultadoSolicitud.fromJson({
          'solicitudId': 17,
          'estado': 'CANCELADA',
          'mensaje': 'La cita fue cancelada.',
          'especialidad': 'CONSULTORIO_FAMILIAR',
          'diaSolicitado': '2026-11-25',
          'prioridad': 1,
          'ordenLlegada': 17,
          'citaId': 37,
        }),
      ]),
    );
    addTearDown(controller.dispose);

    await tester.pumpWidget(MaterialApp(
      home: MisCitasView(
        controller: controller,
        cancelacionService: _CancelacionServiceFake(),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Cancelada'), findsOneWidget);
    expect(find.text('Cancelar cita'), findsNothing);
  });
}
