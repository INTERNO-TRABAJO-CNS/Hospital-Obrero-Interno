import 'package:flutter/material.dart';

/// Debajo de este ancho la tabla y el formulario se apilan (ventana angosta).
const double anchoMinimoDosColumnas = 900;

/// Columna de [TablaCatalogo].
class ColumnaTabla {
  const ColumnaTabla(this.titulo, {this.flex = 1});

  final String titulo;
  final int flex;
}

/// Tabla del Figma: encabezado verde, filas alternadas y fila seleccionable.
class TablaCatalogo extends StatelessWidget {
  const TablaCatalogo({
    super.key,
    required this.columnas,
    required this.filas,
    this.alTocarFila,
    this.seleccionada,
    this.textoVacio = 'Sin registros',
    this.filaFija,
  });

  final List<ColumnaTabla> columnas;
  final List<List<Widget>> filas;
  final ValueChanged<int>? alTocarFila;
  final int? seleccionada;
  final String textoVacio;

  /// Fila informativa al inicio (no seleccionable), p. ej. "Sáb–Dom".
  final List<Widget>? filaFija;

  @override
  Widget build(BuildContext context) {
    final esquema = Theme.of(context).colorScheme;
    final estiloEncabezado = Theme.of(context)
        .textTheme
        .labelLarge
        ?.copyWith(color: esquema.onPrimary, fontWeight: FontWeight.bold);

    Widget fila(List<Widget> celdas, {Color? color, VoidCallback? alTocar, Key? key}) {
      return Material(
        key: key,
        color: color ?? esquema.surface,
        child: InkWell(
          onTap: alTocar,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                for (var i = 0; i < columnas.length; i++)
                  Expanded(
                    flex: columnas[i].flex,
                    child: Align(alignment: Alignment.centerLeft, child: celdas[i]),
                  ),
              ],
            ),
          ),
        ),
      );
    }

    final cuerpo = <Widget>[
      if (filaFija != null) fila(filaFija!, color: esquema.surfaceContainerHighest),
      for (var i = 0; i < filas.length; i++)
        fila(
          filas[i],
          key: ValueKey('fila_$i'),
          color: i == seleccionada
              ? esquema.secondaryContainer
              : (i.isOdd ? esquema.primaryContainer.withValues(alpha: 0.35) : esquema.surface),
          alTocar: alTocarFila == null ? null : () => alTocarFila!(i),
        ),
      if (filas.isEmpty && filaFija == null)
        Padding(
          padding: const EdgeInsets.all(24),
          child: Center(child: Text(textoVacio)),
        ),
    ];

    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Container(
            color: esquema.primary,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                for (final c in columnas)
                  Expanded(flex: c.flex, child: Text(c.titulo, style: estiloEncabezado)),
              ],
            ),
          ),
          ...cuerpo,
        ],
      ),
    );
  }
}

/// Etiqueta ACTIVO / BAJA de la columna Estado.
class EstadoChip extends StatelessWidget {
  const EstadoChip({super.key, required this.activo});

  final bool activo;

  @override
  Widget build(BuildContext context) {
    final esquema = Theme.of(context).colorScheme;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
      decoration: BoxDecoration(
        color: activo ? esquema.tertiaryContainer : esquema.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        activo ? 'ACTIVO' : 'BAJA',
        style: Theme.of(context).textTheme.labelSmall?.copyWith(fontWeight: FontWeight.bold),
      ),
    );
  }
}

/// Tarjeta blanca con titulo (panel "Nuevo / editar ..." y tarjetas de tablas).
class PanelCatalogo extends StatelessWidget {
  const PanelCatalogo({super.key, required this.titulo, required this.child});

  final String titulo;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(titulo, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 16),
            child,
          ],
        ),
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
    final esquema = Theme.of(context).colorScheme;
    return Container(
      margin: const EdgeInsets.only(top: 12),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(color: esquema.errorContainer, borderRadius: BorderRadius.circular(8)),
      child: Text(texto!, style: TextStyle(color: esquema.onErrorContainer)),
    );
  }
}

/// Tabla + formulario lado a lado; en ventana angosta, uno debajo del otro.
class DosColumnas extends StatelessWidget {
  const DosColumnas({super.key, required this.principal, required this.lateral, this.anchoLateral = 320});

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

void avisar(BuildContext context, String texto) {
  ScaffoldMessenger.maybeOf(context)?.showSnackBar(SnackBar(content: Text(texto)));
}

String? validarEntero(String? valor) =>
    int.tryParse(valor?.trim() ?? '') == null ? 'Ingrese un número' : null;

String? validarTexto(String? valor) => (valor?.trim().isEmpty ?? true) ? 'Campo obligatorio' : null;
