import 'package:cns_app/core/app_router.dart';
import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/views/admin/catalogos_view.dart';
import 'package:cns_app/views/auth/login_view.dart';
import 'package:cns_app/views/comun/marco_personal.dart';
import 'package:cns_app/views/enfermeria/fichas_dia_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

Future<void> _abrir(WidgetTester tester, String ruta) async {
  tester.view.physicalSize = const Size(1440, 900);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(MaterialApp(
    navigatorKey: navegadorApp,
    initialRoute: ruta,
    onGenerateRoute: generarRuta,
  ));
  await tester.pump();
}

void main() {
  tearDown(() => sesionApp.sesion = null);

  testWidgets('sin sesion, /admin muestra el login (antes se abria sin proteccion)', (tester) async {
    await _abrir(tester, AppRutas.admin);

    expect(find.byType(LoginView), findsOneWidget);
    expect(find.byType(CatalogosView), findsNothing);
  });

  testWidgets('enfermeria no puede abrir pantallas del administrador', (tester) async {
    sesionApp.sesion =
        const Sesion(usuarioId: 2, rol: Rol.enfermeria, personaId: 2, nombre: 'María Flores', token: 't');

    await _abrir(tester, AppRutas.adminReportes);

    expect(find.byType(LoginView), findsOneWidget);
  });

  testWidgets('enfermeria entra a Fichas del día dentro del marco del Figma', (tester) async {
    sesionApp.sesion =
        const Sesion(usuarioId: 2, rol: Rol.enfermeria, personaId: 2, nombre: 'María Flores', token: 't');

    await _abrir(tester, AppRutas.enfermeria);
    await tester.pump(const Duration(milliseconds: 50));

    expect(find.byType(MarcoPersonal), findsOneWidget);
    expect(find.byType(FichasDiaView), findsOneWidget);
    expect(find.text('Enfermería · María Flores'), findsOneWidget);
    expect(find.text('Confirmar asistencia'), findsOneWidget);
  });
}
