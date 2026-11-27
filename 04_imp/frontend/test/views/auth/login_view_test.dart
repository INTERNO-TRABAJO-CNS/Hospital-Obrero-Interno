import 'package:cns_app/controllers/sesion_controller.dart';
import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/services/auth_service.dart';
import 'package:cns_app/services/paciente_service.dart';
import 'package:cns_app/views/auth/login_view.dart';
import 'package:cns_app/views/auth/registro_titular_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

SesionController _controller() =>
    SesionController(auth: FakeAuthService(), pacientes: FakePacienteService());

Future<void> _tamano(WidgetTester tester, Size s) async {
  tester.view.physicalSize = s;
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
}

void main() {
  for (final (nombre, tamano) in [('celular', const Size(390, 844)), ('escritorio', const Size(1440, 900))]) {
    testWidgets('login en $nombre: credenciales invalidas muestran el error', (tester) async {
      await _tamano(tester, tamano);
      Sesion? entro;
      await tester.pumpWidget(MaterialApp(
        home: LoginView(controller: _controller(), alEntrar: (s) => entro = s, alRegistrarse: () {}),
      ));

      await tester.enterText(find.byKey(const Key('login_usuario')), 'paciente');
      await tester.enterText(find.byKey(const Key('login_clave')), 'mala');
      await tester.tap(find.byKey(const Key('login_entrar')));
      await tester.pumpAndSettle();

      expect(find.text('Usuario o contraseña incorrectos'), findsOneWidget);
      expect(entro, isNull);
    });
  }

  testWidgets('login correcto entrega la sesion', (tester) async {
    Sesion? entro;
    await tester.pumpWidget(MaterialApp(
      home: LoginView(controller: _controller(), alEntrar: (s) => entro = s, alRegistrarse: () {}),
    ));

    await tester.enterText(find.byKey(const Key('login_usuario')), 'paciente');
    await tester.enterText(find.byKey(const Key('login_clave')), 'paciente123');
    await tester.tap(find.byKey(const Key('login_entrar')));
    await tester.pumpAndSettle();

    expect(entro?.rol, Rol.paciente);
  });

  testWidgets('registro del titular: afiliado inexistente muestra AFILIADO_NO_ENCONTRADO', (tester) async {
    await _tamano(tester, const Size(390, 844));
    await tester.pumpWidget(MaterialApp(
      home: RegistroTitularView(controller: _controller(), alRegistrar: (_) {}, alVolver: () {}),
    ));

    await tester.enterText(find.byKey(const Key('registro_afiliado')), 'AF-00000');
    await tester.enterText(find.byKey(const Key('registro_ci')), '1234567');
    await tester.enterText(find.byKey(const Key('registro_usuario')), 'nuevo');
    await tester.enterText(find.byKey(const Key('registro_clave')), 'secreta1');
    await tester.enterText(find.byKey(const Key('registro_repetir')), 'secreta1');
    await tester.ensureVisible(find.byKey(const Key('registro_crear')));
    await tester.tap(find.byKey(const Key('registro_crear')));
    await tester.pumpAndSettle();

    expect(find.text('El número de afiliado no está registrado en la CNS'), findsOneWidget);
  });
}
