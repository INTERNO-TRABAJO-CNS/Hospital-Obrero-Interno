import 'package:cns_app/services/fake_catalogo_service.dart';
import 'package:cns_app/views/admin/catalogos_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Pantallas de computadora (1440 x 900, como el Figma) con FakeCatalogoService.
Future<FakeCatalogoService> _mostrar(WidgetTester tester) async {
  tester.view.physicalSize = const Size(1440, 900);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  final servicio = FakeCatalogoService(conDatosDeEjemplo: true);
  await tester.pumpWidget(MaterialApp(home: Scaffold(body: CatalogosView(servicio: servicio))));
  await tester.pumpAndSettle();
  return servicio;
}

void main() {
  testWidgets('muestra la tabla de medicos con estado ACTIVO/BAJA', (tester) async {
    await _mostrar(tester);

    expect(find.text('Catálogos'), findsOneWidget);
    expect(find.text('Jorge Ramos'), findsOneWidget);
    expect(find.text('ACTIVO'), findsNWidgets(3));
    expect(find.text('BAJA'), findsOneWidget);
  });

  testWidgets('[3a] cupo 8 muestra el error y no agrega el medico', (tester) async {
    final servicio = await _mostrar(tester);

    await tester.enterText(find.byKey(const Key('medico_nombres')), 'Carla');
    await tester.enterText(find.byKey(const Key('medico_apellidos')), 'Vargas');
    await tester.enterText(find.byKey(const Key('medico_duracion')), '40');
    await tester.enterText(find.byKey(const Key('medico_cupo')), '8');
    await tester.tap(find.byKey(const Key('medico_guardar')));
    await tester.pumpAndSettle();

    expect(find.text('El cupo diario debe estar entre 5 y 7 fichas.'), findsOneWidget);
    expect(servicio.medicos, hasLength(4));
  });

  testWidgets('guardar un medico valido lo agrega a la tabla', (tester) async {
    await _mostrar(tester);

    await tester.enterText(find.byKey(const Key('medico_nombres')), 'Carla');
    await tester.enterText(find.byKey(const Key('medico_apellidos')), 'Vargas');
    await tester.enterText(find.byKey(const Key('medico_duracion')), '40');
    await tester.enterText(find.byKey(const Key('medico_cupo')), '6');
    await tester.tap(find.byKey(const Key('medico_guardar')));
    await tester.pumpAndSettle();

    expect(find.text('Carla Vargas'), findsOneWidget);
  });

  testWidgets('seccion horarios: franjas agrupadas y fila de fin de semana', (tester) async {
    await _mostrar(tester);

    await tester.tap(find.text('Horarios y días no hábiles'));
    await tester.pumpAndSettle();

    expect(find.text('Horarios de médicos'), findsOneWidget);
    expect(find.text('Lun–Vie'), findsNWidgets(3));
    expect(find.text('Sáb–Dom'), findsOneWidget);
    expect(find.text('Navidad'), findsOneWidget);
  });

  testWidgets('seccion consultorios: crear uno nuevo', (tester) async {
    await _mostrar(tester);

    await tester.tap(find.text('Consultorios'));
    await tester.pumpAndSettle();
    await tester.enterText(find.byKey(const Key('consultorio_nombre')), 'Consultorio 9');
    await tester.tap(find.byKey(const Key('consultorio_guardar')));
    await tester.pumpAndSettle();

    expect(find.text('Consultorio 9'), findsOneWidget);
  });
}
