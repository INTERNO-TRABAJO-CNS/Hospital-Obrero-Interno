import 'package:flutter/material.dart';

import '../core/api_client.dart';
import '../models/catalogo/consultorio.dart';
import '../models/catalogo/dia_no_habil.dart';
import '../models/catalogo/especialidad.dart';
import '../models/catalogo/horario.dart';
import '../models/catalogo/medico.dart';
import '../services/catalogo_service.dart';

/// Pestañas de la pantalla Catalogos (chips del Figma).
enum SeccionCatalogo { medicos, consultorios, horarios }

/// Horarios de un medico en un consultorio con la misma hora, agrupados por dias
/// (en pantalla: "Lun–Vie 08:00–12:00"). El backend guarda un horario por dia.
class GrupoHorario {
  GrupoHorario(this.horarios);

  final List<Horario> horarios;

  Horario get _primero => horarios.first;
  int get medicoId => _primero.medicoId;
  int get consultorioId => _primero.consultorioId;
  TimeOfDay get inicio => _primero.inicio;
  TimeOfDay get fin => _primero.fin;
  Set<DiaSemana> get dias => {for (final h in horarios) h.dia};

  /// "Lun–Vie", "Lun, Mié, Vie" o "Sáb".
  String get diasTexto => textoDias(dias);
}

/// Dias en orden; si son seguidos (3 o mas) se muestran como rango.
String textoDias(Set<DiaSemana> dias) {
  final orden = DiaSemana.values.where(dias.contains).toList();
  if (orden.isEmpty) {
    return '';
  }
  final seguidos = orden.last.index - orden.first.index == orden.length - 1;
  if (seguidos && orden.length >= 3) {
    return '${orden.first.corto}–${orden.last.corto}';
  }
  return orden.map((d) => d.corto).join(', ');
}

/// Estado de la pantalla de catalogos (MVC: controlador de la vista).
///
/// Solo valida formato y llama al [CatalogoService]; las reglas las aplica el
/// backend. Cada accion devuelve `null` si salio bien o el mensaje para mostrar.
class CatalogoController extends ChangeNotifier {
  CatalogoController(this._servicio);

  final CatalogoService _servicio;

  SeccionCatalogo seccion = SeccionCatalogo.medicos;
  Especialidad? filtroEspecialidad;

  List<Medico> medicos = const [];
  List<Consultorio> consultorios = const [];
  List<Horario> horarios = const [];
  List<DiaNoHabil> dias = const [];

  bool cargando = false;
  String? errorCarga;

  // ------------------------------------------------------------------- lectura

  Future<void> cargar() async {
    cargando = true;
    errorCarga = null;
    notifyListeners();
    try {
      final r = await Future.wait([
        _servicio.listarMedicos(),
        _servicio.listarConsultorios(),
        _servicio.listarHorarios(),
        _servicio.listarDiasNoHabiles(),
      ]);
      medicos = r[0] as List<Medico>;
      consultorios = r[1] as List<Consultorio>;
      horarios = r[2] as List<Horario>;
      dias = r[3] as List<DiaNoHabil>;
    } on ApiException catch (e) {
      errorCarga = mensajeDe(e);
    } finally {
      cargando = false;
      notifyListeners();
    }
  }

  void cambiarSeccion(SeccionCatalogo nueva) {
    seccion = nueva;
    notifyListeners();
  }

  void filtrarPorEspecialidad(Especialidad? especialidad) {
    filtroEspecialidad = especialidad;
    notifyListeners();
  }

  List<Medico> get medicosFiltrados =>
      [for (final m in medicos) if (filtroEspecialidad == null || m.especialidad == filtroEspecialidad) m];

  List<Medico> get medicosActivos => [for (final m in medicos) if (m.activo) m];
  List<Consultorio> get consultoriosActivos => [for (final c in consultorios) if (c.activo) c];
  List<DiaNoHabil> get diasActivos => [for (final d in dias) if (d.activo) d];

  /// Horarios activos agrupados por medico + consultorio + horas.
  List<GrupoHorario> get gruposHorario {
    final grupos = <String, List<Horario>>{};
    for (final h in horarios.where((h) => h.activo)) {
      final clave = '${h.medicoId}|${h.consultorioId}|${horaATexto(h.inicio)}|${horaATexto(h.fin)}';
      grupos.putIfAbsent(clave, () => []).add(h);
    }
    final lista = [for (final g in grupos.values) GrupoHorario(g..sort((a, b) => a.dia.index - b.dia.index))];
    lista.sort((a, b) => nombreMedico(a.medicoId).compareTo(nombreMedico(b.medicoId)));
    return lista;
  }

  String nombreMedico(int id) {
    for (final m in medicos) {
      if (m.id == id) {
        return m.nombreCompleto;
      }
    }
    return 'Médico $id';
  }

  String nombreConsultorio(int id) {
    for (final c in consultorios) {
      if (c.id == id) {
        return c.nombre;
      }
    }
    return 'Consultorio $id';
  }

  // ------------------------------------------------------------------ medicos

  /// Crea si `medico.id == 0`; si no, modifica.
  Future<String?> guardarMedico(Medico medico) => _ejecutar(() async {
        if (medico.id == 0) {
          await _servicio.crearMedico(medico);
        } else {
          await _servicio.modificarMedico(medico.id, medico);
        }
        medicos = await _servicio.listarMedicos();
      });

  Future<String?> darDeBajaMedico(int id) => _ejecutar(() async {
        await _servicio.darDeBajaMedico(id);
        medicos = await _servicio.listarMedicos();
      });

  // ------------------------------------------------------------- consultorios

  Future<String?> guardarConsultorio(Consultorio consultorio) => _ejecutar(() async {
        if (consultorio.id == 0) {
          await _servicio.crearConsultorio(consultorio);
        } else {
          await _servicio.modificarConsultorio(consultorio.id, consultorio);
        }
        consultorios = await _servicio.listarConsultorios();
      });

  Future<String?> darDeBajaConsultorio(int id) => _ejecutar(() async {
        await _servicio.darDeBajaConsultorio(id);
        consultorios = await _servicio.listarConsultorios();
      });

  // ----------------------------------------------------------------- horarios

  /// Guarda una franja para varios dias. Con [anterior] (edicion): modifica los
  /// dias que siguen, da de baja los que se quitaron y crea los nuevos.
  Future<String?> guardarGrupoHorario({
    GrupoHorario? anterior,
    required int medicoId,
    required int consultorioId,
    required Set<DiaSemana> diasElegidos,
    required TimeOfDay inicio,
    required TimeOfDay fin,
  }) {
    return _ejecutar(() async {
      Horario paraDia(DiaSemana dia, [int id = 0]) => Horario(
          id: id, medicoId: medicoId, consultorioId: consultorioId, dia: dia, inicio: inicio, fin: fin);
      final errores = <String>[];
      Future<void> intentar(Future<void> Function() accion) async {
        try {
          await accion();
        } on ApiException catch (e) {
          errores.add(mensajeDe(e));
        }
      }

      final existentes = {for (final h in anterior?.horarios ?? const <Horario>[]) h.dia: h};
      for (final h in existentes.values) {
        if (diasElegidos.contains(h.dia)) {
          await intentar(() => _servicio.modificarHorario(h.id, paraDia(h.dia, h.id)));
        } else {
          await intentar(() => _servicio.darDeBajaHorario(h.id));
        }
      }
      for (final dia in DiaSemana.values) {
        if (diasElegidos.contains(dia) && !existentes.containsKey(dia)) {
          await intentar(() => _servicio.definirHorario(paraDia(dia)));
        }
      }
      horarios = await _servicio.listarHorarios();
      if (errores.isNotEmpty) {
        throw ApiException('VARIOS', errores.toSet().join('\n'));
      }
    });
  }

  Future<String?> darDeBajaGrupoHorario(GrupoHorario grupo) => _ejecutar(() async {
        try {
          for (final h in grupo.horarios) {
            await _servicio.darDeBajaHorario(h.id);
          }
        } finally {
          horarios = await _servicio.listarHorarios();
        }
      });

  // ---------------------------------------------------------- dias no habiles

  Future<String?> guardarDiaNoHabil(DiaNoHabil dia) => _ejecutar(() async {
        if (dia.id == 0) {
          await _servicio.registrarDiaNoHabil(dia);
        } else {
          await _servicio.modificarDiaNoHabil(dia.id, dia);
        }
        dias = await _servicio.listarDiasNoHabiles();
      });

  Future<String?> darDeBajaDiaNoHabil(int id) => _ejecutar(() async {
        await _servicio.darDeBajaDiaNoHabil(id);
        dias = await _servicio.listarDiasNoHabiles();
      });

  // ------------------------------------------------------------------ interno

  Future<String?> _ejecutar(Future<void> Function() accion) async {
    try {
      await accion();
      return null;
    } on ApiException catch (e) {
      return mensajeDe(e);
    } finally {
      notifyListeners();
    }
  }
}

/// Texto para el usuario segun el codigo de error del backend.
String mensajeDe(ApiException e) {
  switch (e.codigo) {
    case 'VALIDACION_CUPO':
      return 'El cupo diario debe estar entre $cupoMinimo y $cupoMaximo fichas.';
    case 'CAMBIO_AFECTA_CITAS':
      return 'No se aplicó el cambio: hay citas programadas afectadas.';
    case 'HORARIO_SOLAPADO':
      return 'El horario se cruza con otro del mismo médico o consultorio.';
    case 'CONSULTORIO_DUPLICADO':
      return 'Ya existe un consultorio con ese nombre.';
    case 'DIA_NO_HABIL_DUPLICADO':
      return 'Esa fecha ya está registrada como día no hábil.';
    case 'VALIDACION_FECHA':
      return 'El día no hábil debe ser una fecha futura.';
    case 'ACCESO_DENEGADO':
      return 'Solo el administrador puede gestionar los catálogos.';
    case 'SIN_CONEXION':
    case 'SIN_RESPUESTA':
      return 'No se pudo conectar con el servidor.';
    default:
      return e.mensaje;
  }
}
