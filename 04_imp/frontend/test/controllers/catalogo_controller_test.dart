import 'package:cns_app/controllers/catalogo_controller.dart';
import 'package:cns_app/models/catalogo/consultorio.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/catalogo/horario.dart';
import 'package:cns_app/models/catalogo/medico.dart';
import 'package:cns_app/services/fake_catalogo_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

Medico _pediatra({int cupo = 6, int duracion = 30}) => Medico(
      nombres: 'Juan',
      apellidos: 'Perez',
      especialidad: Especialidad.pediatria,
      duracionCitaMin: duracion,
      cupoMaximoDia: cupo,
    );

void main() {
  late FakeCatalogoService servicio;
  late CatalogoController c;

  setUp(() async {
    servicio = FakeCatalogoService();
    c = CatalogoController(servicio);
    await c.cargar();
  });

  test('[3a] cupo 8: no guarda y explica el rango', () async {
    final error = await c.guardarMedico(_pediatra(cupo: 8));

    expect(error, 'El cupo diario debe estar entre 5 y 7 fichas.');
    expect(c.medicos, isEmpty);
  });

  test('[3a] duracion fuera del rango de la especialidad', () async {
    final error = await c.guardarMedico(_pediatra(duracion: 50));

    expect(error, contains('30 y 45'));
  });

  test('crear medico valido lo agrega a la lista', () async {
    final error = await c.guardarMedico(_pediatra());

    expect(error, isNull);
    expect(c.medicos.single.activo, isTrue);
  });

  test('[3b] baja con citas programadas: no se aplica', () async {
    await c.guardarMedico(_pediatra());
    servicio.medicosConCitas.add(c.medicos.single.id);

    final error = await c.darDeBajaMedico(c.medicos.single.id);

    expect(error, 'No se aplicó el cambio: hay citas programadas afectadas.');
    expect(c.medicos.single.activo, isTrue);
  });

  test('horarios Lun–Vie se muestran en una sola fila', () async {
    await c.guardarMedico(_pediatra());
    await c.guardarConsultorio(const Consultorio(nombre: 'C1'));

    final error = await c.guardarGrupoHorario(
      medicoId: c.medicos.single.id,
      consultorioId: c.consultorios.single.id,
      diasElegidos: {DiaSemana.lunes, DiaSemana.martes, DiaSemana.miercoles, DiaSemana.jueves, DiaSemana.viernes},
      inicio: const TimeOfDay(hour: 8, minute: 0),
      fin: const TimeOfDay(hour: 12, minute: 0),
    );

    expect(error, isNull);
    expect(c.horarios, hasLength(5));
    expect(c.gruposHorario.single.diasTexto, 'Lun–Vie');
  });

  test('editar un grupo: quita viernes (baja logica) y agrega sabado', () async {
    await c.guardarMedico(_pediatra());
    await c.guardarConsultorio(const Consultorio(nombre: 'C1'));
    final medico = c.medicos.single.id;
    final consultorio = c.consultorios.single.id;
    const inicio = TimeOfDay(hour: 8, minute: 0);
    const fin = TimeOfDay(hour: 12, minute: 0);
    await c.guardarGrupoHorario(
        medicoId: medico,
        consultorioId: consultorio,
        diasElegidos: {DiaSemana.lunes, DiaSemana.viernes},
        inicio: inicio,
        fin: fin);

    final error = await c.guardarGrupoHorario(
        anterior: c.gruposHorario.single,
        medicoId: medico,
        consultorioId: consultorio,
        diasElegidos: {DiaSemana.lunes, DiaSemana.sabado},
        inicio: inicio,
        fin: fin);

    expect(error, isNull);
    expect(c.gruposHorario.single.dias, {DiaSemana.lunes, DiaSemana.sabado});
    expect(c.horarios.where((h) => !h.activo).single.dia, DiaSemana.viernes);
  });

  test('horario que se cruza: HORARIO_SOLAPADO', () async {
    await c.guardarMedico(_pediatra());
    await c.guardarConsultorio(const Consultorio(nombre: 'C1'));
    Future<String?> franja(int desde, int hasta) => c.guardarGrupoHorario(
        medicoId: c.medicos.single.id,
        consultorioId: c.consultorios.single.id,
        diasElegidos: {DiaSemana.lunes},
        inicio: TimeOfDay(hour: desde, minute: 0),
        fin: TimeOfDay(hour: hasta, minute: 0));
    await franja(8, 12);

    final error = await franja(11, 14);

    expect(error, 'El horario se cruza con otro del mismo médico o consultorio.');
  });

  test('textoDias: dias sueltos separados por coma', () {
    expect(textoDias({DiaSemana.viernes, DiaSemana.lunes, DiaSemana.miercoles}), 'Lun, Mié, Vie');
  });
}
