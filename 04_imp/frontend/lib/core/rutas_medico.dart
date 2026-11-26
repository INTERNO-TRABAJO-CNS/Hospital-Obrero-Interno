/// Rutas de las pantallas del médico (antes eran la web Nuxt; desde el 15/11/2026 están en Flutter).
class RutasMedico {
  const RutasMedico._();

  static const String prefijo = '/medico';
  static const String agenda = '/medico/agenda';
  static const String atencion = '/medico/atencion';
  static const String historial = '/medico/historial';
  static const String ticketInterno = '/medico/ticket-interno';
  static const String cola = '/medico/cola';
  static const String fichasInternas = '/medico/fichas-internas';
  static const String cirugias = '/medico/cirugias';
  static const String programarCirugia = '/medico/cirugias/programar';

  /// "/medico/atencion/503".
  static String atender(int citaId) => '$atencion/$citaId';

  /// "/medico/pacientes/102/historial".
  static String historialDe(int pacienteId) => '/medico/pacientes/$pacienteId/historial';

  /// "/medico/ticket-interno?citaId=503".
  static String ticketInternoDe(int citaId) => '$ticketInterno?citaId=$citaId';

  /// "/medico/cirugias/programar?ficha=1024".
  static String programarDe(int ticket) => '$programarCirugia?ficha=$ticket';
}
