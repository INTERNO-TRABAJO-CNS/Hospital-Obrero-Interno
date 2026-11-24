import 'package:cns_app/controllers/historial_paciente_controller.dart';
import 'package:cns_app/controllers/medico_controller.dart';
import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/core/app_router.dart';
import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/medico/historial_paciente.dart';
import 'package:cns_app/models/medico/medico_actual.dart';
import 'package:cns_app/services/cirugias_service.dart';
import 'package:cns_app/services/fake_medico_service.dart';
import 'package:cns_app/services/fichas_internas_service.dart';
import 'package:cns_app/services/medico_service.dart';
import 'package:flutter_test/flutter_test.dart';

MedicoActual _medico({
  Especialidad especialidad = Especialidad.consultorioFamiliar,
  bool puedeDerivar = true,
  bool operaEnQuirofano = false,
  String nombre = 'Carlos Ramos',
}) =>
    MedicoActual(
        medicoId: 1,
        nombre: nombre,
        especialidad: especialidad,
        puedeDerivar: puedeDerivar,
        operaEnQuirofano: operaEnQuirofano);

List<String> _textos(MedicoActual m) => [for (final o in menuMedico(m)) o.texto];

class _MedicoQueFalla implements MedicoService {
  _MedicoQueFalla(this.error);

  final ApiException error;

  @override
  Future<MedicoActual> yo() => Future.error(error);
}

void main() {
  group('historial del paciente (CU-14)', () {
    test('une citas pasadas y recetas en una línea de tiempo, de la más reciente a la más antigua', () async {
      final c = HistorialPacienteController(FakeHistorialPacienteService());

      await c.cargar(100);

      expect(c.eventos.map((e) => e.fecha), ['2026-10-14', '2026-08-02']);
      expect(c.eventos.first.tipo, TipoEvento.atencion);
      expect(c.eventos.first.recetas, ['Losartán 50 mg cada 24 h por 30 días.']);
      expect(c.eventos.last.estado, 'CANCELADA');
      expect(c.eventos.last.lineas, ['Cita cancelada']);
      expect(c.historial!.totalRecetas, 1);
    });

    test('suma fichas internas y cirugías y se puede filtrar', () async {
      final fichas = FakeFichasInternasService();
      final fake = FakeHistorialPacienteService();
      fake.porPaciente[113] =
          const HistorialPaciente(pacienteId: 113, nombrePaciente: 'Marco Ríos', citasPasadas: [], atenciones: []);
      final c = HistorialPacienteController(fake, fichas: fichas, cirugias: FakeCirugiasService(fichas));

      await c.cargar(113);

      expect(c.eventos.map((e) => e.tipo).toSet(), {TipoEvento.cirugia, TipoEvento.fichaInterna});
      c.filtrar(FiltroHistorial.cirugias);
      expect(c.filtrados.map((e) => e.tipo), [TipoEvento.cirugia]);
    });

    test('paciente fuera de su agenda: sin permiso y con el mensaje del backend', () async {
      final c = HistorialPacienteController(FakeHistorialPacienteService());

      await c.cargar(999);

      expect(c.sinPermiso, isTrue);
      expect(c.historial, isNull);
      expect(c.error, contains('Solo puede ver el historial'));
    });
  });

  group('menú según el tipo de médico (Figma)', () {
    test('Médico General: Agenda, Atención, Historial, Ticket interno y Cola de prioridad', () {
      expect(_textos(_medico()), ['Agenda', 'Atención', 'Historial', 'Ticket interno', 'Cola de prioridad']);
    });

    test('Pediatría también deriva', () {
      expect(_textos(_medico(especialidad: Especialidad.pediatria)), contains('Ticket interno'));
    });

    test('Odontología no deriva', () {
      expect(_textos(_medico(especialidad: Especialidad.odontologia, puedeDerivar: false)),
          ['Agenda', 'Atención', 'Historial']);
    });

    test('Especialista: agenda de fichas internas y «Cirugías» solo si opera', () {
      expect(_textos(_medico(especialidad: Especialidad.dermatologia, puedeDerivar: false, operaEnQuirofano: true)),
          ['Agenda de fichas internas', 'Cirugías']);
      expect(_textos(_medico(especialidad: Especialidad.infectologia, puedeDerivar: false)),
          ['Agenda de fichas internas']);
    });

    test('píldora de la barra superior', () {
      expect(_medico().etiqueta, 'Médico General · Carlos Ramos');
      expect(_medico(nombre: 'Raúl Vargas', especialidad: Especialidad.gastroenterologia).etiqueta,
          'Especialista · Raúl Vargas · Gastroenterología');
    });
  });

  group('datos del médico de la sesión (/medico/yo)', () {
    test('con la sesión de un médico de prueba carga sus datos', () async {
      const sesion = Sesion(usuarioId: 121, rol: Rol.medico, personaId: 21, nombre: 'Raúl Vargas', token: 't');
      final c = MedicoController(FakeMedicoService(sesion: () => sesion));

      expect(await c.cargar(), CargaMedico.ok);
      expect(c.medico!.especialidad, Especialidad.gastroenterologia);
      expect(menuMedico(c.medico!).first.texto, 'Agenda de fichas internas');
    });

    test('si el backend no responde NO es un rechazo; un 401 o 403 sí', () async {
      final sinRed = MedicoController(_MedicoQueFalla(const ApiException('SIN_CONEXION', 'No se pudo conectar')));
      final vencida = MedicoController(_MedicoQueFalla(const ApiException('NO_AUTENTICADO', 'Vencida', estado: 401)));

      expect(await sinRed.cargar(), CargaMedico.sinConexion);
      expect(await vencida.cargar(), CargaMedico.rechazada);
      expect(vencida.error, 'Vencida');
    });
  });
}
