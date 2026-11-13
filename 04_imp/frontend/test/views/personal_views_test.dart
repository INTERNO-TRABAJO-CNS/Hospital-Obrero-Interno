import 'package:cns_app/services/enfermeria_service.dart';
import 'package:cns_app/services/historial_cambios_service.dart';
import 'package:cns_app/services/quirofano_service.dart';
import 'package:cns_app/services/reportes_service.dart';
import 'package:cns_app/services/supervision_service.dart';
import 'package:cns_app/views/admin/reportes_view.dart';
import 'package:cns_app/views/admin/supervision_view.dart';
import 'package:cns_app/views/comun/historial_cambios_view.dart';
import 'package:cns_app/views/comun/marco_personal.dart';
import 'package:cns_app/views/enfermeria/confirmar_asistencia_view.dart';
import 'package:cns_app/views/enfermeria/fichas_dia_view.dart';
import 'package:cns_app/views/enfermeria/ticket_cita_dialog.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

DateTime _reloj() => DateTime(2026, 11, 13, 9, 0);

/// Monta [contenido] dentro del marco del personal, a 1440 x 900 como el Figma (o al [tamano] dado).
Future<void> _mostrar(WidgetTester tester, Widget contenido, {Size tamano = const Size(1440, 900)}) async {
  tester.view.physicalSize = tamano;
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(MaterialApp(
    home: MarcoPersonal(
      titulo: 'Pantalla',
      etiqueta: 'Enfermería · María Flores',
      opciones: const [
        OpcionMenu('Fichas del día', '/enfermeria'),
        OpcionMenu('Confirmar asistencia', '/enfermeria/confirmar'),
      ],
      rutaActual: '/enfermeria',
      alSalir: () {},
      alNavegar: (_) {},
      child: contenido,
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  group('Enfermería', () {
    testWidgets('fichas del día: tabla del Figma y búsqueda por médico', (tester) async {
      await _mostrar(
          tester,
          FichasDiaView(
              servicio: FakeEnfermeriaService(reloj: _reloj), usuario: 'Enfermería · M. Flores', reloj: _reloj));

      expect(find.text('Marcos Condori'), findsOneWidget);
      expect(find.text('ASISTENCIA_CONFIRMADA'), findsOneWidget);
      expect(find.text('Hoy 13/11/2026 (fija)'), findsOneWidget);
      expect(find.text('Buscar por médico…'), findsOneWidget);

      await tester.enterText(find.byKey(const Key('buscar_fichas')), 'mamani');
      await tester.pumpAndSettle();

      expect(find.text('Elena Rojas'), findsOneWidget);
      expect(find.text('Marcos Condori'), findsNothing);
      expect(tester.takeException(), isNull);
    });

    testWidgets('tocar una ficha abre el ticket para imprimir', (tester) async {
      await _mostrar(
          tester,
          FichasDiaView(
              servicio: FakeEnfermeriaService(reloj: _reloj), usuario: 'Enfermería · M. Flores', reloj: _reloj));

      await tester.tap(find.text('Elena Copa'));
      await tester.pumpAndSettle();

      expect(find.text('Imprimir ticket · Ficha N° 3'), findsOneWidget);
      expect(find.text('Elena Copa · 4587-12'), findsOneWidget);
      expect(find.text('Dr. Ramos · Consultorio 4'), findsOneWidget);

      // En pruebas (VM) no hay impresion directa: se avisa en vez de fallar.
      await tester.tap(find.byKey(const Key('ticket_imprimir')));
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('ticket_aviso')), findsOneWidget);

      await tester.tap(find.byKey(const Key('ticket_cerrar')));
      await tester.pumpAndSettle();
      expect(find.byType(TicketCitaDialog), findsNothing);
    });

    testWidgets('el ticket impreso es HTML de 80 mm con los datos escapados', (tester) async {
      final fichas = await FakeEnfermeriaService(reloj: _reloj).fichasDeHoy();
      final html = htmlTicket(fichas.first);

      expect(html, contains('size:80mm'));
      expect(html, contains('CITA · N° 1'));
      expect(html, contains('Marcos Condori · 4512-01'));
      expect(htmlTicket(fichas.first), isNot(contains('<script')));
    });

    testWidgets('confirmar asistencia: muestra lo que falta y confirma', (tester) async {
      final servicio = FakeEnfermeriaService(reloj: _reloj, historial: FakeHistorialCambiosService(reloj: _reloj));
      await _mostrar(
          tester, ConfirmarAsistenciaView(servicio: servicio, usuario: 'Enfermería · M. Flores', reloj: _reloj));

      expect(find.text('Confirmar hasta 09:07'), findsOneWidget);
      expect(find.text('Faltan 7 min'), findsOneWidget);
      expect(find.text('Confirmó llegada'), findsNWidgets(3));

      final pedro = servicio.fichas.firstWhere((f) => f.paciente == 'Pedro Alanoca');
      await tester.tap(find.byKey(Key('confirmar_${pedro.citaId}')));
      await tester.pumpAndSettle();

      expect(find.text('Confirmó llegada'), findsNWidgets(2));
      expect(find.textContaining('Llegada confirmada: Pedro Alanoca'), findsOneWidget);
    });

    testWidgets('celular (390 px): menu en cajón y sin desbordes', (tester) async {
      await _mostrar(
        tester,
        ConfirmarAsistenciaView(
            servicio: FakeEnfermeriaService(reloj: _reloj), usuario: 'Enfermería · M. Flores', reloj: _reloj),
        tamano: const Size(390, 844),
      );

      expect(find.text('Fichas del día'), findsNothing, reason: 'el menu esta en el cajon');
      expect(tester.takeException(), isNull);

      await tester.tap(find.byTooltip('Open navigation menu'));
      await tester.pumpAndSettle();
      expect(find.text('Fichas del día'), findsOneWidget);
    });
  });

  group('Administrador', () {
    FakeSupervisionService supervision() => FakeSupervisionService(
        enfermeria: FakeEnfermeriaService(reloj: _reloj), quirofanos: FakeQuirofanoService(), reloj: _reloj);

    testWidgets('supervisión: citas comunes, fichas internas y quirófanos', (tester) async {
      await _mostrar(tester, SupervisionView(servicio: supervision()));

      expect(find.text('Fichas hoy'), findsOneWidget);
      expect(find.text('Agenda por médico (solo lectura)'), findsOneWidget);
      expect(find.text('Ficha 3 · 08:00'), findsOneWidget);

      await tester.tap(find.text('Fichas internas'));
      await tester.pumpAndSettle();
      expect(find.text('Pedro Huanca'), findsOneWidget);
      expect(find.text('DESPLAZADA'), findsOneWidget);
      expect(find.text('No se presentó'), findsOneWidget);

      await tester.tap(find.text('Tabla Pediatría'));
      await tester.pumpAndSettle();
      expect(find.text('Pedro Huanca'), findsNothing);

      await tester.tap(find.text('Quirófanos'));
      await tester.pumpAndSettle();
      expect(find.text('Ocupación por quirófano'), findsOneWidget);
      expect(find.text('G-1 · Gastroenterología'), findsOneWidget);
      expect(find.text('9.8 h / 44 h'), findsOneWidget);
      expect(tester.takeException(), isNull);
    });

    testWidgets('reportes: por médico (rojo bajo la meta) y fichas internas', (tester) async {
      await _mostrar(tester, ReportesView(servicio: FakeReportesService(reloj: _reloj)));

      expect(find.text('Fichas atendidas por día (meta mínima 5, máximo 7)'), findsOneWidget);
      expect(find.text('4 fichas · bajo la meta'), findsOneWidget);
      expect(find.text('86 solicitudes'), findsOneWidget);

      await tester.tap(find.text('Fichas internas y quirófanos'));
      await tester.pumpAndSettle();
      expect(find.text('Gastroenterología'), findsOneWidget);
      expect(find.text('31 h / 144 h'), findsOneWidget);
    });

    testWidgets('historial de cambios: filtra por usuario', (tester) async {
      await _mostrar(tester, HistorialCambiosView(servicio: FakeHistorialCambiosService(reloj: _reloj)));

      expect(find.text('Cita registrada'), findsOneWidget);
      expect(find.text('Atención finalizada'), findsOneWidget);

      await tester.enterText(find.byKey(const Key('filtro_usuario')), 'sistema');
      await tester.tap(find.byKey(const Key('historial_buscar')));
      await tester.pumpAndSettle();

      expect(find.text('Cita registrada'), findsOneWidget);
      expect(find.text('Atención finalizada'), findsNothing);
    });
  });
}
