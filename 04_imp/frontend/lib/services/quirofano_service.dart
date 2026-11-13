import 'package:flutter/material.dart' show TimeOfDay;

import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/catalogo/horario.dart';
import '../models/quirofano/quirofano.dart';

/// Catalogo de quirofanos del administrador (Figma "ADMIN CATALOGO QUIROFANOS").
abstract class QuirofanoService {
  Future<List<Quirofano>> listar();

  /// Crea si `id == 0`; si no, modifica. Errores: DATOS_INVALIDOS, QUIROFANO_DUPLICADO,
  /// ESPECIALIDAD_NO_OPERA, HORARIO_INVALIDO, LIMPIEZA_FUERA_DE_RANGO, QUIROFANO_CON_CIRUGIAS.
  Future<Quirofano> guardar(Quirofano quirofano);

  /// No se borra. Con cirugias programadas a futuro -> QUIROFANO_CON_CIRUGIAS.
  Future<void> desactivar(int id);
}

/// En memoria: la tabla de quirofanos llega con T100-17 (todavia no esta en el backend).
class FakeQuirofanoService implements QuirofanoService {
  FakeQuirofanoService() {
    const temprano = TimeOfDay(hour: 8, minute: 0);
    const tarde = TimeOfDay(hour: 16, minute: 0);
    Quirofano q(String n, Especialidad e, int limpieza, int cirugias,
            {TimeOfDay apertura = const TimeOfDay(hour: 7, minute: 0),
            TimeOfDay cierre = const TimeOfDay(hour: 19, minute: 0),
            bool activo = true}) =>
        Quirofano(
          id: ++_ultimoId,
          nombre: n,
          especialidad: e,
          apertura: apertura,
          cierre: cierre,
          limpiezaMin: limpieza,
          cirugiasProgramadas: cirugias,
          activo: activo,
        );
    quirofanos.addAll([
      q('G-1', Especialidad.gastroenterologia, 40, 6),
      q('G-2', Especialidad.gastroenterologia, 30, 4),
      q('H-1', Especialidad.hematologia, 45, 2),
      q('D-1', Especialidad.dermatologia, 30, 3, apertura: temprano, cierre: tarde),
      q('D-2', Especialidad.dermatologia, 30, 0, apertura: temprano, cierre: tarde, activo: false),
    ]);
  }

  final List<Quirofano> quirofanos = [];
  int _ultimoId = 0;

  @override
  Future<List<Quirofano>> listar() async => List.of(quirofanos);

  @override
  Future<Quirofano> guardar(Quirofano q) async {
    final nombre = q.nombre.trim();
    if (nombre.isEmpty) {
      throw const ApiException('DATOS_INVALIDOS', 'El quirófano necesita un nombre', estado: 400);
    }
    if (quirofanos.any((o) => o.id != q.id && o.nombre.toLowerCase() == nombre.toLowerCase())) {
      throw const ApiException('QUIROFANO_DUPLICADO', 'Ya existe un quirófano con ese nombre', estado: 409);
    }
    if (!q.especialidad.operaEnQuirofano) {
      throw const ApiException(
          'ESPECIALIDAD_NO_OPERA', 'Solo Gastroenterología, Hematología y Dermatología operan en quirófano',
          estado: 422);
    }
    if (minutosDe(q.apertura) >= minutosDe(q.cierre)) {
      throw const ApiException('HORARIO_INVALIDO', 'La apertura debe ser antes del cierre', estado: 400);
    }
    if (q.limpiezaMin < limpiezaMinima || q.limpiezaMin > limpiezaMaxima) {
      throw const ApiException(
          'LIMPIEZA_FUERA_DE_RANGO', 'La limpieza debe durar entre $limpiezaMinima y $limpiezaMaxima minutos',
          estado: 422);
    }
    if (q.id == 0) {
      final nuevo = q.copiar(id: ++_ultimoId, nombre: nombre, activo: true);
      quirofanos.add(nuevo);
      return nuevo;
    }
    final i = _indice(q.id);
    final actual = quirofanos[i];
    if (actual.especialidad != q.especialidad && actual.cirugiasProgramadas > 0) {
      throw _conCirugias;
    }
    return quirofanos[i] = actual.copiar(
      nombre: nombre,
      especialidad: q.especialidad,
      apertura: q.apertura,
      cierre: q.cierre,
      limpiezaMin: q.limpiezaMin,
    );
  }

  @override
  Future<void> desactivar(int id) async {
    final i = _indice(id);
    if (quirofanos[i].cirugiasProgramadas > 0) {
      throw _conCirugias;
    }
    quirofanos[i] = quirofanos[i].copiar(activo: false);
  }

  static const _conCirugias = ApiException(
    'QUIROFANO_CON_CIRUGIAS',
    'No se puede cambiar la especialidad ni desactivar un quirófano con cirugías programadas a futuro.',
    estado: 409,
  );

  int _indice(int id) {
    final i = quirofanos.indexWhere((q) => q.id == id);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'Quirófano no encontrado', estado: 404);
    }
    return i;
  }
}
