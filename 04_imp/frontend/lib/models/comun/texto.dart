const Map<String, String> _sinTilde = {'á': 'a', 'é': 'e', 'í': 'i', 'ó': 'o', 'ú': 'u', 'ü': 'u'};

/// Minusculas y sin tildes, para buscar ("Médico" encuentra "medico").
String textoPlano(String texto) =>
    texto.toLowerCase().split('').map((c) => _sinTilde[c] ?? c).join().replaceAll('_', ' ').trim();

/// "María Flores" -> "M. Flores" (como en el historial de cambios del Figma).
String nombreCorto(String nombre) {
  final partes = nombre.trim().split(RegExp(r'\s+'));
  if (partes.length < 2) {
    return nombre.trim();
  }
  return '${partes.first[0]}. ${partes.last}';
}
