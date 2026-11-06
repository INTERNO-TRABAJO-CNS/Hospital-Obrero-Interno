/// Tipo de pantalla donde se muestra la app.
enum Dispositivo { celular, escritorio, tv }

/// Desde este ancho (en puntos logicos) la pantalla se considera de escritorio.
const double anchoMinimoEscritorio = 600;

/// Desde este ancho la pantalla se considera TV.
const double anchoMinimoTv = 1600;

/// Ruta que siempre se muestra como TV, sin importar el ancho.
const String rutaTv = '/tv';

/// <600 celular, 600-1599 escritorio, >=1600 (o la ruta /tv) TV.
Dispositivo dispositivoDe(double ancho, {String? ruta}) {
  if (ruta == rutaTv || ancho >= anchoMinimoTv) {
    return Dispositivo.tv;
  }
  if (ancho >= anchoMinimoEscritorio) {
    return Dispositivo.escritorio;
  }
  return Dispositivo.celular;
}
