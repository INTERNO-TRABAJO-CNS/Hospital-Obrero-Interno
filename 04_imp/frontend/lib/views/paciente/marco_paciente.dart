import 'package:flutter/material.dart';

import '../../core/app_theme.dart';

/// Pestañas de la barra inferior del paciente (Figma "HOME PACIENTE" / "MIS CITAS").
enum PestanaPaciente {
  inicio('Inicio'),
  misCitas('Mis citas'),
  avisos('Avisos');

  const PestanaPaciente(this.etiqueta);

  final String etiqueta;
}

/// Marco de las pantallas del paciente: fondo verde del Figma y barra inferior verde oscuro con
/// "Inicio / Mis citas / Avisos" (la elegida en negrita). Siempre en tema claro: el Figma no tiene modo oscuro.
/// La cabecera (logo, titulo y "Salir") la pone cada pantalla con `EncabezadoPaciente`.
class MarcoPaciente extends StatelessWidget {
  const MarcoPaciente({super.key, required this.actual, required this.alElegir, required this.child});

  final PestanaPaciente actual;
  final ValueChanged<PestanaPaciente> alElegir;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Theme(
      data: AppTheme.claro(),
      child: Scaffold(
        backgroundColor: ColoresCns.fondo,
        body: SafeArea(bottom: false, child: child),
        bottomNavigationBar: BarraPaciente(actual: actual, alElegir: alElegir),
      ),
    );
  }
}

/// Barra inferior del Figma: tres textos blancos sobre verde oscuro.
class BarraPaciente extends StatelessWidget {
  const BarraPaciente({super.key, required this.actual, required this.alElegir});

  final PestanaPaciente actual;
  final ValueChanged<PestanaPaciente> alElegir;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: ColoresCns.menu,
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 56,
          child: Row(
            children: [
              for (final p in PestanaPaciente.values)
                Expanded(
                  child: Semantics(
                    selected: p == actual,
                    button: true,
                    child: InkWell(
                      key: Key('pestana_${p.name}'),
                      onTap: p == actual ? null : () => alElegir(p),
                      child: Center(
                        child: Text(
                          p.etiqueta,
                          style: TextStyle(
                            color: ColoresCns.blanco,
                            fontSize: 15,
                            fontWeight: p == actual ? FontWeight.w800 : FontWeight.w400,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
