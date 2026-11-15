import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/comun/fechas_iso.dart';
import '../models/comun/texto.dart';
import '../models/medico/ficha_interna.dart';
import '../models/medico/medico_actual.dart';

/// CU-21..CU-23 (fichas internas). PENDIENTE T100-20/21/22: el backend todavía no tiene estos
/// endpoints ni sus tablas, por eso la app usa [FakeFichasInternasService] (en memoria). Cuando
/// existan, se agrega ApiFichasInternasService con esta misma interfaz y solo cambia app_router.dart.
abstract class FichasInternasService {
  /// "Generar": valida, asigna (o deja EN_ESPERA) o rechaza con su código.
  Future<ResultadoFicha> generar(SolicitudFicha solicitud, MedicoActual medico, CitaOrigen? cita);

  /// Ficha generada desde esa cita (una sola derivación por cita).
  Future<FichaInterna?> deCita(int citaId);
  Future<FichaInterna?> porTicket(int ticket);
  Future<List<FichaInterna>> delPaciente(int pacienteId);

  /// Cola de prioridad de una especialidad (solo lectura), ya ordenada.
  Future<List<FichaInterna>> cola(Especialidad especialidad, TablaFicha tabla);

  /// Urgencia sin cita: buscar al paciente por N° de afiliado.
  Future<PacienteBuscado?> buscarPaciente(String numeroAfiliado);

  /// Agenda del especialista: sus fichas de ese día, ordenadas por hora.
  Future<List<FichaInterna>> agendaEspecialista(MedicoActual medico, String fecha);
  Future<FichaInterna> marcarAtendida(int ticket, String observaciones);
  Future<FichaInterna> noSePresento(int ticket);
}

class _Especialista {
  const _Especialista(this.nombre, this.especialidad, this.consultorio, this.duracionMin);

  final String nombre;
  final Especialidad especialidad;
  final String consultorio;
  final int duracionMin;
}

/// Especialistas de prueba (nombres del Figma). Endocrinología no tiene: sus fichas quedan EN_ESPERA.
const List<_Especialista> _especialistas = [
  _Especialista('Raúl Vargas', Especialidad.gastroenterologia, '12', 25),
  _Especialista('Patricia Arce', Especialidad.gastroenterologia, '14', 25),
  _Especialista('Miguel Paredes', Especialidad.cardiologia, '15', 30),
  _Especialista('Daniela Salinas', Especialidad.dermatologia, '16', 20),
  _Especialista('Sonia Pinto', Especialidad.hematologia, '17', 30),
  _Especialista('Óscar Rocha', Especialidad.neumologia, '18', 25),
  _Especialista('Gloria Mendoza', Especialidad.nefrologia, '19', 30),
  _Especialista('Iván Cuéllar', Especialidad.infectologia, '20', 40),
  _Especialista('Rocío Aliaga', Especialidad.medicinaTrabajo, '21', 30),
  _Especialista('Hugo Lima', Especialidad.terapiaIntensiva, 'UTI', 45),
];

/// Pacientes que se pueden buscar por N° de afiliado en una urgencia.
const List<PacienteBuscado> _directorio = [
  PacienteBuscado(pacienteId: 104, nombre: 'Rosa Choque', numeroAfiliado: '7781-03', edad: 68),
  PacienteBuscado(pacienteId: 103, nombre: 'Luis Quispe', numeroAfiliado: '5012-08', edad: 39),
  PacienteBuscado(pacienteId: 120, nombre: 'Mario Cruz', numeroAfiliado: '3310-07', edad: 40),
];

const String _inicioJornada = '08:00';
const String _finJornada = '16:00';
const int _diasHabilesMaximos = 15;

/// Fichas internas en memoria con los datos del Figma (agenda del Dr. Ramos, agenda de fichas del
/// Dr. Vargas y cola de Gastroenterología). Aplica las reglas del 10/11/2026: una derivación por
/// cita, Terapia Intensiva solo con urgencia, Pediatría < 10 años, una ficha activa por paciente y
/// búsqueda de hasta 15 días hábiles. El desplazamiento por prioridad lo decidirá el backend
/// (T100-20); aquí una urgencia toma el siguiente hueco de hoy.
class FakeFichasInternasService implements FichasInternasService {
  FakeFichasInternasService({DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now {
    fichas.addAll(_semilla(hoyIso(_reloj())));
    _siguienteTicket = fichas.map((f) => f.ticket + 1).fold(1033, (a, b) => a > b ? a : b);
  }

  final DateTime Function() _reloj;
  final List<FichaInterna> fichas = [];
  int _siguienteTicket = 1033;

  @override
  Future<ResultadoFicha> generar(SolicitudFicha s, MedicoActual medico, CitaOrigen? cita) async {
    final hoy = hoyIso(_reloj());
    if (!medico.puedeDerivar) {
      return const ResultadoFicha.rechazada('MEDICO_NO_DERIVA', 'Solo Médico General y Pediatría pueden derivar.');
    }
    final motivo = s.motivo.trim();
    if (motivo.isEmpty || motivo.length > maxMotivo) {
      return const ResultadoFicha.rechazada('MOTIVO_INVALIDO', 'El motivo debe tener de 1 a $maxMotivo caracteres.');
    }
    if (s.especialidad == Especialidad.terapiaIntensiva && !s.urgencia) {
      return const ResultadoFicha.rechazada('TERAPIA_SOLO_URGENCIA', 'Terapia Intensiva solo se asigna con Urgencia.');
    }
    if (!s.urgencia) {
      final valida =
          cita != null && cita.fecha == hoy && (cita.estado == 'ASISTENCIA_CONFIRMADA' || cita.estado == 'ATENDIDA');
      if (!valida) {
        return const ResultadoFicha.rechazada(
            'CITA_NO_VALIDA', 'La cita debe ser tuya, de hoy y estar confirmada o atendida.');
      }
    }
    if (cita != null) {
      final previa = fichas.where((f) => f.citaOrigenId == cita.citaId).firstOrNull;
      if (previa != null) {
        return ResultadoFicha.rechazada('CITA_YA_DERIVADA',
            'Esta cita ya tiene una ficha interna (Ticket N° ${previa.ticket}). Solo se permite una derivación por cita.');
      }
    }
    if (medico.especialidad == Especialidad.pediatria && s.edad >= edadPediatria) {
      return const ResultadoFicha.rechazada('EDAD_NO_PEDIATRICA', 'Pediatría solo atiende a menores de 10 años.');
    }
    if (fichas.any((f) => f.pacienteId == s.pacienteId && f.activa)) {
      return const ResultadoFicha.rechazada(
          'PACIENTE_CON_FICHA_ACTIVA', 'El paciente ya tiene una ficha interna activa.');
    }

    var ficha = FichaInterna(
      ticket: _siguienteTicket++,
      citaOrigenId: cita?.citaId,
      pacienteId: s.pacienteId,
      nombrePaciente: s.nombrePaciente,
      numeroAfiliado: s.numeroAfiliado,
      edad: s.edad,
      especialidad: s.especialidad,
      tabla: medico.tabla,
      urgencia: s.urgencia,
      gravedad: s.gravedad,
      motivo: motivo,
      estado: EstadoFicha.enEspera,
      fecha: null,
      hora: null,
      duracionMin: s.especialidad.duracionMin,
      medico: null,
      consultorio: null,
      derivadoPor: medico.nombre,
      especialidadOrigen: medico.especialidad,
      llegada: _reloj(),
    );
    final hueco = s.urgencia ? _huecoHoy(s.especialidad) : _huecoDesdeManana(s.especialidad);
    if (hueco != null) {
      ficha = ficha.copia(
        estado: EstadoFicha.asignada,
        fecha: hueco.fecha,
        hora: hueco.hora,
        medico: hueco.medico.nombre,
        consultorio: hueco.medico.consultorio,
        duracionMin: hueco.medico.duracionMin,
      );
    }
    fichas.add(ficha);
    return ResultadoFicha.generada(ficha);
  }

  @override
  Future<FichaInterna?> deCita(int citaId) async => fichas.where((f) => f.citaOrigenId == citaId).firstOrNull;

  @override
  Future<FichaInterna?> porTicket(int ticket) async => fichas.where((f) => f.ticket == ticket).firstOrNull;

  @override
  Future<List<FichaInterna>> delPaciente(int pacienteId) async => [
        for (final f in fichas)
          if (f.pacienteId == pacienteId) f
      ];

  @override
  Future<List<FichaInterna>> cola(Especialidad especialidad, TablaFicha tabla) async => [
        for (final f in fichas)
          if (f.especialidad == especialidad && f.tabla == tabla && f.activa) f
      ]..sort(compararPrioridad);

  @override
  Future<PacienteBuscado?> buscarPaciente(String numeroAfiliado) async =>
      _directorio.where((p) => p.numeroAfiliado == numeroAfiliado.trim()).firstOrNull;

  @override
  Future<List<FichaInterna>> agendaEspecialista(MedicoActual medico, String fecha) async => [
        for (final f in fichas)
          if (mismoNombre(f.medico, medico.nombre) && f.fecha == fecha) f
      ]..sort((a, b) => (a.hora ?? '').compareTo(b.hora ?? ''));

  @override
  Future<FichaInterna> marcarAtendida(int ticket, String observaciones) async {
    final i = _indice(ticket);
    if (fichas[i].estado != EstadoFicha.asignada) {
      throw const ApiException('ESTADO_NO_PERMITE', 'Solo se atiende una ficha ASIGNADA', estado: 409);
    }
    final texto = observaciones.trim();
    return fichas[i] = fichas[i].copia(estado: EstadoFicha.atendida, observaciones: texto.isEmpty ? null : texto);
  }

  @override
  Future<FichaInterna> noSePresento(int ticket) async {
    final i = _indice(ticket);
    final f = fichas[i];
    if (f.estado != EstadoFicha.asignada) {
      throw const ApiException('ESTADO_NO_PERMITE', 'Solo se cancela una ficha ASIGNADA', estado: 409);
    }
    final ahora = _reloj();
    final hoy = hoyIso(ahora);
    final fecha = f.fecha;
    if (fecha != null &&
        (fecha.compareTo(hoy) > 0 || (fecha == hoy && (f.hora ?? '').compareTo(horaActual(ahora)) > 0))) {
      throw ApiException('TODAVIA_NO_ES_LA_HORA', '«No se presentó» se habilita después de las ${f.hora}.',
          estado: 409);
    }
    return fichas[i] = f.copia(estado: EstadoFicha.cancelada);
  }

  int _indice(int ticket) {
    final i = fichas.indexWhere((f) => f.ticket == ticket);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'La ficha no existe', estado: 404);
    }
    return i;
  }

  /// Urgencia: hoy, desde ahora, con el especialista que se libere antes.
  ({_Especialista medico, String fecha, String hora})? _huecoHoy(Especialidad especialidad) {
    final ahora = _reloj();
    final desde = sumarMinutos(horaActual(ahora), 10 - ahora.minute % 10);
    return _primerHueco(especialidad, hoyIso(ahora), desde, '23:30');
  }

  /// Sin urgencia: desde mañana, hasta 15 días hábiles, con el especialista que tenga menos fichas ese día.
  ({_Especialista medico, String fecha, String hora})? _huecoDesdeManana(Especialidad especialidad) {
    var fecha = hoyIso(_reloj());
    for (var i = 0; i < _diasHabilesMaximos; i++) {
      fecha = sumarDias(fecha, 1, habiles: true);
      final hueco = _primerHueco(especialidad, fecha, _inicioJornada, _finJornada);
      if (hueco != null) {
        return hueco;
      }
    }
    return null;
  }

  ({_Especialista medico, String fecha, String hora})? _primerHueco(
      Especialidad especialidad, String fecha, String desde, String hasta) {
    final medicos = _especialistas.where((m) => m.especialidad == especialidad).toList()
      ..sort((a, b) => _fichasDe(a, fecha).length - _fichasDe(b, fecha).length);
    ({_Especialista medico, String fecha, String hora})? mejor;
    for (final medico in medicos) {
      final ocupadas = _fichasDe(medico, fecha);
      for (var hora = desde; minutosDe(hora) + medico.duracionMin <= minutosDe(hasta); hora = sumarMinutos(hora, 5)) {
        final inicio = minutosDe(hora);
        final fin = inicio + medico.duracionMin;
        final choca = ocupadas.any((f) => minutosDe(f.hora!) < fin && inicio < minutosDe(f.hora!) + f.duracionMin);
        if (!choca) {
          if (mejor == null || hora.compareTo(mejor.hora) < 0) {
            mejor = (medico: medico, fecha: fecha, hora: hora);
          }
          break;
        }
      }
    }
    return mejor;
  }

  List<FichaInterna> _fichasDe(_Especialista medico, String fecha) => [
        for (final f in fichas)
          if (f.medico == medico.nombre && f.fecha == fecha && f.estado != EstadoFicha.cancelada) f
      ];
}

/// Datos del Figma, con fechas relativas a hoy.
List<FichaInterna> _semilla(String hoy) {
  final manana = sumarDias(hoy, 1, habiles: true);
  FichaInterna base({
    required int ticket,
    required int pacienteId,
    required String nombrePaciente,
    int? citaOrigenId,
    String numeroAfiliado = '',
    int edad = 40,
    Especialidad especialidad = Especialidad.gastroenterologia,
    TablaFicha tabla = TablaFicha.general,
    bool urgencia = false,
    Gravedad gravedad = Gravedad.media,
    String motivo = '',
    EstadoFicha estado = EstadoFicha.asignada,
    String? fecha = '',
    String? hora = '08:00',
    int duracionMin = 25,
    String? medico = 'Raúl Vargas',
    String? consultorio = '12',
    String derivadoPor = 'Carlos Ramos',
    Especialidad especialidadOrigen = Especialidad.consultorioFamiliar,
    DateTime? llegada,
  }) =>
      FichaInterna(
        ticket: ticket,
        citaOrigenId: citaOrigenId,
        pacienteId: pacienteId,
        nombrePaciente: nombrePaciente,
        numeroAfiliado: numeroAfiliado,
        edad: edad,
        especialidad: especialidad,
        tabla: tabla,
        urgencia: urgencia,
        gravedad: gravedad,
        motivo: motivo,
        estado: estado,
        fecha: fecha == '' ? hoy : fecha,
        hora: hora,
        duracionMin: duracionMin,
        medico: medico,
        consultorio: consultorio,
        derivadoPor: derivadoPor,
        especialidadOrigen: especialidadOrigen,
        llegada: llegada ?? DateTime.parse('${hoy}T07:00:00'),
      );
  return [
    // Derivación de la agenda del Dr. Ramos: Ana Mamani (ficha 2, 08:45) -> Cardiología, Ticket N° 1021.
    base(
        ticket: 1021,
        citaOrigenId: 502,
        pacienteId: 101,
        nombrePaciente: 'Ana Mamani',
        numeroAfiliado: '2290-15',
        edad: 41,
        especialidad: Especialidad.cardiologia,
        gravedad: Gravedad.baja,
        motivo: 'Palpitaciones al esfuerzo, control de ritmo.',
        fecha: sumarDias(hoy, 2, habiles: true),
        hora: '09:00',
        duracionMin: 30,
        medico: 'Miguel Paredes',
        consultorio: '15',
        llegada: DateTime.parse('${hoy}T08:55:00')),
    // Agenda de fichas internas del Dr. Vargas (hoy).
    base(
        ticket: 1019,
        pacienteId: 110,
        nombrePaciente: 'Pedro Huanca',
        numeroAfiliado: '6120-02',
        edad: 63,
        gravedad: Gravedad.alta,
        motivo: 'Hemorragia digestiva alta',
        estado: EstadoFicha.atendida,
        hora: '08:00'),
    base(
        ticket: 1020,
        pacienteId: 111,
        nombrePaciente: 'Elena Rojas',
        numeroAfiliado: '4410-05',
        edad: 52,
        gravedad: Gravedad.alta,
        motivo: 'Dolor epigástrico recurrente',
        estado: EstadoFicha.atendida,
        hora: '08:30'),
    base(
        ticket: 1022,
        pacienteId: 112,
        nombrePaciente: 'Carmen Ticona',
        numeroAfiliado: '3871-11',
        edad: 66,
        motivo: 'Control de gastritis crónica',
        estado: EstadoFicha.cancelada,
        hora: '09:00'),
    base(
        ticket: 1024,
        pacienteId: 113,
        nombrePaciente: 'Marco Ríos',
        numeroAfiliado: '4587-12',
        edad: 24,
        motivo: 'Dolor abdominal persistente hace 3 semanas, sospecha de úlcera.',
        hora: '10:20',
        llegada: DateTime.parse('${hoy}T09:52:00')),
    base(
        ticket: 1026,
        pacienteId: 114,
        nombrePaciente: 'Sofía Vargas',
        numeroAfiliado: '5530-04',
        edad: 6,
        tabla: TablaFicha.pediatria,
        motivo: 'Reflujo en niña de 6 años',
        hora: '11:00',
        derivadoPor: 'Lucía Quispe',
        especialidadOrigen: Especialidad.pediatria),
    base(
        ticket: 1031,
        pacienteId: 115,
        nombrePaciente: 'Juan Mamani',
        numeroAfiliado: '6702-09',
        edad: 52,
        urgencia: true,
        gravedad: Gravedad.alta,
        motivo: 'Sangrado digestivo (urgencia de hoy)',
        hora: '12:30'),
    // Cola de Gastroenterología (tabla general).
    base(
        ticket: 1032,
        pacienteId: 104,
        nombrePaciente: 'Rosa Choque',
        numeroAfiliado: '7781-03',
        edad: 68,
        urgencia: true,
        gravedad: Gravedad.alta,
        motivo: 'Vómitos con sangre',
        hora: '14:20',
        llegada: DateTime.parse('${hoy}T13:05:00')),
    base(
        ticket: 1027,
        pacienteId: 116,
        nombrePaciente: 'Luis Paz',
        numeroAfiliado: '2981-06',
        edad: 66,
        gravedad: Gravedad.alta,
        motivo: 'Disfagia progresiva',
        fecha: manana,
        hora: '08:00',
        llegada: DateTime.parse('2026-11-11T09:10:00')),
    base(
        ticket: 1028,
        pacienteId: 117,
        nombrePaciente: 'Ana Rojas',
        numeroAfiliado: '6011-02',
        edad: 45,
        gravedad: Gravedad.alta,
        motivo: 'Anemia por pérdida digestiva',
        fecha: manana,
        hora: '08:30',
        medico: 'Patricia Arce',
        consultorio: '14',
        llegada: DateTime.parse('2026-11-10T15:40:00')),
    base(
        ticket: 1029,
        pacienteId: 118,
        nombrePaciente: 'Teresa Ticona',
        numeroAfiliado: '1873-10',
        edad: 71,
        motivo: 'Dolor abdominal crónico',
        fecha: manana,
        hora: '09:00',
        medico: 'Patricia Arce',
        consultorio: '14',
        llegada: DateTime.parse('2026-11-12T08:20:00')),
    base(
        ticket: 1030,
        pacienteId: 119,
        nombrePaciente: 'Jorge Limachi',
        numeroAfiliado: '4402-01',
        edad: 62,
        gravedad: Gravedad.baja,
        motivo: 'Control de colon irritable',
        fecha: sumarDias(hoy, 5, habiles: true),
        hora: '11:00',
        medico: 'Patricia Arce',
        consultorio: '14',
        llegada: DateTime.parse('2026-11-09T10:30:00')),
    base(
        ticket: 1025,
        pacienteId: 120,
        nombrePaciente: 'Mario Cruz',
        numeroAfiliado: '3310-07',
        gravedad: Gravedad.baja,
        motivo: 'Control de hígado graso',
        estado: EstadoFicha.enEspera,
        fecha: null,
        hora: null,
        medico: null,
        consultorio: null,
        llegada: DateTime.parse('2026-11-09T11:00:00')),
  ];
}
