import 'dart:js_interop';
import 'dart:js_interop_unsafe';

/// Web: abre una ventana con el ticket y llama a `print()`. Devuelve `false` si el navegador
/// bloqueo la ventana emergente.
bool imprimirHtml(String html) {
  final ventana = globalContext.callMethod<JSObject?>('open'.toJS, ''.toJS, '_blank'.toJS);
  if (ventana == null) {
    return false;
  }
  final documento = ventana.getProperty<JSObject>('document'.toJS);
  documento.callMethod<JSAny?>('open'.toJS);
  documento.callMethod<JSAny?>('write'.toJS, html.toJS);
  documento.callMethod<JSAny?>('close'.toJS);
  ventana.callMethod<JSAny?>('focus'.toJS);
  ventana.callMethod<JSAny?>('print'.toJS);
  return true;
}
