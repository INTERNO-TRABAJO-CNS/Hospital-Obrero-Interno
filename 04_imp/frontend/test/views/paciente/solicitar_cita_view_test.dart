import 'dart:convert';

import 'package:cns_app/controllers/solicitud_controller.dart';
import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/citas/cita_paciente.dart';
import 'package:cns_app/services/cancelacion_service.dart';
import 'package:cns_app/services/citas_paciente_service.dart';
import 'package:cns_app/services/paciente_service.dart';
import 'package:cns_app/services/solicitud_service.dart';
import 'package:cns_app/views/paciente/solicitar_cita_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

/// CU-04 (RF-04, RU-01, RN-01, RN-05): especialidad + dia, para el titular o uno de sus beneficiarios.
/// Hoy: miercoles 18/11/2026. Grupo de ejemplo: Juan Carlos (titular, 1985) y Lucia (beneficiaria, 2019).
void main() {
  final hoy = DateTime(2026, 11, 18);

  Future<FakeSolicitudService> abrir(WidgetTester tester) async {
    final servicio = FakeSolicitudService(reloj: () => hoy);
    final controller = SolicitudController(
      service: servicio,
      pacientes: FakePacienteService(conDatosDeEjemplo: true),
    );
    await tester.binding.setSurfaceSize(const Size(900, 1400));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await tester.pumpWidget(MaterialApp(
      home: SolicitarCitaView(controller: controller, numeroAfiliado: 'AF-10001'),
    ));
    await tester.pumpAndSettle();
    return servicio;
  }

  testWidgets('muestra los dias habiles (sin sabado ni domingo) y el grupo familiar', (tester) async {
    await abrir(tester);

    expect(find.byKey(const Key('dia_2026-11-18')), findsOneWidget, reason: 'hoy es habil');
    expect(find.byKey(const Key('dia_2026-11-23')), findsOneWidget);
    expect(find.byKey(const Key('dia_2026-11-21')), findsNothing, reason: 'sabado');
    expect(find.byKey(const Key('dia_2026-11-22')), findsNothing, reason: 'domingo');
    expect(find.text('Juan Carlos Choque Mamani (titular)'), findsOneWidget);
    expect(find.text('Lucía Choque Rojas'), findsOneWidget);
  });

  testWidgets('sin dia no se envia la solicitud', (tester) async {
    final servicio = await abrir(tester);

    await tester.tap(find.byKey(const Key('especialidad_CONSULTORIO_FAMILIAR')));
    await tester.tap(find.byKey(const Key('solicitar_continuar')));
    await tester.pump();

    expect(find.text('Seleccione el día de atención.'), findsOneWidget);
    expect(servicio.solicitudes, isEmpty);
  });

  testWidgets('el titular adulto no puede elegir Pediatria; su hija si', (tester) async {
    await abrir(tester);

    expect(find.text('Solo para menores de 10 años'), findsOneWidget, reason: 'por defecto, el titular (41 años)');

    await tester.tap(find.byKey(const Key('paciente_2')));
    await tester.pump();

    expect(find.text('Solo para menores de 10 años'), findsNothing);
  });

  testWidgets('solicita Pediatria para la beneficiaria el dia elegido', (tester) async {
    final servicio = await abrir(tester);

    await tester.tap(find.byKey(const Key('paciente_2')));
    await tester.pump();
    await tester.tap(find.byKey(const Key('especialidad_PEDIATRIA')));
    await tester.tap(find.byKey(const Key('dia_2026-11-23')));
    await tester.pump();
    await tester.tap(find.byKey(const Key('solicitar_continuar')));
    await tester.pumpAndSettle();

    final s = servicio.solicitudes.single;
    expect(s.especialidad, Especialidad.pediatria);
    expect(s.diaSolicitado, DateTime(2026, 11, 23));
    expect(s.pacienteId, 2);
  });

  test('ApiSolicitudService manda especialidad, dia y beneficiario', () async {
    late http.Request peticion;
    final api = ApiClient(
      urlBase: 'http://api',
      cliente: MockClient((r) async {
        peticion = r;
        return http.Response(
            jsonEncode({
              'solicitudId': 1,
              'pacienteId': 2,
              'estado': 'PENDIENTE',
              'mensaje': 'ok',
              'especialidad': 'PEDIATRIA',
              'diaSolicitado': '2026-11-23',
              'prioridad': 1,
              'ordenLlegada': 5,
              'citaId': null,
            }),
            201,
            headers: {'content-type': 'application/json; charset=utf-8'});
      }),
    )..token = 'tok';

    final r = await ApiSolicitudService(api)
        .solicitar(Especialidad.pediatria, DateTime(2026, 11, 23), pacienteId: 2);

    expect(peticion.url.path, '/api/v1/solicitudes');
    expect(jsonDecode(peticion.body), {'especialidad': 'PEDIATRIA', 'dia': '2026-11-23', 'pacienteId': 2});
    expect(r.pacienteId, 2);
  });

  test('cancelar y reprogramar la cita de un beneficiario mandan su pacienteId (RN-05)', () async {
    final rutas = <String>[];
    final api = ApiClient(
      urlBase: 'http://api',
      cliente: MockClient((r) async {
        rutas.add('${r.url.path}?${r.url.query}');
        final cuerpo = r.url.path.endsWith('reprogramar')
            ? {
                'solicitudId': 9,
                'pacienteId': 2,
                'estado': 'PENDIENTE',
                'mensaje': 'ok',
                'especialidad': 'PEDIATRIA',
                'diaSolicitado': '2026-11-24',
                'prioridad': 2,
                'ordenLlegada': 6,
                'citaId': null,
              }
            : {'resultado': 'CANCELADA', 'mensaje': 'Cita cancelada'};
        return http.Response(jsonEncode(cuerpo), 200, headers: {'content-type': 'application/json; charset=utf-8'});
      }),
    )..token = 'tok';

    await ApiCancelacionService(api).cancelar(7, pacienteId: 2);
    await ApiCitasPacienteService(api).reprogramar(8, pacienteId: 2);
    await ApiCancelacionService(api).cancelar(10);

    expect(rutas, [
      '/api/v1/citas/7/cancelar?pacienteId=2',
      '/api/v1/citas/8/reprogramar?pacienteId=2',
      '/api/v1/citas/10/cancelar?',
    ]);
  });

  test('CitaPaciente lee de quien es la cita', () {
    final c = CitaPaciente.fromJson({
      'citaId': 4,
      'pacienteId': 2,
      'nombrePaciente': 'Lucía Choque Rojas',
      'nombreMedico': 'Dra. Quispe',
      'especialidad': 'PEDIATRIA',
      'consultorio': 6,
      'fecha': '2026-11-23',
      'hora': '09:00:00',
      'numeroFicha': 1,
      'estado': 'PROGRAMADA',
    });

    expect(c.pacienteId, 2);
    expect(c.paciente, 'Lucía Choque Rojas');
    expect(c.copiar().pacienteId, 2, reason: 'copiar conserva al paciente');
  });

  test('edad: se cuenta el dia de la cita', () {
    expect(edadEn(DateTime(2016, 11, 23), DateTime(2026, 11, 22)), 9);
    expect(edadEn(DateTime(2016, 11, 23), DateTime(2026, 11, 23)), 10);
  });
}
