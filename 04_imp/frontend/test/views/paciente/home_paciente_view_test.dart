import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/views/paciente/home_paciente_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

const _sesion = Sesion(usuarioId: 1, rol: Rol.paciente, personaId: 1, nombre: 'Juan Carlos Choque Mamani');

void main() {
  testWidgets('widget_home_paciente_celular: saludo, acciones y avisos', (tester) async {
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);
    var solicitar = 0;
    var misCitas = 0;

    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: HomePacienteView(
          sesion: _sesion,
          alSolicitarCita: () => solicitar++,
          alMisCitas: () => misCitas++,
          alRegistrarBeneficiario: () {},
        ),
      ),
    ));

    expect(find.text('Hola, Juan'), findsOneWidget);
    expect(find.text('Solicitar cita'), findsOneWidget);
    expect(find.text('Mis citas'), findsOneWidget);
    expect(find.byKey(const Key('home_sin_avisos')), findsOneWidget);
    expect(tester.takeException(), isNull, reason: 'no debe desbordarse en celular');

    await tester.tap(find.byKey(const Key('home_solicitar')));
    await tester.tap(find.byKey(const Key('home_mis_citas')));
    expect(solicitar, 1);
    expect(misCitas, 1);
  });

  testWidgets('muestra los avisos cuando existen', (tester) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: HomePacienteView(
          sesion: _sesion,
          alSolicitarCita: () {},
          alMisCitas: () {},
          alRegistrarBeneficiario: () {},
          avisos: const ['Su cita de mañana es a las 08:40'],
        ),
      ),
    ));

    expect(find.text('Su cita de mañana es a las 08:40'), findsOneWidget);
    expect(find.byKey(const Key('home_sin_avisos')), findsNothing);
  });
}
