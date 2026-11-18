/// Fechas y horas como texto, tal como viajan en el JSON del backend: fecha "2026-11-12" y
/// hora "08:40" o "08:40:00". Las usan las pantallas del médico.
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
const List<String> _mesesCortos = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

String _dos(int n) => n.toString().padLeft(2, '0');

String _mayuscula(String s) => s.isEmpty ? s : '${s[0].toUpperCase()}${s.substring(1)}';

/// "2026-11-12" de una fecha local.
String isoDe(DateTime f) => '${f.year}-${_dos(f.month)}-${_dos(f.day)}';

/// Fecha local de hoy, "2026-11-12".
String hoyIso([DateTime? ahora]) => isoDe(ahora ?? DateTime.now());

/// Hora local "08:05".
String horaActual([DateTime? ahora]) {
  final f = ahora ?? DateTime.now();
  return '${_dos(f.hour)}:${_dos(f.minute)}';
}

/// "2026-11-12" -> DateTime local (sin hora).
DateTime fechaDeIso(String iso) {
  final p = iso.substring(0, 10).split('-').map(int.parse).toList();
  return DateTime(p[0], p[1], p[2]);
}

/// "2026-11-12" -> "jueves 12 de noviembre de 2026".
String fechaLargaIso(String iso) {
  final f = fechaDeIso(iso);
  return '${_dias[f.weekday - 1]} ${f.day} de ${_meses[f.month - 1]} de ${f.year}';
}

/// "2026-11-12" -> "Jueves 12 de noviembre" (titulos de la agenda).
String fechaTituloIso(String iso) {
  final f = fechaDeIso(iso);
  return '${_mayuscula(_dias[f.weekday - 1])} ${f.day} de ${_meses[f.month - 1]}';
}

/// "2026-11-12" -> "Jueves 12 nov 2026" (encabezado de la atención).
String fechaDiaMesIso(String iso) {
  final f = fechaDeIso(iso);
  return '${_mayuscula(_dias[f.weekday - 1])} ${f.day} ${_mesesCortos[f.month - 1]} ${f.year}';
}

/// "2026-11-12" -> "12/11/2026".
String fechaCortaIso(String iso) {
  final f = fechaDeIso(iso);
  return '${_dos(f.day)}/${_dos(f.month)}/${f.year}';
}

/// "2026-11-12" -> "12 nov 2026" (o "12 nov" sin el año).
String fechaMesIso(String iso, {bool conAnio = true}) {
  final f = fechaDeIso(iso);
  final texto = '${f.day} ${_mesesCortos[f.month - 1]}';
  return conAnio ? '$texto ${f.year}' : texto;
}

/// "2026-11-12" -> "Jue 12/11/2026" (ticket impreso).
String fechaTicketIso(String iso) => '${_diasCortos[fechaDeIso(iso).weekday - 1]} ${fechaCortaIso(iso)}';

/// "08:40:00" -> "08:40".
String horaCorta(String hora) => hora.length >= 5 ? hora.substring(0, 5) : hora;

/// "08:40" -> 520.
int minutosDe(String hora) {
  final p = hora.split(':');
  return int.parse(p[0]) * 60 + int.parse(p[1]);
}

/// Suma minutos a "HH:mm" (se corta en 23:59: solo las urgencias pasan del horario).
String sumarMinutos(String hora, int minutos) {
  final total = (minutosDe(hora) + minutos).clamp(0, 23 * 60 + 59);
  return '${_dos(total ~/ 60)}:${_dos(total % 60)}';
}

/// Suma dias a "YYYY-MM-DD"; con [habiles] salta sabados y domingos.
String sumarDias(String iso, int dias, {bool habiles = false}) {
  var f = fechaDeIso(iso);
  var restantes = dias;
  while (restantes > 0) {
    f = DateTime(f.year, f.month, f.day + 1);
    if (!habiles || f.weekday <= DateTime.friday) {
      restantes--;
    }
  }
  return isoDe(f);
}
