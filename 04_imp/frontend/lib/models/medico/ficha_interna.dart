import '../catalogo/especialidad.dart';
import 'medico_actual.dart';

/// CU-21/CU-22: ficha interna (derivación de Médico General o Pediatría a una especialidad interna).
const int maxMotivo = 500;
const int edadPediatria = 10;
const int edadAdultoMayor = 60;

enum Gravedad {
  alta('ALTA', 0),
  media('MEDIA', 1),
  baja('BAJA', 2);

  const Gravedad(this.codigo, this.peso);

  final String codigo;

  /// Menor peso = más grave.
  final int peso;
}

enum EstadoFicha {
  enEspera('EN_ESPERA'),
  asignada('ASIGNADA'),
  atendida('ATENDIDA'),
  cancelada('CANCELADA');

  const EstadoFicha(this.codigo);

  final String codigo;
}

class FichaInterna {
  const FichaInterna({
    required this.ticket,
    required this.citaOrigenId,
    required this.pacienteId,
    required this.nombrePaciente,
    required this.numeroAfiliado,
    required this.edad,
    required this.especialidad,
    required this.tabla,
    required this.urgencia,
    required this.gravedad,
    required this.motivo,
    required this.estado,
    required this.fecha,
    required this.hora,
    required this.duracionMin,
    required this.medico,
    required this.consultorio,
    required this.derivadoPor,
    required this.especialidadOrigen,
    required this.llegada,
    this.observaciones,
  });

  /// N° de ticket (1024).
  final int ticket;
  final int? citaOrigenId;
  final int pacienteId;
  final String nombrePaciente;
  final String numeroAfiliado;
  final int edad;
  final Especialidad especialidad;
  final TablaFicha tabla;
  final bool urgencia;

  /// Solo la ve el médico que derivó (general o pediatra): nunca sale en el ticket.
  final Gravedad gravedad;
  final String motivo;
  final EstadoFicha estado;

  /// "2026-11-12" y "10:20"; null mientras está EN_ESPERA.
  final String? fecha;
  final String? hora;
  final int duracionMin;
  final String? medico;
  final String? consultorio;
  final String derivadoPor;
  final Especialidad especialidadOrigen;

  /// Momento de la derivación: desempata la cola por orden de llegada.
  final DateTime llegada;
  final String? observaciones;

  /// Todavía ocupa al paciente (una sola ficha activa a la vez).
  bool get activa => estado == EstadoFicha.asignada || estado == EstadoFicha.enEspera;

  FichaInterna copia({
    EstadoFicha? estado,
    String? fecha,
    String? hora,
    int? duracionMin,
    String? medico,
    String? consultorio,
    String? observaciones,
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
        estado: estado ?? this.estado,
        fecha: fecha ?? this.fecha,
        hora: hora ?? this.hora,
        duracionMin: duracionMin ?? this.duracionMin,
        medico: medico ?? this.medico,
        consultorio: consultorio ?? this.consultorio,
        derivadoPor: derivadoPor,
        especialidadOrigen: especialidadOrigen,
        llegada: llegada,
        observaciones: observaciones ?? this.observaciones,
      );
}

/// Lo que llena el médico en "Nueva ficha interna".
class SolicitudFicha {
  const SolicitudFicha({
    required this.citaOrigenId,
    required this.pacienteId,
    required this.nombrePaciente,
    required this.numeroAfiliado,
    required this.edad,
    required this.especialidad,
    required this.urgencia,
    required this.gravedad,
    required this.motivo,
  });

  final int? citaOrigenId;
  final int pacienteId;
  final String nombrePaciente;
  final String numeroAfiliado;
  final int edad;
  final Especialidad especialidad;
  final bool urgencia;
  final Gravedad gravedad;
  final String motivo;
}

/// Cita desde la que se deriva (para validar CITA_NO_VALIDA y CITA_YA_DERIVADA).
class CitaOrigen {
  const CitaOrigen(
      {required this.citaId, required this.numeroFicha, required this.fecha, required this.hora, required this.estado});

  final int citaId;
  final int numeroFicha;
  final String fecha;
  final String hora;

  /// Código del estado de la cita ("ASISTENCIA_CONFIRMADA").
  final String estado;
}

class PacienteBuscado {
  const PacienteBuscado(
      {required this.pacienteId, required this.nombre, required this.numeroAfiliado, required this.edad});

  final int pacienteId;
  final String nombre;
  final String numeroAfiliado;
  final int edad;
}

/// ASIGNADA o EN_ESPERA con su [ficha], o RECHAZADA con [codigo] y [mensaje].
class ResultadoFicha {
  const ResultadoFicha.generada(FichaInterna this.ficha)
      : codigo = null,
        mensaje = null;

  const ResultadoFicha.rechazada(String this.codigo, String this.mensaje) : ficha = null;

  final FichaInterna? ficha;
  final String? codigo;
  final String? mensaje;

  bool get rechazada => ficha == null;
}

/// Rechazos posibles al generar la ficha (Figma "TICKET RECHAZADO").
const List<(String, String)> rechazosFicha = [
  ('MEDICO_NO_DERIVA', 'Solo Médico General y Pediatría pueden derivar.'),
  ('CITA_NO_VALIDA', 'La cita debe ser tuya, de hoy y estar confirmada o atendida.'),
  ('CITA_YA_DERIVADA', 'Una sola derivación por cita.'),
  ('TERAPIA_SOLO_URGENCIA', 'Terapia Intensiva solo con Urgencia.'),
  ('EDAD_NO_PEDIATRICA', 'Pediatría solo atiende a menores de 10 años.'),
  ('PACIENTE_CON_FICHA_ACTIVA', 'El paciente ya tiene una ficha interna activa.'),
  ('MOTIVO_INVALIDO', 'El motivo debe tener de 1 a $maxMotivo caracteres.'),
];

/// Regla única de prioridad: urgencia > gravedad (ALTA > MEDIA > BAJA) > 60 años o más > orden de llegada.
int compararPrioridad(FichaInterna a, FichaInterna b) {
  if (a.urgencia != b.urgencia) {
    return a.urgencia ? -1 : 1;
  }
  if (a.gravedad != b.gravedad) {
    return a.gravedad.peso - b.gravedad.peso;
  }
  final mayorA = a.edad >= edadAdultoMayor;
  final mayorB = b.edad >= edadAdultoMayor;
  if (mayorA != mayorB) {
    return mayorA ? -1 : 1;
  }
  return a.llegada.compareTo(b.llegada);
}
