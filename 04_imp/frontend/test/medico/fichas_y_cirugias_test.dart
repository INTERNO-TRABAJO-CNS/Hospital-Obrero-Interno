import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/medico/cirugia.dart';
import 'package:cns_app/models/medico/ficha_interna.dart';
import 'package:cns_app/models/medico/medico_actual.dart';
import 'package:cns_app/services/cirugias_service.dart';
import 'package:cns_app/services/fichas_internas_service.dart';
import 'package:flutter_test/flutter_test.dart';

/// Lunes 16 de noviembre de 2026, 09:00 (hora local).
DateTime _ahora() => DateTime(2026, 11, 16, 9);
const _hoy = '2026-11-16';

const _general = MedicoActual(
    medicoId: 7,
    nombre: 'Carlos Ramos',
    especialidad: Especialidad.consultorioFamiliar,
    puedeDerivar: true,
    operaEnQuirofano: false);
const _pediatra = MedicoActual(
    medicoId: 8,
    nombre: 'Lucía Quispe',
    especialidad: Especialidad.pediatria,
    puedeDerivar: true,
    operaEnQuirofano: false);
const _odontologo = MedicoActual(
    medicoId: 9,
    nombre: 'Jorge Mamani',
    especialidad: Especialidad.odontologia,
    puedeDerivar: false,
    operaEnQuirofano: false);
const _vargas = MedicoActual(
    medicoId: 21,
    nombre: 'Raúl Vargas',
    especialidad: Especialidad.gastroenterologia,
    puedeDerivar: false,
    operaEnQuirofano: true);

CitaOrigen _cita({String fecha = _hoy, String estado = 'ASISTENCIA_CONFIRMADA'}) =>
    CitaOrigen(citaId: 503, numeroFicha: 3, fecha: fecha, hora: '09:30:00', estado: estado);

SolicitudFicha _solicitud({
  int? citaOrigenId = 503,
  int pacienteId = 102,
  int edad = 34,
  Especialidad especialidad = Especialidad.gastroenterologia,
  bool urgencia = false,
  String motivo = 'Dolor abdominal persistente',
}) =>
    SolicitudFicha(
      citaOrigenId: citaOrigenId,
      pacienteId: pacienteId,
      nombrePaciente: 'Elena Copa',
      numeroAfiliado: '4587-12',
      edad: edad,
      especialidad: especialidad,
      urgencia: urgencia,
      gravedad: Gravedad.media,
      motivo: motivo,
    );

Future<String?> _codigoDe(Future<Object?> Function() accion) async {
  try {
    await accion();
    return null;
  } on ApiException catch (e) {
    return e.codigo;
  }
}

void main() {
  group('fichas internas (reglas del 10/11/2026)', () {
    test('desde una cita confirmada de hoy: ASIGNADA desde mañana con un gastroenterólogo', () async {
      final s = FakeFichasInternasService(reloj: _ahora);

      final r = await s.generar(_solicitud(), _general, _cita());

      expect(r.rechazada, isFalse);
      expect(r.ficha!.estado, EstadoFicha.asignada);
      expect(r.ficha!.ticket, 1033);
      expect(r.ficha!.fecha!.compareTo(_hoy), greaterThan(0));
      expect(['Raúl Vargas', 'Patricia Arce'], contains(r.ficha!.medico));
      expect(r.ficha!.tabla, TablaFicha.general);
    });

    test('una sola derivación por cita', () async {
      final s = FakeFichasInternasService(reloj: _ahora);
      await s.generar(_solicitud(), _general, _cita());

      final r = await s.generar(_solicitud(pacienteId: 999), _general, _cita());

      expect(r.codigo, 'CITA_YA_DERIVADA');
    });

    test('cada rechazo con su código', () async {
      final s = FakeFichasInternasService(reloj: _ahora);
      Future<String?> codigo(SolicitudFicha sol, MedicoActual m, CitaOrigen c) async =>
          (await s.generar(sol, m, c)).codigo;

      expect(await codigo(_solicitud(), _odontologo, _cita()), 'MEDICO_NO_DERIVA');
      expect(await codigo(_solicitud(especialidad: Especialidad.terapiaIntensiva), _general, _cita()),
          'TERAPIA_SOLO_URGENCIA');
      expect(await codigo(_solicitud(motivo: '  '), _general, _cita()), 'MOTIVO_INVALIDO');
      expect(await codigo(_solicitud(), _general, _cita(fecha: '2026-11-13')), 'CITA_NO_VALIDA');
      expect(await codigo(_solicitud(), _general, _cita(estado: 'PROGRAMADA')), 'CITA_NO_VALIDA');
      expect(await codigo(_solicitud(edad: 12), _pediatra, _cita()), 'EDAD_NO_PEDIATRICA');
      expect(await codigo(_solicitud(pacienteId: 113), _general, _cita()), 'PACIENTE_CON_FICHA_ACTIVA');
    });

    test('sin especialista disponible queda EN_ESPERA', () async {
      final s = FakeFichasInternasService(reloj: _ahora);

      final r = await s.generar(_solicitud(especialidad: Especialidad.endocrinologia), _general, _cita());

      expect(r.ficha!.estado, EstadoFicha.enEspera);
      expect(r.ficha!.fecha, isNull);
    });

    test('urgencia sin cita: se asigna hoy', () async {
      final s = FakeFichasInternasService(reloj: _ahora);

      final r = await s.generar(
          _solicitud(citaOrigenId: null, pacienteId: 103, urgencia: true, especialidad: Especialidad.terapiaIntensiva),
          _general,
          null);

      expect(r.ficha!.estado, EstadoFicha.asignada);
      expect(r.ficha!.fecha, _hoy);
    });

    test('cola: urgencia > gravedad > 60 o más > llegada', () async {
      final s = FakeFichasInternasService(reloj: _ahora);

      final cola = await s.cola(Especialidad.gastroenterologia, TablaFicha.general);

      expect(cola.map((f) => f.nombrePaciente), [
        'Rosa Choque',
        'Juan Mamani',
        'Luis Paz',
        'Ana Rojas',
        'Teresa Ticona',
        'Marco Ríos',
        'Jorge Limachi',
        'Mario Cruz',
      ]);
    });

    test('«No se presentó» solo después de la hora de la ficha', () async {
      final s = FakeFichasInternasService(reloj: _ahora);

      expect(await _codigoDe(() => s.noSePresento(1024)), 'TODAVIA_NO_ES_LA_HORA');
      final atendida = await s.marcarAtendida(1024, '  Úlcera confirmada  ');
      expect(atendida.estado, EstadoFicha.atendida);
      expect(atendida.observaciones, 'Úlcera confirmada');
    });
  });

  group('cirugías (cero choques)', () {
    test('propone el primer hueco desde mañana y confirma', () async {
      final fichas = FakeFichasInternasService(reloj: _ahora);
      final s = FakeCirugiasService(fichas, reloj: _ahora);
      const datos = DatosCirugia(fichaTicket: 1019, duracionMin: 90, gravedad: Gravedad.alta, urgente: false);

      final p = await s.proponer(datos, _vargas);

      expect(
          (p.quirofano, p.fecha, p.inicio, p.fin, p.limpiezaHasta), ('G-1', '2026-11-17', '07:00', '08:30', '09:10'));
      expect(p.desplaza, isNull);
      final c = await s.confirmar(datos, p, _vargas);
      expect(c.numero, 58);
    });

    test('una propuesta vieja que ya se ocupó lanza CHOQUE_DE_HORARIO', () async {
      final fichas = FakeFichasInternasService(reloj: _ahora);
      final s = FakeCirugiasService(fichas, reloj: _ahora);
      const datos = DatosCirugia(fichaTicket: 1019, duracionMin: 90, gravedad: Gravedad.alta, urgente: false);
      final p = await s.proponer(datos, _vargas);
      await s.confirmar(datos, p, _vargas);

      expect(await _codigoDe(() => s.confirmar(datos, p, _vargas)), 'CHOQUE_DE_HORARIO');
    });

    test('rechaza duración fuera de rango y fichas que no atendió', () async {
      final fichas = FakeFichasInternasService(reloj: _ahora);
      final s = FakeCirugiasService(fichas, reloj: _ahora);

      expect(
          await _codigoDe(() => s.proponer(
              const DatosCirugia(fichaTicket: 1019, duracionMin: 200, gravedad: Gravedad.alta, urgente: false),
              _vargas)),
          'DURACION_FUERA_DE_RANGO');
      expect(
          await _codigoDe(() => s.proponer(
              const DatosCirugia(fichaTicket: 1024, duracionMin: 60, gravedad: Gravedad.alta, urgente: false),
              _vargas)),
          'FICHA_NO_ATENDIDA');
    });

    test('una urgencia sin hueco hoy desplaza a la primera cirugía no urgente que no empezó', () async {
      final fichas = FakeFichasInternasService(reloj: _ahora);
      final s = FakeCirugiasService(fichas, reloj: _ahora);
      Cirugia base(int numero, String quirofano, String inicio, String fin, String limpieza, bool urgente) => Cirugia(
            numero: numero,
            fichaTicket: 0,
            pacienteId: 900,
            nombrePaciente: 'P$numero',
            numeroAfiliado: '',
            edad: 50,
            especialidad: Especialidad.gastroenterologia,
            quirofano: quirofano,
            cirujano: 'Patricia Arce',
            fecha: _hoy,
            inicio: inicio,
            fin: fin,
            limpiezaHasta: limpieza,
            duracionMin: 60,
            urgente: urgente,
            gravedad: Gravedad.media,
            estado: EstadoCirugia.programada,
          );
      s.cirugias
        ..clear()
        ..addAll([
          base(1, 'G-1', '09:00', '23:00', '23:40', true),
          base(2, 'G-2', '09:00', '12:00', '12:30', false),
          base(57, 'G-2', '12:30', '23:00', '23:30', false),
        ]);

      final p = await s.proponer(
          const DatosCirugia(fichaTicket: 1019, duracionMin: 60, gravedad: Gravedad.alta, urgente: true), _vargas);

      expect(p.desplaza?.cirugia.numero, 57);
      expect((p.quirofano, p.fecha, p.inicio, p.fin), ('G-2', _hoy, '12:30', '13:30'));
    });
  });
}
