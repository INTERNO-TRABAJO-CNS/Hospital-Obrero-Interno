import 'package:flutter/material.dart';

import '../../models/auth/sesion.dart';

/// Primera pantalla del paciente (celular): saludo, «Solicitar cita», «Mis citas» y avisos.
/// «Solicitar cita» (T100-06) y «Mis citas» (T100-07) llaman a [alSolicitarCita] y [alMisCitas].
class HomePacienteView extends StatelessWidget {
  const HomePacienteView({
    super.key,
    required this.sesion,
    required this.alSolicitarCita,
    required this.alMisCitas,
    required this.alRegistrarBeneficiario,
    this.avisos = const [],
  });

  final Sesion sesion;
  final VoidCallback alSolicitarCita;
  final VoidCallback alMisCitas;
  final VoidCallback alRegistrarBeneficiario;

  /// Avisos de citas (notificaciones de T100-15). Vacio mientras no existan.
  final List<String> avisos;

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final colores = tema.colorScheme;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 24, 20, 24),
      children: [
        Text('Hola, ${sesion.primerNombre}', key: const Key('home_saludo'), style: tema.textTheme.headlineSmall),
        const SizedBox(height: 4),
        Text('¿Qué desea hacer hoy?', style: tema.textTheme.bodyMedium?.copyWith(color: colores.onSurfaceVariant)),
        const SizedBox(height: 24),
        _Accion(
          clave: 'home_solicitar',
          icono: Icons.event_available,
          titulo: 'Solicitar cita',
          detalle: 'Médico General, Odontología o Pediatría',
          principal: true,
          alTocar: alSolicitarCita,
        ),
        const SizedBox(height: 12),
        _Accion(
          clave: 'home_mis_citas',
          icono: Icons.calendar_month,
          titulo: 'Mis citas',
          detalle: 'Ver, reprogramar o cancelar',
          alTocar: alMisCitas,
        ),
        const SizedBox(height: 12),
        _Accion(
          clave: 'home_beneficiario',
          icono: Icons.person_add_alt_1,
          titulo: 'Registrar beneficiario',
          detalle: 'Hijos o familiares de su afiliación',
          alTocar: alRegistrarBeneficiario,
        ),
        const SizedBox(height: 28),
        Text('Avisos', style: tema.textTheme.titleMedium),
        const SizedBox(height: 8),
        if (avisos.isEmpty)
          Card(
            key: const Key('home_sin_avisos'),
            child: ListTile(
              leading: Icon(Icons.notifications_none, color: colores.onSurfaceVariant),
              title: const Text('No tiene avisos por ahora'),
            ),
          )
        else
          for (final aviso in avisos)
            Card(child: ListTile(leading: const Icon(Icons.notifications_active_outlined), title: Text(aviso))),
      ],
    );
  }
}

class _Accion extends StatelessWidget {
  const _Accion({
    required this.clave,
    required this.icono,
    required this.titulo,
    required this.detalle,
    required this.alTocar,
    this.principal = false,
  });

  final String clave;
  final IconData icono;
  final String titulo;
  final String detalle;
  final VoidCallback alTocar;
  final bool principal;

  @override
  Widget build(BuildContext context) {
    final colores = Theme.of(context).colorScheme;
    final fondo = principal ? colores.primary : colores.surfaceContainerHighest;
    final texto = principal ? colores.onPrimary : colores.onSurface;
    return Material(
      color: fondo,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        key: Key(clave),
        borderRadius: BorderRadius.circular(16),
        onTap: alTocar,
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Row(
            children: [
              Icon(icono, color: texto, size: 30),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(titulo, style: TextStyle(color: texto, fontSize: 17, fontWeight: FontWeight.w600)),
                    const SizedBox(height: 2),
                    Text(detalle, style: TextStyle(color: texto.withValues(alpha: .85))),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: texto),
            ],
          ),
        ),
      ),
    );
  }
}
