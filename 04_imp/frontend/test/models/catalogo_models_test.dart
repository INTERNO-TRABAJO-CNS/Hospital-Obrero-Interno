import 'package:cns_app/models/catalogo/dia_no_habil.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/catalogo/horario.dart';
import 'package:cns_app/models/catalogo/medico.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Medico: lee y escribe el JSON del backend', () {
    final m = Medico.desdeJson({
      'medicoId': 3,
      'nombres': 'Ana',
      'apellidos': 'Quispe',
      'especialidad': 'PEDIATRIA',
      'duracionCitaMin': 35,
      'cupoMaximoDia': 6,
      'activo': true,
    });

    expect(m.especialidad, Especialidad.pediatria);
    expect(m.nombreCompleto, 'Ana Quispe');
    expect(m.aJson()['especialidad'], 'PEDIATRIA');
    expect(m.aJson()['cupoMaximoDia'], 6);
  });

  test('Horario: acepta "08:00:00" del backend y envia "08:00"', () {
    final h = Horario.desdeJson({
      'horarioId': 1,
      'medicoId': 2,
      'consultorioId': 4,
      'diaSemana': 'WEDNESDAY',
      'horaInicio': '08:00:00',
      'horaFin': '12:30:00',
      'activo': true,
    });

    expect(h.dia, DiaSemana.miercoles);
    expect(h.fin, const TimeOfDay(hour: 12, minute: 30));
    expect(h.aJson()['horaInicio'], '08:00');
    expect(h.aJson()['diaSemana'], 'WEDNESDAY');
  });

  test('DiaNoHabil: fecha sin hora', () {
    final d = DiaNoHabil(fecha: DateTime(2026, 12, 5), descripcion: 'Asueto');

    expect(d.aJson()['fecha'], '2026-12-05');
    expect(DiaNoHabil.desdeJson(d.aJson()).fecha, DateTime(2026, 12, 5));
  });
}
