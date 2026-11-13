import 'impresion_stub.dart' if (dart.library.js_interop) 'impresion_web.dart' as plataforma;

/// Imprime una pagina HTML (el ticket de 80 mm). En la web abre el dialogo de impresion del
/// navegador y devuelve `true`; en celular y escritorio devuelve `false` (falta un plugin de
/// impresion para esas plataformas).
bool imprimirHtml(String html) => plataforma.imprimirHtml(html);

/// URL absoluta de un asset de la app (para el logo dentro del ticket impreso).
String urlDeAsset(String asset) => Uri.base.resolve('assets/$asset').toString();
