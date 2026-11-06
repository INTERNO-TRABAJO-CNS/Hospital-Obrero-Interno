import 'package:cns_app/core/app_router.dart';
import 'package:cns_app/views/shell/app_shell.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

Widget _app(String ruta) {
  return MaterialApp(
    home: AppShell(
      rutaActual: ruta,
      destinos: const [
        DestinoShell(etiqueta: 'Inicio', icono: Icons.home_outlined, ruta: AppRutas.paciente),
        DestinoShell(etiqueta: 'Salir', icono: Icons.logout, ruta: AppRutas.login),
      ],
      alNavegar: (_) {},
      child: const Text('contenido'),
    ),
  );
}

Future<void> _mostrar(WidgetTester tester, Size tamano, String ruta) async {
  tester.view.physicalSize = tamano;
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(_app(ruta));
}

void main() {
  testWidgets('celular (400 px): barra de navegacion inferior', (tester) async {
    await _mostrar(tester, const Size(400, 800), AppRutas.paciente);

    expect(find.byType(NavigationBar), findsOneWidget);
    expect(find.byType(NavigationRail), findsNothing);
    expect(find.text('contenido'), findsOneWidget);
  });

  testWidgets('escritorio (1000 px): riel lateral', (tester) async {
    await _mostrar(tester, const Size(1000, 700), AppRutas.paciente);

    expect(find.byType(NavigationRail), findsOneWidget);
    expect(find.byType(NavigationBar), findsNothing);
  });

  testWidgets('TV (1920 px): sin menu', (tester) async {
    await _mostrar(tester, const Size(1920, 1080), AppRutas.paciente);

    expect(find.byType(NavigationRail), findsNothing);
    expect(find.byType(NavigationBar), findsNothing);
    expect(find.text('contenido'), findsOneWidget);
  });

  testWidgets('la ruta /tv no muestra menu aunque la ventana sea chica', (tester) async {
    await _mostrar(tester, const Size(400, 800), AppRutas.tv);

    expect(find.byType(NavigationRail), findsNothing);
    expect(find.byType(NavigationBar), findsNothing);
  });
}
