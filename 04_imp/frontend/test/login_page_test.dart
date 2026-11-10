import 'package:cns_frontend/controllers/login_controller.dart';
import 'package:cns_frontend/models/error_dto.dart';
import 'package:cns_frontend/models/sesion_dto.dart';
import 'package:cns_frontend/services/api_client.dart';
import 'package:cns_frontend/services/auth_api.dart';
import 'package:cns_frontend/views/login_page.dart';
import 'package:cns_frontend/views/menu_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

class UnavailableAuthApi implements AuthApi {
  @override
  Future<SesionDto> login(String usuario, String clave) async {
    throw const ApiException(
      ErrorDto(
        codigo: 'RED_NO_DISPONIBLE',
        mensaje: 'No se pudo conectar con el servidor.',
      ),
    );
  }
}

class RejectedAuthApi implements AuthApi {
  @override
  Future<SesionDto> login(String usuario, String clave) async {
    throw const ApiException(
      ErrorDto(codigo: 'AUTH_401', mensaje: 'No fue posible autenticar.'),
      statusCode: 401,
    );
  }
}

void main() {
  testWidgets('muestra error controlado con campos incompletos', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: LoginPage(controller: LoginController(const FakeAuthApi())),
      ),
    );

    await tester.tap(find.text('Ingresar'));
    await tester.pump();

    expect(find.byKey(const Key('login-error')), findsOneWidget);
    expect(find.text('Completa usuario y clave.'), findsOneWidget);
  });

  testWidgets('FakeAuthApi permite iniciar sesion sin backend', (tester) async {
    final controller = LoginController(const FakeAuthApi());
    await tester.pumpWidget(
      MaterialApp(home: LoginPage(controller: controller)),
    );

    await tester.enterText(find.byType(TextField).at(0), 'usuario-prueba');
    await tester.enterText(find.byType(TextField).at(1), 'clave-prueba');
    await tester.tap(find.text('Ingresar'));
    await tester.pumpAndSettle();

    expect(controller.sesion?.nombreUsuario, 'usuario-prueba');
    expect(controller.sesion?.permisos, contains('CITAS_CONSULTAR'));
  });

  testWidgets('muestra error controlado cuando el backend no esta disponible', (tester) async {
    final controller = LoginController(UnavailableAuthApi());
    await tester.pumpWidget(
      MaterialApp(home: LoginPage(controller: controller)),
    );

    await tester.enterText(find.byType(TextField).at(0), 'usuario-prueba');
    await tester.enterText(find.byType(TextField).at(1), 'clave-prueba');
    await tester.tap(find.text('Ingresar'));
    await tester.pumpAndSettle();

    expect(find.byKey(const Key('login-error')), findsOneWidget);
    expect(find.text('No se pudo conectar con el servidor.'), findsOneWidget);
  });

  testWidgets('credenciales rechazadas mantienen la sesion cerrada', (tester) async {
    final controller = LoginController(RejectedAuthApi());
    await tester.pumpWidget(
      MaterialApp(home: LoginPage(controller: controller)),
    );

    await tester.enterText(find.byType(TextField).at(0), 'usuario-prueba');
    await tester.enterText(find.byType(TextField).at(1), 'clave-prueba');
    await tester.tap(find.text('Ingresar'));
    await tester.pumpAndSettle();

    expect(controller.sesion, isNull);
    expect(find.text('No fue posible autenticar.'), findsOneWidget);
  });

  testWidgets('medico solo ve las opciones de sus permisos', (tester) async {
    const sesion = SesionDto(
      usuarioId: 2,
      nombreUsuario: 'medico-prueba',
      rol: 'MEDICO',
      permisos: ['AGENDA_CONSULTAR', 'PACIENTES_CONSULTAR'],
    );
    await tester.pumpWidget(
      MaterialApp(
        home: MenuPage(sesion: sesion, onLogout: () {}),
      ),
    );

    expect(find.text('Mi agenda'), findsOneWidget);
    expect(find.text('Pacientes'), findsOneWidget);
    expect(find.text('Usuarios'), findsNothing);
    expect(find.text('Reportes'), findsNothing);
  });
}
