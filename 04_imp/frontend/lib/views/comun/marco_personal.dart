import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import 'cns_widgets.dart';

/// Opcion del menu lateral.
class OpcionMenu {
  const OpcionMenu(this.texto, this.ruta);

  final String texto;
  final String ruta;
}

/// Desde este ancho el menu lateral queda fijo; debajo se abre con el boton de menu.
const double anchoMenuFijo = 900;

/// Marco de las pantallas del personal (Figma COMPUTER): menu verde de 240 px con el logo,
/// fondo verde, titulo de la pantalla y la pildora "Rol · Nombre". Siempre en tema claro.
class MarcoPersonal extends StatelessWidget {
  const MarcoPersonal({
    super.key,
    required this.titulo,
    required this.etiqueta,
    required this.opciones,
    required this.rutaActual,
    required this.alSalir,
    required this.child,
    this.alNavegar,
  });

  final String titulo;

  /// "Enfermería · María Flores".
  final String etiqueta;
  final List<OpcionMenu> opciones;
  final String rutaActual;
  final VoidCallback alSalir;
  final Widget child;

  /// Por defecto reemplaza la pantalla con `Navigator.pushReplacementNamed`.
  final void Function(String ruta)? alNavegar;

  void _ir(BuildContext context, String ruta) {
    if (alNavegar != null) {
      alNavegar!(ruta);
    } else if (ruta != rutaActual) {
      Navigator.of(context).pushReplacementNamed(ruta);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Theme(
      data: AppTheme.claro(),
      child: LayoutBuilder(builder: (context, r) {
        final fijo = r.maxWidth >= anchoMenuFijo;
        final menu = _MenuLateral(
          opciones: opciones,
          rutaActual: rutaActual,
          alElegir: (ruta) => _ir(context, ruta),
          alSalir: alSalir,
        );
        final contenido = _Contenido(titulo: titulo, etiqueta: etiqueta, angosto: !fijo, child: child);
        if (fijo) {
          return Scaffold(
            backgroundColor: ColoresCns.fondo,
            body: Row(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [SizedBox(width: 240, child: menu), Expanded(child: contenido)],
            ),
          );
        }
        return Scaffold(
          backgroundColor: ColoresCns.fondo,
          appBar: AppBar(
            backgroundColor: ColoresCns.menu,
            foregroundColor: ColoresCns.blanco,
            title: const Text('Hospital Obrero CNS', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800)),
          ),
          drawer: Drawer(width: 260, backgroundColor: ColoresCns.menu, child: menu),
          body: contenido,
        );
      }),
    );
  }
}

class _MenuLateral extends StatelessWidget {
  const _MenuLateral({
    required this.opciones,
    required this.rutaActual,
    required this.alElegir,
    required this.alSalir,
  });

  final List<OpcionMenu> opciones;
  final String rutaActual;
  final ValueChanged<String> alElegir;
  final VoidCallback alSalir;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: ColoresCns.menu,
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 28, 20, 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Align(alignment: Alignment.centerLeft, child: LogoCns()),
              const SizedBox(height: 12),
              const Text(
                'Hospital Obrero\nCNS',
                style: TextStyle(color: ColoresCns.blanco, fontSize: 16, fontWeight: FontWeight.w800, height: 1.15),
              ),
              const SizedBox(height: 24),
              for (final op in opciones) ...[
                _Opcion(opcion: op, activa: op.ruta == rutaActual, alTocar: () => alElegir(op.ruta)),
                const SizedBox(height: 8),
              ],
              const Spacer(),
              Align(
                alignment: Alignment.centerLeft,
                child: TextButton.icon(
                  key: const Key('menu_salir'),
                  onPressed: alSalir,
                  style: TextButton.styleFrom(foregroundColor: ColoresCns.blanco),
                  icon: const Icon(Icons.logout, size: 18),
                  label: const Text('Cerrar sesión', style: TextStyle(fontSize: 13)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Opcion extends StatelessWidget {
  const _Opcion({required this.opcion, required this.activa, required this.alTocar});

  final OpcionMenu opcion;
  final bool activa;
  final VoidCallback alTocar;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      selected: activa,
      child: Material(
        color: activa ? ColoresCns.amarillo : Colors.transparent,
        borderRadius: BorderRadius.circular(10),
        child: InkWell(
          key: Key('menu_${opcion.ruta}'),
          borderRadius: BorderRadius.circular(10),
          onTap: alTocar,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
            child: Text(
              opcion.texto,
              style: TextStyle(
                fontSize: 14,
                color: activa ? ColoresCns.texto : ColoresCns.blanco,
                fontWeight: activa ? FontWeight.w800 : FontWeight.w400,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _Contenido extends StatelessWidget {
  const _Contenido({required this.titulo, required this.etiqueta, required this.angosto, required this.child});

  final String titulo;
  final String etiqueta;
  final bool angosto;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final lado = angosto ? 16.0 : 32.0;
    return SafeArea(
      child: SingleChildScrollView(
        padding: EdgeInsets.fromLTRB(lado, angosto ? 18 : 28, lado, 32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Wrap(
              alignment: WrapAlignment.spaceBetween,
              crossAxisAlignment: WrapCrossAlignment.center,
              spacing: 12,
              runSpacing: 8,
              children: [
                Text(titulo, style: TextStyle(fontSize: angosto ? 22 : 28, fontWeight: FontWeight.w800)),
                Pildora(etiqueta),
              ],
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}
