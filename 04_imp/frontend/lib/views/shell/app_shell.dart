import 'package:flutter/material.dart';

import '../../core/responsive.dart';

/// Un destino del menu del marco (una pantalla a la que se puede ir).
class DestinoShell {
  const DestinoShell({required this.etiqueta, required this.icono, required this.ruta});

  final String etiqueta;
  final IconData icono;
  final String ruta;
}

/// Marco adaptable de la app: barra inferior en celular, riel lateral en
/// escritorio y sin menu en TV (la TV es solo lectura).
class AppShell extends StatelessWidget {
  const AppShell({
    super.key,
    required this.rutaActual,
    required this.destinos,
    required this.child,
    this.alNavegar,
  });

  final String rutaActual;
  final List<DestinoShell> destinos;
  final Widget child;

  /// Por defecto reemplaza la pantalla con `Navigator.pushReplacementNamed`.
  final void Function(String ruta)? alNavegar;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, restricciones) {
        final dispositivo = dispositivoDe(restricciones.maxWidth, ruta: rutaActual);
        // NavigationBar y NavigationRail necesitan al menos 2 destinos.
        final sinMenu = dispositivo == Dispositivo.tv || destinos.length < 2;
        if (sinMenu) {
          return Scaffold(body: SafeArea(child: child));
        }

        final indice = _indiceActual();
        void ir(int i) => _navegar(context, destinos[i].ruta);

        if (dispositivo == Dispositivo.celular) {
          return Scaffold(
            body: SafeArea(child: child),
            bottomNavigationBar: NavigationBar(
              selectedIndex: indice,
              onDestinationSelected: ir,
              destinations: [
                for (final d in destinos)
                  NavigationDestination(icon: Icon(d.icono), label: d.etiqueta),
              ],
            ),
          );
        }

        return Scaffold(
          body: SafeArea(
            child: Row(
              children: [
                NavigationRail(
                  selectedIndex: indice,
                  onDestinationSelected: ir,
                  labelType: NavigationRailLabelType.all,
                  destinations: [
                    for (final d in destinos)
                      NavigationRailDestination(icon: Icon(d.icono), label: Text(d.etiqueta)),
                  ],
                ),
                const VerticalDivider(width: 1),
                Expanded(child: child),
              ],
            ),
          ),
        );
      },
    );
  }

  int _indiceActual() {
    final i = destinos.indexWhere((d) => d.ruta == rutaActual);
    return i < 0 ? 0 : i;
  }

  void _navegar(BuildContext context, String ruta) {
    if (alNavegar != null) {
      alNavegar!(ruta);
    } else if (ruta != rutaActual) {
      Navigator.of(context).pushReplacementNamed(ruta);
    }
  }
}
