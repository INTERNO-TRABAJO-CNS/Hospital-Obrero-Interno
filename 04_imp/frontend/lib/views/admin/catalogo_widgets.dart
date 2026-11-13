import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../comun/cns_widgets.dart';

export '../comun/cns_widgets.dart' show ColumnaTabla, TablaCns;

/// Debajo de este ancho la tabla y el formulario se apilan (ventana angosta).
const double anchoMinimoDosColumnas = 900;

/// Tabla del Figma (el nombre se mantiene para las pantallas de catalogos).
typedef TablaCatalogo = TablaCns;

/// Etiqueta ACTIVO / BAJA de la columna Estado.
class EstadoChip extends StatelessWidget {
  const EstadoChip({super.key, required this.activo});

  final bool activo;

  @override
  Widget build(BuildContext context) => ChipEstado(activo ? 'ACTIVO' : 'BAJA');
}

/// Tarjeta blanca con titulo (panel "Nuevo / editar ..." del Figma).
class PanelCatalogo extends StatelessWidget {
  const PanelCatalogo({super.key, required this.titulo, required this.child, this.subtitulo});

  final String titulo;
  final String? subtitulo;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return TarjetaBlanca(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(titulo, style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800)),
          if (subtitulo != null) ...[
            const SizedBox(height: 3),
            Text(subtitulo!, style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave)),
          ],
          const SizedBox(height: 14),
          child,
        ],
      ),
    );
  }
}

/// Mensaje de error dentro de un formulario.
class MensajeError extends StatelessWidget {
  const MensajeError(this.texto, {super.key});

  final String? texto;

  @override
  Widget build(BuildContext context) {
    if (texto == null) {
      return const SizedBox.shrink();
    }
    return Container(
      margin: const EdgeInsets.only(top: 4, bottom: 12),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: ColoresCns.fondoError,
        border: Border.all(color: ColoresCns.peligro),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Text(texto!, style: const TextStyle(fontSize: 12.5, color: ColoresCns.texto)),
    );
  }
}

/// Tabla + formulario lado a lado; en ventana angosta, uno debajo del otro.
class DosColumnas extends StatelessWidget {
  const DosColumnas({super.key, required this.principal, required this.lateral, this.anchoLateral = 340});

  final Widget principal;
  final Widget lateral;
  final double anchoLateral;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, r) {
      if (r.maxWidth < anchoMinimoDosColumnas) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [principal, const SizedBox(height: 20), lateral],
        );
      }
      return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(child: principal),
          const SizedBox(width: 20),
          SizedBox(width: anchoLateral, child: lateral),
        ],
      );
    });
  }
}

/// Pide confirmacion antes de una accion (paso "Confirma la operacion" del CU-02).
Future<bool> confirmar(BuildContext context, String titulo, String texto) async {
  final r = await showDialog<bool>(
    context: context,
    builder: (c) => AlertDialog(
      title: Text(titulo),
      content: Text(texto),
      actions: [
        TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Cancelar')),
        FilledButton(onPressed: () => Navigator.pop(c, true), child: const Text('Confirmar')),
      ],
    ),
  );
  return r ?? false;
}

void avisar(BuildContext context, String texto) => mostrarAviso(context, texto);

String? validarEntero(String? valor) => int.tryParse(valor?.trim() ?? '') == null ? 'Ingrese un número' : null;

String? validarTexto(String? valor) => (valor?.trim().isEmpty ?? true) ? 'Campo obligatorio' : null;
