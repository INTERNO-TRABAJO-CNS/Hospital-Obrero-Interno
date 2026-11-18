import '../core/api_client.dart';
import '../models/catalogo/consultorio.dart';
import '../models/catalogo/dia_no_habil.dart';
import '../models/catalogo/especialidad.dart';
import '../models/catalogo/horario.dart';
import '../models/catalogo/medico.dart';
import 'catalogo_service.dart';

/// CatalogoService en memoria para probar las pantallas sin backend.
///
/// Repite las reglas principales del backend con los mismos codigos de error
/// para que las pantallas se comporten igual. Se activa con
/// `flutter run --dart-define=CATALOGO_FAKE=true`.
class FakeCatalogoService implements CatalogoService {
  FakeCatalogoService({bool conDatosDeEjemplo = false}) {
    if (conDatosDeEjemplo) {
      _cargarEjemplo();
    }
  }

  final List<Medico> medicos = [];
  final List<Consultorio> consultorios = [];
  final List<Horario> horarios = [];
  final List<DiaNoHabil> dias = [];

  /// Ids de medicos/consultorios que "tienen citas programadas" (simula CAMBIO_AFECTA_CITAS).
  final Set<int> medicosConCitas = {};
  final Set<int> consultoriosConCitas = {};

  int _siguienteId = 0;

  int _id() => ++_siguienteId;

  // ------------------------------------------------------------------ medicos

  @override
  Future<List<Medico>> listarMedicos({Especialidad? filtro}) async =>
      [for (final m in medicos) if (filtro == null || m.especialidad == filtro) m];

  @override
  Future<Medico> crearMedico(Medico medico) async {
    _validarMedico(medico);
    final nuevo = medico.copiar(id: _id(), activo: true);
    medicos.add(nuevo);
    return nuevo;
  }

  @override
  Future<Medico> modificarMedico(int id, Medico medico) async {
    final i = _indice(medicos.map((m) => m.id), id);
    final actual = medicos[i];
    _validarMedico(medico);
    final cambiaAgenda = actual.especialidad != medico.especialidad ||
        actual.duracionCitaMin != medico.duracionCitaMin ||
        actual.cupoMaximoDia != medico.cupoMaximoDia;
    if (cambiaAgenda && medicosConCitas.contains(id)) {
      throw const ApiException('CAMBIO_AFECTA_CITAS', 'El medico tiene citas programadas', estado: 409);
    }
    return medicos[i] = medico.copiar(id: id, activo: actual.activo);
  }

  @override
  Future<void> darDeBajaMedico(int id) async {
    final i = _indice(medicos.map((m) => m.id), id);
    if (medicosConCitas.contains(id)) {
      throw const ApiException('CAMBIO_AFECTA_CITAS', 'El medico tiene citas programadas', estado: 409);
    }
    medicos[i] = medicos[i].copiar(activo: false);
  }

  // ------------------------------------------------------------- consultorios

  @override
  Future<List<Consultorio>> listarConsultorios() async => List.of(consultorios);

  @override
  Future<Consultorio> crearConsultorio(Consultorio consultorio) async {
    _validarConsultorio(consultorio, 0);
    final nuevo = consultorio.copiar(id: _id(), nombre: consultorio.nombre.trim(), activo: true);
    consultorios.add(nuevo);
    return nuevo;
  }

  @override
  Future<Consultorio> modificarConsultorio(int id, Consultorio consultorio) async {
    final i = _indice(consultorios.map((c) => c.id), id);
    _validarConsultorio(consultorio, id);
    return consultorios[i] = consultorios[i].copiar(nombre: consultorio.nombre.trim());
  }

  @override
  Future<void> darDeBajaConsultorio(int id) async {
    final i = _indice(consultorios.map((c) => c.id), id);
    if (consultoriosConCitas.contains(id)) {
      throw const ApiException('CAMBIO_AFECTA_CITAS', 'El consultorio tiene citas programadas', estado: 409);
    }
    consultorios[i] = consultorios[i].copiar(activo: false);
  }

  // ----------------------------------------------------------------- horarios

  @override
  Future<List<Horario>> listarHorarios() async => List.of(horarios);

  @override
  Future<Horario> definirHorario(Horario horario) async {
    _validarHorario(horario, 0);
    final nuevo = horario.copiar(id: _id(), activo: true);
    horarios.add(nuevo);
    return nuevo;
  }

  @override
  Future<Horario> modificarHorario(int id, Horario horario) async {
    final i = _indice(horarios.map((h) => h.id), id);
    _validarHorario(horario, id);
    _horarioSinCitas(horarios[i]);
    return horarios[i] = horario.copiar(id: id, activo: horarios[i].activo);
  }

  @override
  Future<void> darDeBajaHorario(int id) async {
    final i = _indice(horarios.map((h) => h.id), id);
    _horarioSinCitas(horarios[i]);
    horarios[i] = horarios[i].copiar(activo: false);
  }

  // ---------------------------------------------------------- dias no habiles

  @override
  Future<List<DiaNoHabil>> listarDiasNoHabiles() async => List.of(dias)..sort((a, b) => a.fecha.compareTo(b.fecha));

  @override
  Future<DiaNoHabil> registrarDiaNoHabil(DiaNoHabil dia) async {
    _validarDia(dia, 0);
    final nuevo = dia.copiar(id: _id(), activo: true);
    dias.add(nuevo);
    return nuevo;
  }

  @override
  Future<DiaNoHabil> modificarDiaNoHabil(int id, DiaNoHabil dia) async {
    final i = _indice(dias.map((d) => d.id), id);
    _validarDia(dia, id);
    return dias[i] = dia.copiar(id: id, activo: dias[i].activo);
  }

  @override
  Future<void> darDeBajaDiaNoHabil(int id) async {
    final i = _indice(dias.map((d) => d.id), id);
    dias[i] = dias[i].copiar(activo: false);
  }

  // ------------------------------------------------------------------ interno

  static int _indice(Iterable<int> ids, int id) {
    final i = ids.toList().indexOf(id);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'Registro no encontrado', estado: 404);
    }
    return i;
  }

  static void _validarMedico(Medico m) {
    if (m.nombres.trim().isEmpty || m.apellidos.trim().isEmpty) {
      throw const ApiException('DATOS_INVALIDOS', 'El medico necesita nombres y apellidos', estado: 400);
    }
    if (m.cupoMaximoDia < cupoMinimo || m.cupoMaximoDia > cupoMaximo) {
      throw const ApiException('VALIDACION_CUPO', 'El cupo diario debe estar entre 5 y 7', estado: 422);
    }
    final e = m.especialidad;
    if (m.duracionCitaMin < e.duracionMin || m.duracionCitaMin > e.duracionMax) {
      throw ApiException('VALIDACION_DURACION',
          'La duracion de la ficha debe estar entre ${e.duracionMin} y ${e.duracionMax} minutos',
          estado: 422);
    }
  }

  void _validarConsultorio(Consultorio c, int idPropio) {
    final nombre = c.nombre.trim().toLowerCase();
    if (nombre.isEmpty) {
      throw const ApiException('DATOS_INVALIDOS', 'El consultorio necesita un nombre', estado: 400);
    }
    if (consultorios.any((o) => o.activo && o.id != idPropio && o.nombre.toLowerCase() == nombre)) {
      throw const ApiException('CONSULTORIO_DUPLICADO', 'Ya existe un consultorio con ese nombre', estado: 422);
    }
  }

  void _validarHorario(Horario h, int idPropio) {
    if (minutosDe(h.inicio) >= minutosDe(h.fin)) {
      throw const ApiException('DATOS_INVALIDOS', 'La hora de inicio debe ser anterior a la de fin', estado: 400);
    }
    for (final o in horarios) {
      final mismoRecurso = o.medicoId == h.medicoId || o.consultorioId == h.consultorioId;
      final cruza = o.dia == h.dia &&
          minutosDe(h.inicio) < minutosDe(o.fin) &&
          minutosDe(o.inicio) < minutosDe(h.fin);
      if (o.activo && o.id != idPropio && mismoRecurso && cruza) {
        throw const ApiException('HORARIO_SOLAPADO', 'El horario se cruza con otro', estado: 409);
      }
    }
  }

  void _horarioSinCitas(Horario h) {
    if (medicosConCitas.contains(h.medicoId) || consultoriosConCitas.contains(h.consultorioId)) {
      throw const ApiException('CAMBIO_AFECTA_CITAS', 'El horario tiene citas programadas', estado: 409);
    }
  }

  void _validarDia(DiaNoHabil d, int idPropio) {
    if (d.descripcion.trim().isEmpty) {
      throw const ApiException('DATOS_INVALIDOS', 'El dia no habil necesita descripcion', estado: 400);
    }
    final hoy = DateTime.now();
    if (!d.fecha.isAfter(DateTime(hoy.year, hoy.month, hoy.day))) {
      throw const ApiException('VALIDACION_FECHA', 'El dia no habil debe ser una fecha futura', estado: 422);
    }
    if (dias.any((o) => o.activo && o.id != idPropio && fechaATexto(o.fecha) == fechaATexto(d.fecha))) {
      throw const ApiException('DIA_NO_HABIL_DUPLICADO', 'Esa fecha ya esta registrada', estado: 422);
    }
  }

  /// Datos parecidos a los del Figma.
  void _cargarEjemplo() {
    Medico m(String n, String a, Especialidad e, int d, int c) =>
        Medico(id: _id(), nombres: n, apellidos: a, especialidad: e, duracionCitaMin: d, cupoMaximoDia: c);
    medicos.addAll([
      m('Carlos', 'Ramos', Especialidad.consultorioFamiliar, 40, 7),
      m('Lucía', 'Quispe', Especialidad.pediatria, 35, 6),
      m('Jorge', 'Mamani', Especialidad.odontologia, 50, 5),
      m('Raúl', 'Vargas', Especialidad.gastroenterologia, 25, 6),
      m('Patricia', 'Arce', Especialidad.gastroenterologia, 25, 7),
      m('Daniela', 'Salinas', Especialidad.dermatologia, 20, 7),
      m('Miguel', 'Paredes', Especialidad.cardiologia, 30, 5),
      m('Hugo', 'Lima', Especialidad.terapiaIntensiva, 45, 5),
      m('Rosa', 'Salas', Especialidad.consultorioFamiliar, 45, 7).copiar(activo: false),
    ]);
    consultorios.addAll([
      for (final n in ['Consultorio 2', 'Consultorio 4', 'Consultorio 6']) Consultorio(id: _id(), nombre: n),
    ]);
    const lunAVie = [DiaSemana.lunes, DiaSemana.martes, DiaSemana.miercoles, DiaSemana.jueves, DiaSemana.viernes];
    void franja(int medico, int consultorio, String fin) {
      for (final d in lunAVie) {
        horarios.add(Horario(
            id: _id(),
            medicoId: medicos[medico].id,
            consultorioId: consultorios[consultorio].id,
            dia: d,
            inicio: horaDesdeTexto('08:00'),
            fin: horaDesdeTexto(fin)));
      }
    }

    franja(0, 1, '12:00');
    franja(1, 2, '12:30');
    franja(2, 0, '11:30');
    final anio = DateTime.now().year;
    dias.add(DiaNoHabil(id: _id(), fecha: DateTime(anio, 12, 25), descripcion: 'Navidad'));
  }
}
