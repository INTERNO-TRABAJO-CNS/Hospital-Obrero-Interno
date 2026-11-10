import 'package:cns_frontend/controllers/atencion_controller.dart';
import 'package:cns_frontend/services/atencion_service.dart';
import 'package:cns_frontend/views/medico/atencion_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('muestra los datos autocompletados de la cita', (tester) async {
    final controller = AtencionController(FakeAtencionService(), 10, 20);
    await tester.pumpWidget(
      MaterialApp(home: AtencionView(controller: controller)),
    );
    await tester.pumpAndSettle();

    expect(find.text('Afiliado: 1234567'), findsOneWidget);
    expect(find.text('Ficha: FIC-01'), findsOneWidget);
    expect(find.text('Consulta: Consulta general'), findsOneWidget);
  });

  testWidgets('registra una receta y muestra confirmacion', (tester) async {
    final service = FakeAtencionService();
    final controller = AtencionController(service, 10, 20);
    await tester.pumpWidget(
      MaterialApp(home: AtencionView(controller: controller)),
    );
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('medicacion-field')), 'Paracetamol 500 mg');
    await tester.tap(find.byKey(const Key('finalizar-atencion-button')));
    await tester.pumpAndSettle();

    expect(service.medicacionRegistrada, 'Paracetamol 500 mg');
    expect(find.byKey(const Key('atencion-success')), findsOneWidget);
  });

  testWidgets('rechaza una receta vacia sin llamar al servicio', (tester) async {
    final service = FakeAtencionService();
    final controller = AtencionController(service, 10, 20);
    await tester.pumpWidget(
      MaterialApp(home: AtencionView(controller: controller)),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.byKey(const Key('finalizar-atencion-button')));
    await tester.pump();

    expect(find.byKey(const Key('atencion-error')), findsOneWidget);
    expect(service.medicacionRegistrada, isNull);
  });
}