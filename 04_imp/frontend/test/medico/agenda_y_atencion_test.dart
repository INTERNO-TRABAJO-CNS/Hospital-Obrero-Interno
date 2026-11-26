import 'package:cns_app/controllers/atencion_controller.dart';
import 'package:cns_app/controllers/medico_controller.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/enfermeria/ficha_dia.dart';
import 'package:cns_app/models/medico/agenda_medico.dart';
import 'package:cns_app/models/medico/medico_actual.dart';
import 'package:cns_app/services/fake_medico_service.dart';
import 'package:flutter_test/flutter_test.dart';

/// Martes 10 de noviembre de 2026, 09:00.
DateTime _ahora() => DateTime(2026, 11, 10, 9);
const _hoy = '2026-11-10';

FichaAgenda _ficha(int citaId, EstadoCita estado) => FichaAgenda(
    citaId: citaId,
    numeroFicha: citaId,
    hora: '0$citaId:00:00',
    nombrePaciente: 'P$citaId',
    estado: estado,
    pacienteId: 100 + citaId);

const _general = MedicoActual(
    medicoId: 7,
    nombre: 'Rosa',
    especialidad: Especialidad.consultorioFamiliar,
    puedeDerivar: true,
    operaEnQuirofano: false);
const _odontologo = MedicoActual(
    medicoId: 9, nombre: 'Jorge', especialidad: Especialidad.odontologia, puedeDerivar: false, operaEnQuirofano: false);

void main() {
  group('agenda del médico (CU-13)', () {
    late FakeAgendaMedicoService fake;
    late AgendaMedicoController agenda;

    setUp(() {
      fake = FakeAgendaMedicoService(reloj: _ahora);
      fake.porFecha[_hoy] = [
        _ficha(1, EstadoCita.atendida),
        _ficha(2, EstadoCita.asistenciaConfirmada),
        _ficha(3, EstadoCita.cancelada)
      ];
      agenda = AgendaMedicoController(fake, reloj: _ahora);
    });

    test('cuenta las citas de hoy (también las canceladas)', () async {
      await agenda.cargar(_hoy);

      expect(agenda.titulo, '3 citas hoy');
      expect(agenda.resumen, 'Martes 10 de noviembre · 3 citas hoy');
    });

    test('otra fecha cambia el encabezado y pide esa fecha', () async {
      await agenda.cargar('2026-11-12');

      expect(fake.llamadas, ['2026-11-12']);
      expect(agenda.titulo, '0 citas este día');
      expect(agenda.fechaElegida, '2026-11-12');
    });

    test('la cancelada sigue en la lista con su estado', () async {
      await agenda.cargar(_hoy);

      expect(agenda.agenda!.fichas.firstWhere((f) => f.citaId == 3).estado, EstadoCita.cancelada);
    });

    test('«Atender» solo con asistencia confirmada', () async {
      await agenda.cargar(_hoy);
      final [atendida, confirmada, cancelada] = agenda.agenda!.fichas;

      expect(agenda.puedeAtender(confirmada), isTrue);
      expect(agenda.puedeAtender(atendida), isFalse);
      expect(agenda.puedeAtender(cancelada), isFalse);
    });

    test('«Ticket interno» solo si el médico deriva', () async {
      await agenda.cargar(_hoy);
      final [atendida, confirmada, cancelada] = agenda.agenda!.fichas;

      expect(agenda.puedeTicketInterno(confirmada, _general), isTrue);
      expect(agenda.puedeTicketInterno(atendida, _general), isTrue);
      expect(agenda.puedeTicketInterno(cancelada, _general), isFalse);
      expect(agenda.puedeTicketInterno(confirmada, _odontologo), isFalse);
    });

    test('«Ticket interno» solo para citas de hoy', () async {
      fake.porFecha['2026-11-09'] = [_ficha(4, EstadoCita.atendida)];
      await agenda.cargar('2026-11-09');

      expect(agenda.puedeTicketInterno(agenda.agenda!.fichas.first, _general), isFalse);
    });
  });

  group('atención del médico (CU-15)', () {
    late FakeAtencionService fake;
    late AtencionController atencion;

    setUp(() {
      fake = FakeAtencionService(reloj: _ahora);
      atencion = AtencionController(fake);
    });

    test('una receta vacía no se envía', () async {
      await atencion.cargar(503);
      atencion.escribir('   ');

      expect(atencion.puedeFinalizar, isFalse);
      expect(await atencion.finalizar(), isFalse);
      expect(fake.registradas, isEmpty);
    });

    test('una receta de más de 2000 caracteres no se envía', () async {
      await atencion.cargar(503);
      atencion.escribir('x' * 2001);

      expect(atencion.puedeFinalizar, isFalse);
    });

    test('finalizar envía la receta sin espacios y marca la cita ATENDIDA', () async {
      await atencion.cargar(503);
      atencion.escribir('  Amoxicilina 500 mg c/8 h por 7 días  ');

      expect(await atencion.finalizar(), isTrue);
      expect(fake.registradas, [(503, 'Amoxicilina 500 mg c/8 h por 7 días')]);
      expect(atencion.finalizada, isTrue);
      expect(atencion.datos!.estado, EstadoCita.atendida);
      expect(atencion.puedeFinalizar, isFalse);
    });

    test('una cita inexistente muestra el error del backend', () async {
      await atencion.cargar(404);

      expect(atencion.datos, isNull);
      expect(atencion.error, 'La cita no existe');
    });
  });
}
