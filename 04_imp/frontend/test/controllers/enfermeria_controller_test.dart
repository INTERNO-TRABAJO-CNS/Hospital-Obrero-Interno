import 'package:cns_app/controllers/enfermeria_controller.dart';
import 'package:cns_app/models/enfermeria/ficha_dia.dart';
import 'package:cns_app/models/historial/cambio_cita.dart';
import 'package:cns_app/services/enfermeria_service.dart';
import 'package:cns_app/services/historial_cambios_service.dart';
import 'package:flutter_test/flutter_test.dart';

/// Reloj que se puede adelantar.
class _Reloj {
  _Reloj(this.ahora);

  DateTime ahora;

  DateTime call() => ahora;
}

void main() {
  late _Reloj reloj;
  late FakeHistorialCambiosService historial;
  late FakeEnfermeriaService servicio;
  late EnfermeriaController c;

  setUp(() async {
    reloj = _Reloj(DateTime(2026, 11, 13, 9, 0));
    historial = FakeHistorialCambiosService(reloj: reloj.call);
    servicio = FakeEnfermeriaService(reloj: reloj.call, historial: historial);
    c = EnfermeriaController(servicio, usuario: 'Enfermería · M. Flores', reloj: reloj.call);
    await c.cargar();
  });

  FichaDelDia ficha(String paciente) => c.fichas.firstWhere((f) => f.paciente == paciente);

  test('carga las 8 fichas de hoy de los tres consultorios', () {
    expect(c.fichas, hasLength(8));
    expect(c.fichas.map((f) => f.medico).toSet(), {'Dr. Ramos', 'Dr. Mamani', 'Dra. Quispe'});
  });

  test('busca por medico (por defecto), paciente, estado y consultorio sin importar tildes', () {
    c.buscar('quispe');
    expect(c.filtradas.map((f) => f.paciente), ['Sofía Vargas', 'Mateo Flores']);

    c.elegirCampo(CampoBusqueda.paciente);
    c.buscar('sofia');
    expect(c.filtradas.single.paciente, 'Sofía Vargas');

    c.elegirCampo(CampoBusqueda.estado);
    c.buscar('asistencia confirmada');
    expect(c.filtradas.single.paciente, 'Elena Copa');

    c.elegirCampo(CampoBusqueda.consultorio);
    c.buscar('2');
    expect(c.filtradas.map((f) => f.medico).toSet(), {'Dr. Mamani'});
  });

  test('por confirmar: solo PROGRAMADA con el plazo abierto, la mas urgente primero', () {
    expect(c.porConfirmar.map((f) => f.paciente), ['Pedro Alanoca', 'Mateo Flores', 'Luis Quispe']);
    // Pedro: cita 09:37, se confirma hasta las 09:07.
    expect(c.minutosRestantes(ficha('Pedro Alanoca')), 7);
  });

  test('confirmar dentro del plazo la deja ASISTENCIA_CONFIRMADA y queda en el historial', () async {
    final error = await c.confirmar(ficha('Mateo Flores'));

    expect(error, isNull);
    expect(ficha('Mateo Flores').estado, EstadoCita.asistenciaConfirmada);
    final cambios = await historial.buscar(const FiltroCambios(usuario: 'M. Flores'));
    expect(cambios.first.accion, 'Asistencia confirmada');
    expect(cambios.first.cita, 'Ficha 2 · Quispe');
  });

  test('pasado el plazo no se confirma y la cita se cancela sola', () async {
    reloj.ahora = DateTime(2026, 11, 13, 9, 10);

    final error = await c.confirmar(ficha('Pedro Alanoca'));

    expect(error, contains('09:07'));
    expect(ficha('Pedro Alanoca').estado, EstadoCita.cancelada);
    final cambios = await historial.buscar(const FiltroCambios(usuario: 'Sistema'));
    expect(cambios.map((c) => c.accion), contains('Cancelada (automática)'));
    expect(c.porConfirmar.map((f) => f.paciente), isNot(contains('Pedro Alanoca')));
  });

  test('una cita ya confirmada no se vuelve a confirmar', () async {
    final error = await c.confirmar(ficha('Elena Copa'));

    expect(error, contains('PROGRAMADA'));
  });
}
