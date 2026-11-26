import 'package:cns_app/models/enfermeria/ficha_dia.dart';
import 'package:cns_app/services/citas_paciente_service.dart';
import 'package:cns_app/views/paciente/mis_citas_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Martes 10/11/2026 08:00. Las citas de ejemplo: ficha 3 en 2 dias (PROGRAMADA), ficha 5 hoy (CONFIRMADA),
/// ficha 2 (ATENDIDA) y ficha 4 (CANCELADA) en el pasado.
DateTime _reloj() => DateTime(2026, 11, 10, 8, 0);

Future<FakeCitasPacienteService> _mostrar(WidgetTester tester, Size tamano) async {
  tester.view.physicalSize = tamano;
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.reset);
  final servicio = FakeCitasPacienteService(reloj: _reloj);
  await tester.pumpWidget(MaterialApp(
    home: MisCitasView(citas: servicio, cancelacion: servicio, reloj: _reloj),
  ));
  await tester.pumpAndSettle();
  return servicio;
}

void main() {
  group('celular', () {
    testWidgets('lista las citas con su estado (Figma MIS CITAS)', (tester) async {
      await _mostrar(tester, const Size(393, 852));

      expect(find.text('Mis citas'), findsOneWidget);
      expect(find.text('Ficha N° 3'), findsOneWidget);
      expect(find.text('PROGRAMADA'), findsOneWidget);
      expect(find.text('ASISTENCIA_CONFIRMADA'), findsOneWidget);
      expect(find.text('ATENDIDA'), findsOneWidget);
      expect(find.text('CANCELADA'), findsOneWidget);
      expect(find.text('Jueves 12 nov · 09:30'), findsOneWidget);
    });

    testWidgets('detalle de una cita atendida: sin reprogramar ni cancelar', (tester) async {
      await _mostrar(tester, const Size(393, 852));

      await tester.tap(find.byKey(const Key('cita_12')));
      await tester.pumpAndSettle();

      expect(find.text('Detalle de cita'), findsOneWidget);
      expect(find.byKey(const Key('detalle_reprogramar')), findsNothing);
      expect(find.byKey(const Key('detalle_cancelar')), findsNothing);
    });

    testWidgets('cancelar pide confirmación y deja la cita CANCELADA', (tester) async {
      final servicio = await _mostrar(tester, const Size(393, 852));

      await tester.tap(find.byKey(const Key('cita_31')));
      await tester.pumpAndSettle();
      expect(find.textContaining('09:00 como máximo'), findsOneWidget);

      await tester.tap(find.byKey(const Key('detalle_cancelar')));
      await tester.pumpAndSettle();
      expect(find.text('¿Cancelar la cita?'), findsOneWidget);
      expect(find.textContaining('ficha N° 3 del jueves 12 de noviembre'), findsOneWidget);

      await tester.tap(find.byKey(const Key('cancelar_si')));
      await tester.pumpAndSettle();

      expect(servicio.citas.firstWhere((c) => c.citaId == 31).estado, EstadoCita.cancelada);
      expect(find.text('Mis citas'), findsOneWidget);
      expect(find.text('PROGRAMADA'), findsNothing);
    });

    testWidgets('reprogramar: si no confirma, la cita sigue PROGRAMADA [2a]', (tester) async {
      final servicio = await _mostrar(tester, const Size(393, 852));

      await tester.tap(find.byKey(const Key('cita_31')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('detalle_reprogramar')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('reprogramar_volver')));
      await tester.pumpAndSettle();

      expect(servicio.citas.firstWhere((c) => c.citaId == 31).estado, EstadoCita.programada);
    });

    testWidgets('reprogramar confirmado: cancela y muestra la nueva solicitud', (tester) async {
      final servicio = await _mostrar(tester, const Size(393, 852));

      await tester.tap(find.byKey(const Key('cita_31')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('detalle_reprogramar')));
      await tester.pumpAndSettle();
      expect(find.text('¿Reprogramar la cita?'), findsOneWidget);

      await tester.tap(find.byKey(const Key('reprogramar_si')));
      await tester.pumpAndSettle();

      expect(find.text('Cita reprogramada'), findsOneWidget);
      // Cita del jueves 12: la nueva solicitud busca desde el viernes 13.
      expect(find.textContaining('viernes 13 de noviembre'), findsOneWidget);
      expect(servicio.citas.firstWhere((c) => c.citaId == 31).estado, EstadoCita.cancelada);
    });
  });

  group('escritorio', () {
    testWidgets('pestañas Próximas / Pasadas y panel de detalle (Figma WEB PACIENTE MIS CITAS)', (tester) async {
      await _mostrar(tester, const Size(1440, 900));

      // Próximas: ficha 5 (hoy) y ficha 3; el panel muestra la primera.
      expect(find.text('N° 5'), findsOneWidget);
      expect(find.text('N° 3'), findsOneWidget);
      expect(find.text('N° 2'), findsNothing);
      expect(find.byKey(const Key('detalle_cita_25')), findsOneWidget);

      await tester.tap(find.text('N° 3'));
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('detalle_cita_31')), findsOneWidget);
      expect(find.byKey(const Key('detalle_reprogramar')), findsOneWidget);

      await tester.tap(find.text('Pasadas'));
      await tester.pumpAndSettle();
      expect(find.text('N° 2'), findsOneWidget);
      expect(find.text('N° 4'), findsOneWidget);
      expect(find.text('N° 3'), findsNothing);
    });
  });

  testWidgets('sin citas muestra el aviso [2a]', (tester) async {
    tester.view.physicalSize = const Size(393, 852);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.reset);
    final vacio = FakeCitasPacienteService(reloj: _reloj, conDatosDeEjemplo: false);
    await tester.pumpWidget(MaterialApp(home: MisCitasView(citas: vacio, cancelacion: vacio, reloj: _reloj)));
    await tester.pumpAndSettle();

    expect(find.byKey(const Key('mis_citas_vacio')), findsOneWidget);
  });
}
