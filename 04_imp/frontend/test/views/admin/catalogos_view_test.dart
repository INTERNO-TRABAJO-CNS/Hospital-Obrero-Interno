import 'package:cns_app/services/cuentas_service.dart';
import 'package:cns_app/services/fake_catalogo_service.dart';
import 'package:cns_app/services/quirofano_service.dart';
import 'package:cns_app/views/admin/catalogos_view.dart';
import 'package:cns_app/views/comun/marco_personal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Pantallas de computadora (1440 x 900, como el Figma) dentro del marco del personal.
Future<(FakeCatalogoService, FakeQuirofanoService, FakeCuentasService)> _mostrar(WidgetTester tester) async {
  tester.view.physicalSize = const Size(1440, 900);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  final servicio = FakeCatalogoService(conDatosDeEjemplo: true);
  final quirofanos = FakeQuirofanoService();
  final cuentas = FakeCuentasService();
  await tester.pumpWidget(MaterialApp(
    home: MarcoPersonal(
      titulo: 'Catálogos',
      etiqueta: 'Administrador · Prueba',
      opciones: const [OpcionMenu('Catálogos', '/admin')],
      rutaActual: '/admin',
      alSalir: () {},
      alNavegar: (_) {},
      child: CatalogosView(servicio: servicio, quirofanos: quirofanos, cuentas: cuentas),
    ),
  ));
  await tester.pumpAndSettle();
  return (servicio, quirofanos, cuentas);
}

void main() {
  testWidgets('muestra la tabla de medicos con tipo BÁSICA/INTERNA y estado ACTIVO/BAJA', (tester) async {
    await _mostrar(tester);

    expect(find.text('Catálogos'), findsWidgets);
    expect(find.text('Administrador · Prueba'), findsOneWidget);
    expect(find.text('Carlos Ramos'), findsOneWidget);
    expect(find.text('ACTIVO'), findsNWidgets(8));
    expect(find.text('BAJA'), findsOneWidget);
    expect(find.text('BÁSICA'), findsNWidgets(4));
    expect(find.text('INTERNA'), findsNWidgets(5));
    expect(tester.takeException(), isNull);
  });

  testWidgets('el desplegable de especialidad agrupa las 3 básicas y las 10 internas', (tester) async {
    await _mostrar(tester);

    await tester.tap(find.byKey(const ValueKey('medico_especialidad_0')));
    await tester.pumpAndSettle();

    expect(find.text('BÁSICAS'), findsOneWidget);
    expect(find.text('INTERNAS'), findsOneWidget);
    expect(find.text('Terapia Intensiva'), findsWidgets);
    expect(find.text('30–60 min'), findsOneWidget);
  });

  testWidgets('[3a] cupo 8 muestra el error y no agrega el medico', (tester) async {
    final (servicio, _, _) = await _mostrar(tester);

    await tester.enterText(find.byKey(const Key('medico_nombres')), 'Carla');
    await tester.enterText(find.byKey(const Key('medico_apellidos')), 'Vargas');
    await tester.enterText(find.byKey(const Key('medico_duracion')), '40');
    await tester.enterText(find.byKey(const Key('medico_cupo')), '8');
    await tester.tap(find.byKey(const Key('medico_guardar')));
    await tester.pumpAndSettle();

    expect(find.text('El cupo diario debe estar entre 5 y 7 fichas.'), findsOneWidget);
    expect(servicio.medicos, hasLength(9));
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

  testWidgets('quirofanos: no se desactiva uno con cirugias programadas (QUIROFANO_CON_CIRUGIAS)', (tester) async {
    final (_, quirofanos, _) = await _mostrar(tester);

    await tester.tap(find.text('Quirófanos'));
    await tester.pumpAndSettle();
    expect(find.text('G-1'), findsOneWidget);

    await tester.tap(find.text('G-1'));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('quirofano_desactivar')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Confirmar'));
    await tester.pumpAndSettle();

    expect(find.text('QUIROFANO_CON_CIRUGIAS'), findsOneWidget);
    expect(quirofanos.quirofanos.first.activo, isTrue);
  });

  testWidgets('quirofanos: limpieza fuera de 30-45 min se rechaza con su codigo', (tester) async {
    await _mostrar(tester);
    await tester.tap(find.text('Quirófanos'));
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('quirofano_nombre')), 'G-3');
    await tester.enterText(find.byKey(const Key('quirofano_limpieza')), '20');
    await tester.tap(find.byKey(const Key('quirofano_guardar')));
    await tester.pumpAndSettle();

    expect(find.text('LIMPIEZA_FUERA_DE_RANGO'), findsOneWidget);
  });

  testWidgets('cuentas: crear una cuenta de enfermeria', (tester) async {
    final (_, _, cuentas) = await _mostrar(tester);
    await tester.tap(find.text('Cuentas'));
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('cuenta_nombre')), 'Rosa Apaza');
    await tester.enterText(find.byKey(const Key('cuenta_usuario')), 'rapaza');
    await tester.enterText(find.byKey(const Key('cuenta_clave')), 'segura123');
    await tester.tap(find.byKey(const Key('cuenta_crear')));
    await tester.pumpAndSettle();

    expect(find.text('rapaza'), findsOneWidget);
    expect(cuentas.cuentas.last.nombre, 'Rosa Apaza');
  });

  testWidgets('cuentas: contraseña corta se rechaza sin llamar al servicio', (tester) async {
    final (_, _, cuentas) = await _mostrar(tester);
    await tester.tap(find.text('Cuentas'));
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('cuenta_nombre')), 'Rosa Apaza');
    await tester.enterText(find.byKey(const Key('cuenta_usuario')), 'rapaza');
    await tester.enterText(find.byKey(const Key('cuenta_clave')), '123');
    await tester.tap(find.byKey(const Key('cuenta_crear')));
    await tester.pumpAndSettle();

    expect(find.text('La contraseña debe tener al menos 6 caracteres.'), findsOneWidget);
    expect(cuentas.cuentas, hasLength(3));
  });
}
