import '../core/api_client.dart';
import '../models/auth/sesion.dart';
import '../models/catalogo/especialidad.dart';
import '../models/comun/fechas_iso.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/medico/agenda_medico.dart';
import '../models/medico/atencion.dart';
import '../models/medico/historial_paciente.dart';
import '../models/medico/medico_actual.dart';
import 'medico_service.dart';

/// Sin backend (`--dart-define=AUTH_FAKE=true`): un médico por cada tipo de pantalla del Figma.
/// La clave es el personaId que devuelve FakeAuthService.
const Map<int, MedicoActual> medicosDePrueba = {
  7: MedicoActual(
      medicoId: 7,
      nombre: 'Carlos Ramos',
      especialidad: Especialidad.consultorioFamiliar,
      puedeDerivar: true,
      operaEnQuirofano: false),
  8: MedicoActual(
      medicoId: 8,
      nombre: 'Lucía Quispe',
      especialidad: Especialidad.pediatria,
      puedeDerivar: true,
      operaEnQuirofano: false),
  9: MedicoActual(
      medicoId: 9,
      nombre: 'Jorge Mamani',
      especialidad: Especialidad.odontologia,
      puedeDerivar: false,
      operaEnQuirofano: false),
  21: MedicoActual(
      medicoId: 21,
      nombre: 'Raúl Vargas',
      especialidad: Especialidad.gastroenterologia,
      puedeDerivar: false,
      operaEnQuirofano: true),
};

/// Devuelve el médico de prueba de la sesión actual.
class FakeMedicoService implements MedicoService {
  FakeMedicoService({required Sesion? Function() sesion}) : _sesion = sesion;

  final Sesion? Function() _sesion;

  @override
  Future<MedicoActual> yo() async {
    final sesion = _sesion();
    final medico = sesion == null ? null : medicosDePrueba[sesion.personaId];
    if (medico == null) {
      throw const ApiException('NO_ES_MEDICO', 'La cuenta no corresponde a un médico', estado: 403);
    }
    return medico;
  }
}

/// Agenda del Figma ("WEB MEDICO AGENDA"): 6 citas del Dr. Ramos con los 4 estados. [porFecha] manda.
class FakeAgendaMedicoService implements AgendaMedicoService {
  FakeAgendaMedicoService({DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now;

  final DateTime Function() _reloj;
  final Map<String, List<FichaAgenda>> porFecha = {};
  final List<String?> llamadas = [];

  @override
  Future<AgendaDia> agenda([String? fecha]) async {
    llamadas.add(fecha);
    final hoy = hoyIso(_reloj());
    final dia = fecha ?? hoy;
    final fichas = porFecha[dia] ?? (dia == hoy && porFecha.isEmpty ? agendaDeEjemplo : const <FichaAgenda>[]);
    return AgendaDia(fecha: dia, cantidad: fichas.length, fichas: List.of(fichas));
  }
}

const List<FichaAgenda> agendaDeEjemplo = [
  FichaAgenda(
      citaId: 501,
      numeroFicha: 1,
      hora: '08:00:00',
      nombrePaciente: 'Marcos Condori',
      estado: EstadoCita.atendida,
      pacienteId: 100),
  FichaAgenda(
      citaId: 502,
      numeroFicha: 2,
      hora: '08:45:00',
      nombrePaciente: 'Ana Mamani',
      estado: EstadoCita.atendida,
      pacienteId: 101),
  FichaAgenda(
      citaId: 503,
      numeroFicha: 3,
      hora: '09:30:00',
      nombrePaciente: 'Elena Copa',
      estado: EstadoCita.asistenciaConfirmada,
      pacienteId: 102),
  FichaAgenda(
      citaId: 504,
      numeroFicha: 4,
      hora: '10:15:00',
      nombrePaciente: 'Luis Quispe',
      estado: EstadoCita.programada,
      pacienteId: 103),
  FichaAgenda(
      citaId: 505,
      numeroFicha: 5,
      hora: '11:00:00',
      nombrePaciente: 'Rosa Choque',
      estado: EstadoCita.programada,
      pacienteId: 104),
  FichaAgenda(
      citaId: 506,
      numeroFicha: 6,
      hora: '11:45:00',
      nombrePaciente: 'Jorge Flores',
      estado: EstadoCita.cancelada,
      pacienteId: 105),
];

/// Imita AtencionController: solo la cita confirmada del Figma (ficha 3, 09:30) se puede atender.
class FakeAtencionService implements AtencionService {
  FakeAtencionService({DateTime Function()? reloj}) {
    datosPorCita[503] = DatosAtencion(
      citaId: 503,
      pacienteId: 102,
      nombrePaciente: 'Elena Copa',
      numeroAfiliado: '4587-12',
      numeroFicha: 3,
      fecha: hoyIso((reloj ?? DateTime.now)()),
      hora: '09:30:00',
      consulta: Especialidad.consultorioFamiliar,
      consultorio: 4,
      medico: 'Carlos Ramos',
      puedeDerivar: true,
      estado: EstadoCita.asistenciaConfirmada,
      edad: 34,
      titular: true,
      nombreConsultorio: 'Consultorio 4',
    );
  }

  final Map<int, DatosAtencion> datosPorCita = {};
  final List<(int, String)> registradas = [];

  @override
  Future<DatosAtencion> datos(int citaId) async {
    final d = datosPorCita[citaId];
    if (d == null) {
      throw const ApiException('NO_ENCONTRADA', 'La cita no existe', estado: 404);
    }
    if (d.estado != null && d.estado != EstadoCita.asistenciaConfirmada) {
      throw const ApiException('ESTADO_NO_PERMITE', 'La cita debe tener la asistencia confirmada', estado: 409);
    }
    return d;
  }

  @override
  Future<void> registrar(int citaId, String medicacion) async {
    final d = datosPorCita[citaId];
    if (d == null) {
      throw const ApiException('NO_ENCONTRADA', 'La cita no existe', estado: 404);
    }
    final texto = medicacion.trim();
    if (texto.isEmpty || texto.length > maxReceta) {
      throw const ApiException('RECETA_INVALIDA', 'La receta no puede estar vacía ni pasar de $maxReceta caracteres',
          estado: 400);
    }
    registradas.add((citaId, texto));
    datosPorCita[citaId] = d.conEstado(EstadoCita.atendida);
  }
}

Receta _receta(int citaId, String medicacion, String fechaHora) =>
    Receta(citaId: citaId, medicacion: medicacion, fechaHora: fechaHora);

/// [porPaciente] manda; un paciente que no está responde PACIENTE_FUERA_DE_AGENDA.
class FakeHistorialPacienteService implements HistorialPacienteService {
  final Map<int, HistorialPaciente> porPaciente = {
    102: HistorialPaciente(
      pacienteId: 102,
      nombrePaciente: 'Elena Copa',
      numeroAfiliado: '4587-12',
      titular: true,
      edad: 34,
      citasPasadas: const [
        CitaPasada(
            citaId: 31,
            fecha: '2026-10-20',
            hora: '10:00:00',
            especialidad: 'ODONTOLOGIA',
            medico: 'Jorge Mamani',
            numeroFicha: 2,
            estado: 'ATENDIDA'),
        CitaPasada(
            citaId: 30,
            fecha: '2026-09-03',
            hora: '08:40:00',
            especialidad: 'CONSULTORIO_FAMILIAR',
            medico: 'Carlos Ramos',
            numeroFicha: 1,
            estado: 'ATENDIDA'),
      ],
      atenciones: [
        AtencionPasada(
            fecha: '2026-10-20',
            hora: '10:00:00',
            especialidad: 'ODONTOLOGIA',
            medico: 'Jorge Mamani',
            recetas: [_receta(31, 'Ibuprofeno 400 mg cada 8 h por 3 días.', '2026-10-20T10:40:00')]),
        AtencionPasada(
            fecha: '2026-09-03',
            hora: '08:40:00',
            especialidad: 'CONSULTORIO_FAMILIAR',
            medico: 'Carlos Ramos',
            recetas: [_receta(30, 'Paracetamol 500 mg si hay fiebre.', '2026-09-03T09:10:00')]),
      ],
    ),
    100: HistorialPaciente(
      pacienteId: 100,
      nombrePaciente: 'Marcos Condori',
      numeroAfiliado: '3987-03',
      titular: false,
      edad: 47,
      citasPasadas: const [
        CitaPasada(
            citaId: 40,
            fecha: '2026-10-14',
            hora: '09:30:00',
            especialidad: 'CONSULTORIO_FAMILIAR',
            medico: 'Carlos Ramos',
            numeroFicha: 3,
            estado: 'ATENDIDA'),
        CitaPasada(
            citaId: 41,
            fecha: '2026-08-02',
            hora: '11:00:00',
            especialidad: 'ODONTOLOGIA',
            medico: 'Jorge Mamani',
            numeroFicha: 5,
            estado: 'CANCELADA'),
      ],
      atenciones: [
        AtencionPasada(
            fecha: '2026-10-14',
            hora: '09:30:00',
            especialidad: 'CONSULTORIO_FAMILIAR',
            medico: 'Carlos Ramos',
            recetas: [_receta(40, 'Losartán 50 mg cada 24 h por 30 días.', '2026-10-14T10:05:00')]),
      ],
    ),
    101: const HistorialPaciente(
      pacienteId: 101,
      nombrePaciente: 'Ana Mamani',
      numeroAfiliado: '2290-15',
      titular: true,
      edad: 41,
      citasPasadas: [],
      atenciones: [],
    ),
  };

  @override
  Future<HistorialPaciente> historial(int pacienteId) async {
    final h = porPaciente[pacienteId];
    if (h == null) {
      throw const ApiException(
          'PACIENTE_FUERA_DE_AGENDA', 'Solo puede ver el historial de pacientes que tienen cita en su agenda',
          estado: 403);
    }
    return h;
  }

  @override
  Future<List<PacienteDeAgenda>> buscar(String numeroAfiliado) async => [
        for (final h in porPaciente.values)
          if (h.numeroAfiliado == numeroAfiliado.trim())
            PacienteDeAgenda(
                pacienteId: h.pacienteId,
                nombre: h.nombrePaciente,
                numeroAfiliado: h.numeroAfiliado ?? '',
                titular: h.titular ?? false,
                edad: h.edad ?? 0)
      ];
}
