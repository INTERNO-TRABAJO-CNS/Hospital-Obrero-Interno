import 'dart:convert';

import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/historial/cambio_cita.dart';
import 'package:cns_app/models/personal/cuenta.dart';
import 'package:cns_app/models/quirofano/quirofano.dart';
import 'package:cns_app/models/reportes/reporte.dart';
import 'package:cns_app/models/supervision/supervision.dart';
import 'package:cns_app/services/cuentas_service.dart';
import 'package:cns_app/services/enfermeria_service.dart';
import 'package:cns_app/services/historial_cambios_service.dart';
import 'package:cns_app/services/quirofano_service.dart';
import 'package:cns_app/services/reportes_service.dart';
import 'package:cns_app/services/supervision_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

Matcher _codigo(String codigo) => throwsA(isA<ApiException>().having((e) => e.codigo, 'codigo', codigo));

void main() {
  DateTime reloj() => DateTime(2026, 11, 13, 9, 0);

  group('ApiCuentasService', () {
    test('lista, crea y da de baja con el token en /api/v1/admin/usuarios', () async {
      final peticiones = <http.Request>[];
      final api = ApiClient(
        urlBase: 'http://x',
        cliente: MockClient((p) async {
          peticiones.add(p);
          final cuenta = {
            'usuarioId': 9,
            'usuario': 'mflores',
            'rol': 'ENFERMERIA',
            'personaId': 0,
            'nombre': 'María Flores',
            'activo': true,
          };
          return switch (p.method) {
            'GET' =>
              http.Response(jsonEncode([cuenta]), 200, headers: {'content-type': 'application/json; charset=utf-8'}),
            'POST' =>
              http.Response(jsonEncode(cuenta), 201, headers: {'content-type': 'application/json; charset=utf-8'}),
            _ => http.Response('', 204),
          };
        }),
      )..token = 't0k';
      final servicio = ApiCuentasService(api);

      final lista = await servicio.listar();
      await servicio
          .crear(const NuevaCuenta(usuario: 'mflores', clave: 'segura1', rol: Rol.enfermeria, nombre: 'María Flores'));
      await servicio.darDeBaja(9);

      expect(lista.single.rol, Rol.enfermeria);
      expect(peticiones.map((p) => '${p.method} ${p.url.path}'),
          ['GET /api/v1/admin/usuarios', 'POST /api/v1/admin/usuarios', 'DELETE /api/v1/admin/usuarios/9']);
      expect(peticiones.every((p) => p.headers['Authorization'] == 'Bearer t0k'), isTrue);
      expect(jsonDecode(peticiones[1].body)['rol'], 'ENFERMERIA');
    });
  });

  group('FakeQuirofanoService', () {
    test('rechaza especialidad que no opera, horario invertido y nombre repetido', () async {
      final s = FakeQuirofanoService();

      await expectLater(
          s.guardar(const Quirofano(nombre: 'C-1', especialidad: Especialidad.cardiologia, limpiezaMin: 30)),
          _codigo('ESPECIALIDAD_NO_OPERA'));
      await expectLater(
          s.guardar(const Quirofano(
              nombre: 'G-9',
              especialidad: Especialidad.gastroenterologia,
              apertura: TimeOfDay(hour: 19, minute: 0),
              cierre: TimeOfDay(hour: 7, minute: 0),
              limpiezaMin: 30)),
          _codigo('HORARIO_INVALIDO'));
      await expectLater(
          s.guardar(const Quirofano(nombre: 'g-1', especialidad: Especialidad.gastroenterologia, limpiezaMin: 30)),
          _codigo('QUIROFANO_DUPLICADO'));
    });

    test('con cirugias programadas no cambia de especialidad; sin cirugias si se desactiva', () async {
      final s = FakeQuirofanoService();
      final g1 = s.quirofanos.first;

      await expectLater(
          s.guardar(g1.copiar(especialidad: Especialidad.hematologia)), _codigo('QUIROFANO_CON_CIRUGIAS'));
      final nuevo =
          await s.guardar(const Quirofano(nombre: 'H-2', especialidad: Especialidad.hematologia, limpiezaMin: 45));
      await s.desactivar(nuevo.id);

      expect(s.quirofanos.last.activo, isFalse);
    });
  });

  group('FakeSupervisionService', () {
    test('citas comunes: totales y una fila por medico mas la ficha en atencion', () async {
      final enfermeria = FakeEnfermeriaService(reloj: reloj);
      final s = FakeSupervisionService(enfermeria: enfermeria, quirofanos: FakeQuirofanoService(), reloj: reloj);

      final todas = await s.citasComunes();
      final pediatria = await s.citasComunes(especialidad: Especialidad.pediatria);

      expect(todas.fichasHoy, 8);
      expect(todas.atendidas, 3);
      expect(todas.confirmadas, 1);
      expect(todas.canceladas, 1);
      expect(todas.filas.where((f) => !f.detalle).map((f) => f.medico), ['Dr. Ramos', 'Dr. Mamani', 'Dra. Quispe']);
      expect(todas.filas.where((f) => f.detalle).single.fichasOPaciente, 'Elena Copa');
      expect(pediatria.fichasHoy, 2);
    });

    test('fichas internas: Gastroenterologia General trae las filas del Figma, sin gravedad', () async {
      final s = FakeSupervisionService(
          enfermeria: FakeEnfermeriaService(reloj: reloj), quirofanos: FakeQuirofanoService(), reloj: reloj);

      final r = await s.fichasInternas(Especialidad.gastroenterologia, TablaInterna.general);
      final pediatria = await s.fichasInternas(Especialidad.cardiologia, TablaInterna.pediatria);

      expect(r.fichas, hasLength(7));
      expect(r.contar(EstadoFichaInterna.desplazada), 1);
      // Hoy es viernes 13: la ficha desplazada pasa al lunes 16, no a un fin de semana.
      expect(r.fichas.firstWhere((f) => f.estado == EstadoFichaInterna.desplazada).detalle, 'pasó al 16 nov 11:00');
      expect(pediatria.fichas, isNotEmpty);
      expect(pediatria.fichas.every((f) => f.derivadoPor == 'Dra. Quispe'), isTrue);
    });

    test('quirofanos: solo los activos y las horas usadas sin contar la limpieza', () async {
      final s = FakeSupervisionService(
          enfermeria: FakeEnfermeriaService(reloj: reloj), quirofanos: FakeQuirofanoService(), reloj: reloj);

      final r = await s.quirofanos();

      expect(r.quirofanos.map((q) => q.codigo), ['G-1', 'G-2', 'H-1', 'D-1']);
      expect(r.cirugiasHoy, 8);
      expect(r.urgentes, 1);
      expect(r.horasUsadas, 9.75);
      expect(r.horasAbiertas, 44);
    });
  });

  group('FakeReportesService', () {
    test('por medico marca en rojo a quien queda bajo la meta de 5 fichas', () async {
      final r = await FakeReportesService(reloj: reloj).reporte(TipoReporte.porMedico);

      final mamani = r.filas.firstWhere((f) => f.etiqueta == 'Dr. Mamani');
      expect(mamani.alerta, isTrue);
      expect(mamani.texto, '4 fichas · bajo la meta');
      expect(r.filas.where((f) => f.alerta), hasLength(1));
    });

    test('cada pestaña tiene grafico, 4 indicadores y nota', () async {
      final s = FakeReportesService(reloj: reloj);
      for (final tipo in TipoReporte.values) {
        final r = await s.reporte(tipo);
        expect(r.filas, isNotEmpty, reason: tipo.name);
        expect(r.indicadores, hasLength(4), reason: tipo.name);
      }
      expect((await s.reporte(TipoReporte.internasYQuirofanos)).titulo, contains('del 9 al 13 de noviembre'));
    });
  });

  group('FakeHistorialCambiosService', () {
    test('filtra por cita, usuario y fechas, del mas nuevo al mas viejo', () async {
      final h = FakeHistorialCambiosService(reloj: reloj);

      final todos = await h.buscar(const FiltroCambios());
      final ramos = await h.buscar(const FiltroCambios(cita: 'ramos'));
      final ayer = await h.buscar(FiltroCambios(desde: DateTime(2026, 11, 12), hasta: DateTime(2026, 11, 12)));

      expect(todos.first.fecha.isAfter(todos.last.fecha), isTrue);
      expect(ramos, hasLength(2));
      expect(ayer.map((c) => c.accion), ['Cancelada por paciente', 'Atención finalizada']);
    });
  });
}
