/// Formatos de fecha y hora en espanol (sin depender de intl).
library;

const List<String> _dias = ['lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado', 'domingo'];
const List<String> _diasCortos = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const List<String> _meses = [
  'enero',
  'febrero',
  'marzo',
  'abril',
  'mayo',
  'junio',
  'julio',
  'agosto',
  'septiembre',
  'octubre',
  'noviembre',
  'diciembre',
];

String _dos(int n) => n.toString().padLeft(2, '0');

/// "08:05".
String horaTexto(DateTime f) => '${_dos(f.hour)}:${_dos(f.minute)}';

/// "13/11/2026".
String fechaNumerica(DateTime f) => '${_dos(f.day)}/${_dos(f.month)}/${f.year}';

/// "13/11".
String diaMes(DateTime f) => '${_dos(f.day)}/${_dos(f.month)}';

/// "18 nov".
String diaMesCorto(DateTime f) => '${f.day} ${_meses[f.month - 1].substring(0, 3)}';

/// El primer dia de lunes a viernes desde [f] (incluido).
DateTime diaHabilDesde(DateTime f) {
  var dia = soloFecha(f);
  while (dia.weekday > DateTime.friday) {
    dia = DateTime(dia.year, dia.month, dia.day + 1);
  }
  return dia;
}

/// "13/11 08:25".
String diaMesHora(DateTime f) => '${diaMes(f)} ${horaTexto(f)}';

/// "viernes 13 de noviembre".
String fechaLarga(DateTime f) => '${_dias[f.weekday - 1]} ${f.day} de ${_meses[f.month - 1]}';

/// "Vie 13/11/2026".
String fechaConDia(DateTime f) => '${_diasCortos[f.weekday - 1]} ${fechaNumerica(f)}';

/// "del 9 al 13 de noviembre" (lunes a viernes de la semana de [f]).
String semanaLaboral(DateTime f) {
  final lunes = DateTime(f.year, f.month, f.day - (f.weekday - 1));
  final viernes = DateTime(lunes.year, lunes.month, lunes.day + 4);
  final mesLunes = lunes.month == viernes.month ? '' : ' de ${_meses[lunes.month - 1]}';
  return 'del ${lunes.day}$mesLunes al ${viernes.day} de ${_meses[viernes.month - 1]}';
}

/// Fecha sin hora.
DateTime soloFecha(DateTime f) => DateTime(f.year, f.month, f.day);

/// Misma fecha con la hora "08:30".
DateTime conHora(DateTime dia, String hora) {
  final partes = hora.split(':');
  return DateTime(dia.year, dia.month, dia.day, int.parse(partes[0]), int.parse(partes[1]));
}
