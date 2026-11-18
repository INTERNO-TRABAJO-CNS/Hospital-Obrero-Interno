import '../models/catalogo/medico.dart';
import '../models/comun/fechas.dart';
import '../models/reportes/reporte.dart';

/// Reportes e indicadores del administrador.
abstract class ReportesService {
  /// Reporte de la semana actual (lunes a viernes).
  Future<Reporte> reporte(TipoReporte tipo);
}

/// De prueba: cifras fijas parecidas a las del Figma. El backend todavia no calcula reportes.
class FakeReportesService implements ReportesService {
  FakeReportesService({DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now;

  final DateTime Function() _reloj;

  @override
  Future<Reporte> reporte(TipoReporte tipo) async {
    final semana = semanaLaboral(_reloj());
    switch (tipo) {
      case TipoReporte.porMedico:
        FilaGrafico medico(String nombre, int fichas) => FilaGrafico(
              etiqueta: nombre,
              valor: fichas,
              texto: fichas < cupoMinimo ? '$fichas fichas · bajo la meta' : '$fichas fichas',
              alerta: fichas < cupoMinimo,
            );
        return Reporte(
          titulo: 'Fichas atendidas por día (meta mínima $cupoMinimo, máximo $cupoMaximo)',
          filas: [medico('Dr. Ramos', 7), medico('Dra. Quispe', 6), medico('Dr. Mamani', 4), medico('Dra. Salas', 5)],
          maximo: cupoMaximo,
          indicadores: const [
            Indicador('86 solicitudes', 'Demanda (semana)'),
            Indicador('9 (4 automáticas)', 'Cancelaciones'),
            Indicador('5.5 fichas/médico/día', 'Carga de trabajo'),
            Indicador('3 solicitudes', 'Sin cupo'),
          ],
          nota: 'Tiempos de espera y de atención por separado no se miden: solo confirmación → fin.',
        );
      case TipoReporte.porEstado:
        return Reporte(
          titulo: 'Fichas por estado (semana $semana)',
          filas: const [
            FilaGrafico(etiqueta: 'Atendidas', valor: 61, texto: '61 fichas'),
            FilaGrafico(etiqueta: 'Asist. confirmada', valor: 8, texto: '8 fichas'),
            FilaGrafico(etiqueta: 'Programadas', valor: 8, texto: '8 fichas'),
            FilaGrafico(etiqueta: 'Canceladas', valor: 9, texto: '9 fichas (4 automáticas)', alerta: true),
          ],
          maximo: 86,
          indicadores: const [
            Indicador('86 fichas', 'Total (semana)'),
            Indicador('71 %', 'Atendidas'),
            Indicador('10 %', 'Canceladas'),
            Indicador('4', 'Canceladas automáticamente'),
          ],
          nota: 'Cancelada (automática): el paciente no confirmó su llegada 30 minutos antes de la cita.',
        );
      case TipoReporte.porEspecialidad:
        return Reporte(
          titulo: 'Solicitudes por especialidad básica (semana $semana)',
          filas: const [
            FilaGrafico(etiqueta: 'Consultorio Familiar', valor: 41, texto: '41 solicitudes'),
            FilaGrafico(etiqueta: 'Pediatría', valor: 26, texto: '26 solicitudes'),
            FilaGrafico(etiqueta: 'Odontología', valor: 19, texto: '19 solicitudes'),
          ],
          maximo: 41,
          indicadores: const [
            Indicador('86 solicitudes', 'Demanda (semana)'),
            Indicador('3 solicitudes', 'Sin cupo'),
            Indicador('Consultorio Familiar', 'La más pedida'),
            Indicador('Odontología', 'La menos pedida'),
          ],
          nota: 'Las especialidades internas no las pide el paciente: llegan por ficha interna '
              '(ver «Fichas internas y quirófanos»).',
        );
      case TipoReporte.porConsultorio:
        return Reporte(
          titulo: 'Fichas atendidas por consultorio (semana $semana)',
          filas: const [
            FilaGrafico(etiqueta: 'Consultorio 1', valor: 9, texto: '9 fichas'),
            FilaGrafico(etiqueta: 'Consultorio 2', valor: 14, texto: '14 fichas'),
            FilaGrafico(etiqueta: 'Consultorio 4', valor: 22, texto: '22 fichas'),
            FilaGrafico(etiqueta: 'Consultorio 6', valor: 16, texto: '16 fichas'),
          ],
          maximo: 22,
          indicadores: const [
            Indicador('4', 'Consultorios en uso'),
            Indicador('Consultorio 4', 'El más usado'),
            Indicador('61 fichas', 'Atendidas (semana)'),
            Indicador('15.3', 'Fichas por consultorio'),
          ],
          nota: 'Cuenta las fichas atendidas en cada consultorio, sin importar el médico.',
        );
      case TipoReporte.internasYQuirofanos:
        FilaGrafico interna(String nombre, int fichas) =>
            FilaGrafico(etiqueta: nombre, valor: fichas, texto: '$fichas fichas');
        return Reporte(
          titulo: 'Fichas internas por especialidad (semana $semana)',
          filas: [
            interna('Gastroenterología', 38),
            interna('Cardiología', 31),
            interna('Dermatología', 24),
            interna('Neumología', 19),
            interna('Endocrinología', 17),
            interna('Hematología', 12),
            interna('Nefrología', 11),
            interna('Infectología', 9),
            interna('Medicina del Trabajo', 6),
            interna('Terapia Intensiva', 3),
          ],
          maximo: 40,
          indicadores: const [
            Indicador('6 %', 'No se presentó (fichas internas)'),
            Indicador('31 h / 144 h', 'Horas de quirófano usadas / abiertas'),
            Indicador('4', 'Desplazamientos (fichas y cirugías)'),
            Indicador('3', 'Fichas en espera ahora'),
          ],
          nota: 'La gravedad no aparece en reportes. Los desplazamientos cuentan las fichas y cirugías movidas '
              'por un caso de mayor prioridad.',
        );
    }
  }
}
