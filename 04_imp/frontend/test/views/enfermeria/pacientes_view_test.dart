import 'dart:convert';

import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/services/paciente_service.dart';
import 'package:cns_app/views/enfermeria/pacientes_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

DateTime _reloj() => DateTime(2026, 11, 26, 9);

/// Con datos de ejemplo: AF-10001 tiene al titular Juan Carlos (CI 4512301) y a la beneficiaria Lucía (CI 9988776).
Future<FakePacienteService> _mostrar(WidgetTester tester) async {
  tester.view.physicalSize = const Size(1440, 900);
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.reset);
  final servicio = FakePacienteService(conDatosDeEjemplo: true);
  await tester.pumpWidget(MaterialApp(
    home: Scaffold(body: SingleChildScrollView(child: PacientesView(servicio: servicio, reloj: _reloj))),
  ));
  return servicio;
}

Future<void> _buscar(WidgetTester tester, String afiliado, {String ci = ''}) async {
  await tester.enterText(find.byKey(const Key('pacientes_afiliado')), afiliado);
  await tester.enterText(find.byKey(const Key('pacientes_ci')), ci);
  await tester.tap(find.byKey(const Key('pacientes_buscar')));
  await tester.pumpAndSettle();
}

Future<void> _llenarFormulario(WidgetTester tester, {String nombres = 'Rosa', String apellidos = 'Quispe'}) async {
  await tester.enterText(find.byKey(const Key('form_nombres')), nombres);
  await tester.enterText(find.byKey(const Key('form_apellidos')), apellidos);
  await tester.tap(find.byKey(const Key('form_nacimiento')));
  await tester.pumpAndSettle();
  await tester.tap(find.text('OK'));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('CP-CNS-08A: buscar un paciente existente abre su ficha sin crear otro', (tester) async {
    final servicio = await _mostrar(tester);

    await _buscar(tester, 'AF-10001', ci: '4512301');

    expect(find.byKey(const Key('ficha_paciente_1')), findsOneWidget);
    expect(find.text('Juan Carlos Choque Mamani'), findsWidgets);
    expect(find.text('41 años'), findsOneWidget);
    expect(find.byKey(const Key('pacientes_registrar')), findsNothing);
    expect(servicio.pacientes, hasLength(2));
  });

  testWidgets('buscar solo por afiliación lista titular y beneficiarios', (tester) async {
    await _mostrar(tester);

    await _buscar(tester, 'AF-10001');

    expect(find.text('TITULAR'), findsOneWidget);
    expect(find.text('BENEFICIARIO'), findsOneWidget);
    await tester.tap(find.text('Lucía Choque Rojas'));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('ficha_paciente_2')), findsOneWidget);
  });

  testWidgets('CP-CNS-08B: registrar con campos faltantes muestra validación y no da de alta', (tester) async {
    final servicio = await _mostrar(tester);
    await _buscar(tester, 'AF-10002', ci: '5550001');
    expect(find.byKey(const Key('pacientes_sin_resultado')), findsOneWidget);

    await tester.tap(find.byKey(const Key('pacientes_registrar')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('form_registrar')));
    await tester.pumpAndSettle();

    expect(find.byKey(const Key('form_error')), findsOneWidget);
    expect(find.textContaining('nombres, apellidos, fecha de nacimiento'), findsOneWidget);
    expect(servicio.pacientes, hasLength(2));
  });

  testWidgets('busca, no existe y lo registra: se abre su ficha', (tester) async {
    final servicio = await _mostrar(tester);
    await _buscar(tester, 'AF-10002', ci: '5550001');
    await tester.tap(find.byKey(const Key('pacientes_registrar')));
    await tester.pumpAndSettle();

    // El formulario llega con la afiliación y el CI de la búsqueda.
    expect(tester.widget<TextField>(find.byKey(const Key('form_afiliado'))).controller!.text, 'AF-10002');
    await _llenarFormulario(tester);
    await tester.tap(find.byKey(const Key('form_registrar')));
    await tester.pumpAndSettle();

    expect(servicio.pacientes, hasLength(3));
    expect(find.textContaining('Paciente registrado'), findsOneWidget);
    expect(find.byKey(const Key('ficha_paciente_3')), findsOneWidget);
  });

  testWidgets('[1a] si ya estaba registrado se muestra el existente y no se duplica', (tester) async {
    final servicio = await _mostrar(tester);
    await _buscar(tester, 'AF-10001', ci: '0000000');
    await tester.tap(find.byKey(const Key('pacientes_registrar')));
    await tester.pumpAndSettle();
    // Corrige el CI al de Lucía, que ya existe.
    await tester.enterText(find.byKey(const Key('form_ci')), '9988776');
    await _llenarFormulario(tester, nombres: 'Lucía', apellidos: 'Choque Rojas');
    await tester.tap(find.byKey(const Key('form_registrar')));
    await tester.pumpAndSettle();

    expect(servicio.pacientes, hasLength(2));
    expect(find.textContaining('ya estaba registrado'), findsOneWidget);
    expect(find.byKey(const Key('ficha_paciente_2')), findsOneWidget);
  });

  test('ApiPacienteService.buscar envía afiliado y CI con el token', () async {
    late http.Request peticion;
    final api = ApiClient(
      urlBase: 'http://api',
      cliente: MockClient((r) async {
        peticion = r;
        return http.Response(
            jsonEncode([
              {
                'pacienteId': 7,
                'numeroAfiliado': 'AF-1',
                'ci': '123',
                'nombres': 'Ana',
                'apellidos': 'Paz',
                'fechaNacimiento': '2000-01-02',
                'titular': true,
              }
            ]),
            200,
            headers: {'content-type': 'application/json; charset=utf-8'});
      }),
    )..token = 'tok';

    final lista = await ApiPacienteService(api).buscar(' AF-1 ', ci: '123');

    expect(peticion.url.path, '/api/v1/pacientes');
    expect(peticion.url.queryParameters, {'afiliado': 'AF-1', 'ci': '123'});
    expect(peticion.headers['Authorization'], 'Bearer tok');
    expect(lista.single.nombreCompleto, 'Ana Paz');
  });
}
