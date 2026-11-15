import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/comun/fechas_iso.dart';
import '../models/comun/texto.dart';
import '../models/medico/cirugia.dart';
import '../models/medico/ficha_interna.dart';
import '../models/medico/medico_actual.dart';
import 'fichas_internas_service.dart';

/// CU-24 (cirugías). PENDIENTE T100-23: el backend todavía no tiene estos endpoints ni sus tablas,
/// por eso la app usa [FakeCirugiasService] (en memoria). La regla de cero choques la garantizará
/// la BD.
abstract class CirugiasService {
  /// "Proponer horario": calcula el primer hueco sin choques; no guarda nada.
  Future<PropuestaCirugia> proponer(DatosCirugia datos, MedicoActual medico);

  /// "Confirmar": guarda la propuesta; si alguien tomó ese horario lanza CHOQUE_DE_HORARIO.
  Future<Cirugia> confirmar(DatosCirugia datos, PropuestaCirugia propuesta, MedicoActual medico);

  /// Cirugías del día en los quirófanos de la especialidad.
  Future<List<Cirugia>> delDia(MedicoActual medico, String fecha);
  Future<List<Cirugia>> delPaciente(int pacienteId);
  Future<Cirugia?> porNumero(int numero);
  Future<Cirugia> marcar(int numero, EstadoCirugia estado);

  /// Quirófanos de la especialidad (para la línea de tiempo).
  List<String> quirofanosDe(Especialidad especialidad);
}

/// Quirófanos de prueba: no se comparten entre especialidades.
const List<QuirofanoEspecialidad> _quirofanos = [
  QuirofanoEspecialidad('G-1', Especialidad.gastroenterologia, 40),
  QuirofanoEspecialidad('G-2', Especialidad.gastroenterologia, 30),
  QuirofanoEspecialidad('H-1', Especialidad.hematologia, 40),
  QuirofanoEspecialidad('D-1', Especialidad.dermatologia, 30),
];

const int _diasHabilesMaximos = 15;

typedef _Hueco = ({String quirofano, String fecha, String inicio, String fin, String limpiezaHasta, int limpiezaMin});

/// Cirugías en memoria con los datos del Figma. Cero choques: quirófano (incluida la limpieza),
/// cirujano y paciente. Una cirugía urgente se busca desde ahora (también fuera de horario) y, si
/// no hay hueco hoy, desplaza a la primera cirugía no urgente de hoy que todavía no empezó.
class FakeCirugiasService implements CirugiasService {
  FakeCirugiasService(this._fichas, {DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now {
    cirugias.addAll(_semilla(hoyIso(_reloj())));
    _siguiente = cirugias.map((c) => c.numero + 1).fold(58, (a, b) => a > b ? a : b);
  }

  final FichasInternasService _fichas;
  final DateTime Function() _reloj;
  final List<Cirugia> cirugias = [];
  int _siguiente = 58;

  @override
  List<String> quirofanosDe(Especialidad especialidad) => [
        for (final q in _quirofanos)
          if (q.especialidad == especialidad) q.codigo
      ];

  @override
  Future<PropuestaCirugia> proponer(DatosCirugia d, MedicoActual medico) async {
    final esp = medico.especialidad;
    if (!esp.operaEnQuirofano) {
      throw ApiException('ESPECIALIDAD_NO_OPERA', '${esp.nombre} no opera en quirófano.', estado: 422);
    }
    final ficha = await _fichas.porTicket(d.fichaTicket);
    if (ficha == null || ficha.estado != EstadoFicha.atendida || !mismoNombre(ficha.medico, medico.nombre)) {
      throw const ApiException(
          'FICHA_NO_ATENDIDA', 'Solo el especialista que atendió la ficha puede programar la cirugía.',
          estado: 422);
    }
    if (d.duracionMin < esp.cirugiaMin || d.duracionMin > esp.cirugiaMax) {
      throw ApiException('DURACION_FUERA_DE_RANGO', '${esp.nombre}: de ${esp.cirugiaMin} a ${esp.cirugiaMax} min.',
          estado: 422);
    }
    final quirofanos = _quirofanos.where((q) => q.especialidad == esp).toList();
    final ahora = _reloj();
    if (d.urgente) {
      final desde = sumarMinutos(horaActual(ahora), 10 - ahora.minute % 10);
      final hoy = hoyIso(ahora);
      final hueco = _hueco(quirofanos, hoy, desde, '23:59', d.duracionMin, medico.nombre, ficha.pacienteId);
      if (hueco != null) {
        return _propuesta(hueco, esp);
      }
      return _desplazando(quirofanos, hoy, desde, d.duracionMin, esp);
    }
    var fecha = hoyIso(ahora);
    for (var i = 0; i < _diasHabilesMaximos; i++) {
      fecha = sumarDias(fecha, 1, habiles: true);
      final hueco =
          _hueco(quirofanos, fecha, quirofanoAbre, quirofanoCierra, d.duracionMin, medico.nombre, ficha.pacienteId);
      if (hueco != null) {
        return _propuesta(hueco, esp);
      }
    }
    throw const ApiException('SIN_HUECO', 'No hay quirófano libre en los próximos 15 días hábiles.', estado: 409);
  }

  @override
  Future<Cirugia> confirmar(DatosCirugia d, PropuestaCirugia p, MedicoActual medico) async {
    final ficha = await _fichas.porTicket(d.fichaTicket);
    if (ficha == null) {
      throw const ApiException('NO_ENCONTRADA', 'La ficha no existe', estado: 404);
    }
    final quirofano = _quirofanos.firstWhere((q) => q.codigo == p.quirofano);
    final desplazada = p.desplaza == null ? -1 : cirugias.indexWhere((c) => c.numero == p.desplaza!.cirugia.numero);
    final choca = _ocupado(p.quirofano, p.fecha, p.inicio, p.limpiezaHasta, medico.nombre, ficha.pacienteId,
        ignorar: desplazada < 0 ? null : cirugias[desplazada].numero);
    if (choca) {
      throw ApiException(
          'CHOQUE_DE_HORARIO',
          'Otro especialista confirmó ese horario del Quirófano ${p.quirofano} mientras revisabas la propuesta. '
              'No se guardó nada: pide una nueva propuesta.',
          estado: 409);
    }
    if (desplazada >= 0) {
      final c = cirugias[desplazada];
      final nuevo = p.desplaza!.nuevoInicio;
      cirugias[desplazada] = c.copia(
        fecha: p.desplaza!.nuevaFecha,
        inicio: nuevo,
        fin: sumarMinutos(nuevo, c.duracionMin),
        limpiezaHasta: sumarMinutos(nuevo, c.duracionMin + quirofano.limpiezaMin),
      );
    }
    final nueva = Cirugia(
      numero: _siguiente++,
      fichaTicket: ficha.ticket,
      pacienteId: ficha.pacienteId,
      nombrePaciente: ficha.nombrePaciente,
      numeroAfiliado: ficha.numeroAfiliado,
      edad: ficha.edad,
      especialidad: medico.especialidad,
      quirofano: p.quirofano,
      cirujano: medico.nombre,
      fecha: p.fecha,
      inicio: p.inicio,
      fin: p.fin,
      limpiezaHasta: p.limpiezaHasta,
      duracionMin: d.duracionMin,
      urgente: d.urgente,
      gravedad: d.gravedad,
      estado: EstadoCirugia.programada,
    );
    cirugias.add(nueva);
    return nueva;
  }

  @override
  Future<List<Cirugia>> delDia(MedicoActual medico, String fecha) async {
    final codigos = quirofanosDe(medico.especialidad);
    return [
      for (final c in cirugias)
        if (codigos.contains(c.quirofano) && c.fecha == fecha) c
    ]..sort((a, b) => a.inicio.compareTo(b.inicio));
  }

  @override
  Future<List<Cirugia>> delPaciente(int pacienteId) async => [
        for (final c in cirugias)
          if (c.pacienteId == pacienteId) c
      ];

  @override
  Future<Cirugia?> porNumero(int numero) async => cirugias.where((c) => c.numero == numero).firstOrNull;

  @override
  Future<Cirugia> marcar(int numero, EstadoCirugia estado) async {
    final i = cirugias.indexWhere((c) => c.numero == numero);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'La cirugía no existe', estado: 404);
    }
    if (cirugias[i].estado != EstadoCirugia.programada) {
      throw const ApiException('ESTADO_NO_PERMITE', 'La cirugía ya no está programada', estado: 409);
    }
    return cirugias[i] = cirugias[i].copia(estado: estado);
  }

  PropuestaCirugia _propuesta(_Hueco h, Especialidad esp) => PropuestaCirugia(
        quirofano: h.quirofano,
        especialidad: esp,
        fecha: h.fecha,
        inicio: h.inicio,
        fin: h.fin,
        limpiezaHasta: h.limpiezaHasta,
        limpiezaMin: h.limpiezaMin,
      );

  _Hueco? _hueco(List<QuirofanoEspecialidad> quirofanos, String fecha, String desde, String hasta, int duracion,
      String cirujano, int pacienteId) {
    _Hueco? mejor;
    for (final q in quirofanos) {
      for (var inicio = desde; minutosDe(inicio) + duracion <= minutosDe(hasta); inicio = sumarMinutos(inicio, 10)) {
        final limpiezaHasta = sumarMinutos(inicio, duracion + q.limpiezaMin);
        if (!_ocupado(q.codigo, fecha, inicio, limpiezaHasta, cirujano, pacienteId)) {
          if (mejor == null || inicio.compareTo(mejor.inicio) < 0) {
            mejor = (
              quirofano: q.codigo,
              fecha: fecha,
              inicio: inicio,
              fin: sumarMinutos(inicio, duracion),
              limpiezaHasta: limpiezaHasta,
              limpiezaMin: q.limpiezaMin,
            );
          }
          break;
        }
        if (inicio == '23:59') {
          break;
        }
      }
    }
    return mejor;
  }

  PropuestaCirugia _desplazando(
      List<QuirofanoEspecialidad> quirofanos, String hoy, String desde, int duracion, Especialidad esp) {
    final codigos = quirofanos.map((q) => q.codigo).toSet();
    final candidatas = [
      for (final c in cirugias)
        if (codigos.contains(c.quirofano) &&
            c.fecha == hoy &&
            c.estado == EstadoCirugia.programada &&
            !c.urgente &&
            c.inicio.compareTo(desde) >= 0)
          c
    ]..sort((a, b) => a.inicio.compareTo(b.inicio));
    if (candidatas.isEmpty) {
      throw const ApiException('SIN_HUECO', 'No hay quirófano libre ni cirugía que se pueda desplazar hoy.',
          estado: 409);
    }
    final victima = candidatas.first;
    final q = quirofanos.firstWhere((x) => x.codigo == victima.quirofano);
    var nuevaFecha = hoy;
    _Hueco? nuevo;
    for (var i = 0; i < _diasHabilesMaximos && nuevo == null; i++) {
      nuevaFecha = sumarDias(nuevaFecha, 1, habiles: true);
      nuevo = _hueco(
          [q], nuevaFecha, quirofanoAbre, quirofanoCierra, victima.duracionMin, victima.cirujano, victima.pacienteId);
    }
    return PropuestaCirugia(
      quirofano: victima.quirofano,
      especialidad: esp,
      fecha: hoy,
      inicio: victima.inicio,
      fin: sumarMinutos(victima.inicio, duracion),
      limpiezaHasta: sumarMinutos(victima.inicio, duracion + q.limpiezaMin),
      limpiezaMin: q.limpiezaMin,
      desplaza: Desplazamiento(cirugia: victima, nuevaFecha: nuevaFecha, nuevoInicio: nuevo?.inicio ?? quirofanoAbre),
    );
  }

  /// ¿Choca con alguna cirugía viva del quirófano (con limpieza), del cirujano o del paciente?
  bool _ocupado(String quirofano, String fecha, String inicio, String limpiezaHasta, String cirujano, int pacienteId,
      {int? ignorar}) {
    final a = minutosDe(inicio);
    final b = minutosDe(limpiezaHasta);
    return cirugias.any((c) {
      if (c.fecha != fecha || c.estado == EstadoCirugia.cancelada || c.numero == ignorar) {
        return false;
      }
      final mismoQuirofano = c.quirofano == quirofano && a < minutosDe(c.limpiezaHasta) && minutosDe(c.inicio) < b;
      final mismaPersona = (mismoNombre(c.cirujano, cirujano) || c.pacienteId == pacienteId) &&
          a < minutosDe(c.fin) &&
          minutosDe(c.inicio) < b;
      return mismoQuirofano || mismaPersona;
    });
  }
}

List<Cirugia> _semilla(String hoy) {
  Cirugia base({
    required int numero,
    required String nombrePaciente,
    required String quirofano,
    required String inicio,
    required String fin,
    required String limpiezaHasta,
    int fichaTicket = 0,
    int? pacienteId,
    String numeroAfiliado = '',
    Especialidad especialidad = Especialidad.gastroenterologia,
    String cirujano = 'Raúl Vargas',
    String? fecha,
    bool urgente = false,
    Gravedad gravedad = Gravedad.media,
    EstadoCirugia estado = EstadoCirugia.programada,
  }) =>
      Cirugia(
        numero: numero,
        fichaTicket: fichaTicket,
        pacienteId: pacienteId ?? 900 + numero,
        nombrePaciente: nombrePaciente,
        numeroAfiliado: numeroAfiliado,
        edad: 50,
        especialidad: especialidad,
        quirofano: quirofano,
        cirujano: cirujano,
        fecha: fecha ?? hoy,
        inicio: inicio,
        fin: fin,
        limpiezaHasta: limpiezaHasta,
        duracionMin: minutosDe(fin) - minutosDe(inicio),
        urgente: urgente,
        gravedad: gravedad,
        estado: estado,
      );
  return [
    base(
        numero: 50,
        nombrePaciente: 'Ana Quispe',
        quirofano: 'G-1',
        inicio: '07:30',
        fin: '08:30',
        limpiezaHasta: '09:10',
        estado: EstadoCirugia.realizada),
    base(
        numero: 51,
        nombrePaciente: 'Marco Ríos',
        quirofano: 'G-1',
        inicio: '10:00',
        fin: '11:30',
        limpiezaHasta: '12:10',
        cirujano: 'Patricia Arce'),
    base(
        numero: 52,
        nombrePaciente: 'Luis Gómez',
        quirofano: 'G-1',
        inicio: '15:00',
        fin: '16:00',
        limpiezaHasta: '16:40'),
    base(
        numero: 53,
        nombrePaciente: 'Lidia Paz',
        quirofano: 'G-2',
        inicio: '09:00',
        fin: '10:00',
        limpiezaHasta: '10:30',
        cirujano: 'Patricia Arce'),
    base(
        numero: 54,
        nombrePaciente: 'Juan Mamani',
        quirofano: 'G-2',
        inicio: '13:30',
        fin: '14:30',
        limpiezaHasta: '15:00',
        urgente: true,
        gravedad: Gravedad.alta,
        pacienteId: 115,
        fichaTicket: 1031),
    base(
        numero: 55,
        nombrePaciente: 'Raúl Choque',
        quirofano: 'G-2',
        inicio: '16:00',
        fin: '17:30',
        limpiezaHasta: '18:00',
        cirujano: 'Patricia Arce'),
    base(
        numero: 40,
        nombrePaciente: 'Marco Ríos',
        pacienteId: 113,
        numeroAfiliado: '4587-12',
        especialidad: Especialidad.dermatologia,
        quirofano: 'D-1',
        fecha: '2026-09-05',
        inicio: '09:00',
        fin: '09:45',
        limpiezaHasta: '10:15',
        cirujano: 'Daniela Salinas',
        estado: EstadoCirugia.realizada),
  ];
}
