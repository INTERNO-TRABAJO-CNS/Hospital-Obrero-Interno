import 'package:cns_app/controllers/medico_controller.dart';
import 'package:cns_app/core/app_router.dart';
import 'package:cns_app/core/app_theme.dart';
import 'package:cns_app/core/rutas_medico.dart';
import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/medico/medico_actual.dart';
import 'package:cns_app/services/cirugias_service.dart';
import 'package:cns_app/services/fake_medico_service.dart';
import 'package:cns_app/services/fichas_internas_service.dart';
import 'package:cns_app/views/auth/login_view.dart';
import 'package:cns_app/views/comun/marco_personal.dart';
import 'package:cns_app/views/medico/agenda_medico_view.dart';
import 'package:cns_app/views/medico/atencion_view.dart';
import 'package:cns_app/views/medico/cirugias_view.dart';
import 'package:cns_app/views/medico/fichas_internas_view.dart';
import 'package:cns_app/views/medico/ticket_interno_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Lunes 16 de noviembre de 2026, 11:00.
DateTime _ahora() => DateTime(2026, 11, 16, 11);

const _general = MedicoActual(
    medicoId: 7,
    nombre: 'Carlos Ramos',
    especialidad: Especialidad.consultorioFamiliar,
    puedeDerivar: true,
    operaEnQuirofano: false);
const _vargas = MedicoActual(
    medicoId: 21,
    nombre: 'Raúl Vargas',
    especialidad: Especialidad.gastroenterologia,
    puedeDerivar: false,
    operaEnQuirofano: true);

void _tamano(WidgetTester tester, Size tamano) {
  tester.view.physicalSize = tamano;
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
}

/// Una pantalla del médico dentro de un Scaffold desplazable, como en el marco.
Future<void> _mostrar(WidgetTester tester, Widget pantalla, {Size tamano = const Size(1440, 900)}) async {
  _tamano(tester, tamano);
  await tester.pumpWidget(MaterialApp(
    theme: AppTheme.claro(),
    onGenerateRoute: (s) => MaterialPageRoute<void>(settings: s, builder: (_) => Scaffold(body: Text(s.name ?? ''))),
    home: Scaffold(body: SingleChildScrollView(padding: const EdgeInsets.all(16), child: pantalla)),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('agenda: muestra las 6 citas del Figma con «Atender» solo en la confirmada', (tester) async {
    final fichas = FakeFichasInternasService(reloj: _ahora);
    final agenda = AgendaMedicoController(FakeAgendaMedicoService(reloj: _ahora), fichas: fichas, reloj: _ahora);

    await _mostrar(tester, AgendaMedicoView(agenda: agenda, medico: _general));

    expect(find.text('Lunes 16 de noviembre · 6 citas hoy'), findsOneWidget);
    expect(find.byKey(const Key('atender_503')), findsOneWidget);
    expect(find.byKey(const Key('atender_504')), findsNothing);
    // Ana Mamani (cita 502) ya tiene su derivación a Cardiología.
    expect(find.text('Derivada a Cardiología · Ticket N° 1021'), findsOneWidget);
    expect(find.byKey(const Key('ticket_interno_502')), findsNothing);
    expect(find.byKey(const Key('ticket_interno_503')), findsOneWidget);

    await tester.tap(find.byKey(const Key('atender_503')));
    await tester.pumpAndSettle();
    expect(find.text(RutasMedico.atender(503)), findsOneWidget);
  });

  testWidgets('atención: finalizar guarda la receta y deja la cita ATENDIDA', (tester) async {
    final atencion = FakeAtencionService(reloj: _ahora);

    await _mostrar(tester, AtencionView(citaId: 503, servicio: atencion, historial: FakeHistorialPacienteService()));

    expect(find.textContaining('Ficha N° 3'), findsOneWidget);
    expect(find.text('Receta: Ibuprofeno 400 mg cada 8 h por 3 días.'), findsOneWidget);
    await tester.enterText(find.byKey(const Key('receta')), 'Omeprazol 20 mg en ayunas por 14 días');
    await tester.pump();
    await tester.tap(find.byKey(const Key('finalizar_atencion')));
    await tester.pumpAndSettle();

    expect(atencion.registradas, [(503, 'Omeprazol 20 mg en ayunas por 14 días')]);
    expect(find.textContaining('Atención finalizada'), findsOneWidget);
  });

  testWidgets('ticket interno: desde la cita 503 genera la ficha y ofrece imprimirla', (tester) async {
    final fichas = FakeFichasInternasService(reloj: _ahora);
    final agenda = AgendaMedicoController(FakeAgendaMedicoService(reloj: _ahora), fichas: fichas, reloj: _ahora);

    await _mostrar(
      tester,
      TicketInternoView(
          medico: _general, agenda: agenda, fichas: fichas, historial: FakeHistorialPacienteService(), citaId: 503),
    );

    expect(find.text('4587-12 · Elena Copa · 34 años'), findsOneWidget);
    await tester.tap(find.byType(DropdownButtonFormField<Especialidad>));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Neumología').last);
    await tester.pumpAndSettle();
    await tester.enterText(find.byKey(const Key('motivo')), 'Tos crónica con disnea');
    await tester.pump();
    await tester.tap(find.byKey(const Key('generar_ficha')));
    await tester.pumpAndSettle();

    expect(find.text('Ticket N° 1033'), findsOneWidget);
    expect(find.text('Ficha interna generada'), findsOneWidget);
    await tester.tap(find.byKey(const Key('imprimir_ficha')));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('ticket_medico_numero')), findsOneWidget);
    expect(find.textContaining('MEDIA'), findsNothing, reason: 'la gravedad nunca se imprime');
  });

  testWidgets('especialista: sus fichas de hoy y «Programar cirugía» en las atendidas', (tester) async {
    final fichas = FakeFichasInternasService(reloj: _ahora);

    await _mostrar(tester, FichasInternasView(medico: _vargas, fichas: fichas, reloj: _ahora));

    expect(find.textContaining('7 fichas internas'), findsOneWidget);
    expect(find.byKey(const Key('programar_1019')), findsOneWidget);
    await tester.tap(find.byKey(const Key('atendida_1024')));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('programar_1024')), findsOneWidget);
  });

  testWidgets('cirugías: línea de tiempo y mis cirugías sin desbordes en celular', (tester) async {
    final fichas = FakeFichasInternasService(reloj: _ahora);
    final cirugias = FakeCirugiasService(fichas, reloj: _ahora);

    await _mostrar(tester, CirugiasView(medico: _vargas, servicio: cirugias, reloj: _ahora),
        tamano: const Size(390, 844));

    expect(find.text('Quirófano G-1'), findsOneWidget);
    expect(find.text('Quirófano G-2'), findsOneWidget);
    expect(find.byKey(const Key('realizada_52')), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('celular (390 px): agenda, atención, ticket interno y fichas sin desbordes', (tester) async {
    final fichas = FakeFichasInternasService(reloj: _ahora);
    final agenda = AgendaMedicoController(FakeAgendaMedicoService(reloj: _ahora), fichas: fichas, reloj: _ahora);
    final pantallas = <Widget>[
      AgendaMedicoView(agenda: agenda, medico: _general),
      AtencionView(citaId: 503, servicio: FakeAtencionService(reloj: _ahora), historial: FakeHistorialPacienteService()),
      TicketInternoView(
          medico: _general, agenda: agenda, fichas: fichas, historial: FakeHistorialPacienteService(), citaId: 503),
      FichasInternasView(medico: _vargas, fichas: fichas, reloj: _ahora),
    ];
    for (final p in pantallas) {
      await _mostrar(tester, p, tamano: const Size(390, 844));
      expect(tester.takeException(), isNull, reason: '${p.runtimeType} se desborda en celular');
    }
  });

  group('router del médico', () {
    tearDown(() {
      sesionApp.sesion = null;
      medicoApp.olvidar();
    });

    Future<void> abrir(WidgetTester tester, String ruta) async {
      _tamano(tester, const Size(1440, 900));
      await tester
          .pumpWidget(MaterialApp(navigatorKey: navegadorApp, initialRoute: ruta, onGenerateRoute: generarRuta));
      await tester.pump();
    }

    testWidgets('sin sesión, /medico/agenda muestra el login', (tester) async {
      await abrir(tester, RutasMedico.agenda);

      expect(find.byType(LoginView), findsOneWidget);
    });

    testWidgets('el especialista que escribe /medico/agenda ve su agenda de fichas internas', (tester) async {
      sesionApp.sesion =
          const Sesion(usuarioId: 121, rol: Rol.medico, personaId: 21, nombre: 'Raúl Vargas', token: 't');
      medicoApp.medico = _vargas;

      await abrir(tester, RutasMedico.agenda);
      await tester.pump(const Duration(milliseconds: 50));

      expect(find.byType(MarcoPersonal), findsOneWidget);
      expect(find.byType(FichasInternasView), findsOneWidget);
      expect(find.byType(AgendaMedicoView), findsNothing);
      expect(find.text('Especialista · Raúl Vargas · Gastroenterología'), findsOneWidget);
      expect(find.text('Cirugías'), findsOneWidget);
    });

    testWidgets('enfermería no entra a las pantallas del médico', (tester) async {
      sesionApp.sesion =
          const Sesion(usuarioId: 2, rol: Rol.enfermeria, personaId: 2, nombre: 'María Flores', token: 't');

      await abrir(tester, RutasMedico.cirugias);

      expect(find.byType(LoginView), findsOneWidget);
      expect(find.byType(CirugiasView), findsNothing);
    });
  });
}
