import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../models/auth/sesion.dart';
import '../../models/citas/cita_paciente.dart';
import '../../models/comun/fechas.dart';
import '../../services/citas_paciente_service.dart';
import '../comun/cns_widgets.dart';
import 'detalle_cita_view.dart';

/// Inicio del paciente (Figma "HOME PACIENTE"): saludo con logo y "Salir", "Tu próxima cita", botones
/// "Nueva cita" y "Mis citas", "Registrar beneficiario" y la tarjeta de avisos.
class HomePacienteView extends StatefulWidget {
  const HomePacienteView({
    super.key,
    required this.sesion,
    required this.alSolicitarCita,
    required this.alMisCitas,
    required this.alRegistrarBeneficiario,
    this.alSalir,
    this.citas,
    this.avisos = const [],
    this.reloj,
  });

  final Sesion sesion;
  final VoidCallback alSolicitarCita;
  final VoidCallback alMisCitas;
  final VoidCallback alRegistrarBeneficiario;

  /// Cerrar sesión / cambiar de usuario. Sin él no se muestra el botón.
  final VoidCallback? alSalir;

  /// Para mostrar "Tu próxima cita". Sin servicio no se muestra.
  final CitasPacienteService? citas;

  /// Avisos de citas (notificaciones de T100-15).
  final List<String> avisos;
  final DateTime Function()? reloj;

  @override
  State<HomePacienteView> createState() => _HomePacienteViewState();
}

class _HomePacienteViewState extends State<HomePacienteView> {
  CitaPaciente? _proxima;

  @override
  void initState() {
    super.initState();
    _cargarProxima();
  }

  Future<void> _cargarProxima() async {
    final servicio = widget.citas;
    if (servicio == null) {
      return;
    }
    try {
      final ahora = (widget.reloj ?? DateTime.now)();
      final proximas = (await servicio.misCitas()).where((c) => c.esProxima(ahora)).toList()
        ..sort((a, b) => a.inicio.compareTo(b.inicio));
      if (mounted) {
        setState(() => _proxima = proximas.isEmpty ? null : proximas.first);
      }
    } catch (_) {
      // Sin la próxima cita el inicio sigue sirviendo: "Mis citas" muestra el error.
    }
  }

  @override
  Widget build(BuildContext context) {
    final proxima = _proxima;
    return Theme(
      data: AppTheme.claro(),
      child: ColoredBox(
        color: ColoresCns.fondo,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(18, 14, 18, 24),
          children: [
            EncabezadoPaciente(
              key: const Key('home_saludo'),
              titulo: 'Hola, ${widget.sesion.primerNombre}',
              alSalir: widget.alSalir,
            ),
            const SizedBox(height: 20),
            if (proxima != null) ...[
              const Text('Tu próxima cita', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800)),
              const SizedBox(height: 10),
              _ProximaCita(cita: proxima),
              const SizedBox(height: 16),
            ],
            _Boton(
              clave: 'home_solicitar',
              texto: 'Nueva cita',
              detalle: 'Médico General, Odontología o Pediatría',
              principal: true,
              alTocar: widget.alSolicitarCita,
            ),
            const SizedBox(height: 12),
            _Boton(clave: 'home_mis_citas', texto: 'Mis citas', alTocar: widget.alMisCitas),
            const SizedBox(height: 12),
            _Boton(
              clave: 'home_beneficiario',
              texto: 'Registrar beneficiario',
              detalle: 'Hijos o familiares de su afiliación',
              alTocar: widget.alRegistrarBeneficiario,
            ),
            const SizedBox(height: 16),
            TarjetaBlanca(
              key: widget.avisos.isEmpty ? const Key('home_sin_avisos') : const Key('home_avisos'),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Avisos', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w800)),
                  const SizedBox(height: 6),
                  if (widget.avisos.isEmpty)
                    const Text('No tiene avisos por ahora', style: TextStyle(fontSize: 14))
                  else
                    for (final aviso in widget.avisos)
                      Padding(
                        padding: const EdgeInsets.only(top: 2),
                        child: Text(aviso, style: const TextStyle(fontSize: 14)),
                      ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Tarjeta blanca "Tu próxima cita" del Figma.
class _ProximaCita extends StatelessWidget {
  const _ProximaCita({required this.cita});

  final CitaPaciente cita;

  @override
  Widget build(BuildContext context) {
    final lugar = [
      cita.especialidad.nombre,
      if (cita.medico.isNotEmpty) cita.medico,
      if (cita.consultorio.isNotEmpty)
        int.tryParse(cita.consultorio) == null ? cita.consultorio : 'Consultorio ${cita.consultorio}',
    ].join(' · ');
    return TarjetaBlanca(
      key: const Key('home_proxima_cita'),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text('Ficha N° ${cita.numeroFicha}',
                    style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
              ),
              ChipEstado(codigoEstado(cita)),
            ],
          ),
          const SizedBox(height: 8),
          Text('${fechaTitulo(cita.inicio)} · ${horaTexto(cita.inicio)}',
              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800)),
          const SizedBox(height: 6),
          Text(lugar, style: const TextStyle(color: ColoresCns.textoSuave, fontSize: 14)),
          const SizedBox(height: 6),
          const Text(
            'Debes confirmar tu llegada con enfermería en el hospital hasta 30 min antes de tu hora.',
            style: TextStyle(color: ColoresCns.textoSuave, fontSize: 12.5),
          ),
        ],
      ),
    );
  }
}

/// Botón ancho del Figma: amarillo ("Nueva cita") o blanco ("Mis citas").
class _Boton extends StatelessWidget {
  const _Boton({
    required this.clave,
    required this.texto,
    required this.alTocar,
    this.detalle,
    this.principal = false,
  });

  final String clave;
  final String texto;
  final String? detalle;
  final VoidCallback alTocar;
  final bool principal;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: principal ? ColoresCns.amarillo : ColoresCns.blanco,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        key: Key(clave),
        borderRadius: BorderRadius.circular(16),
        onTap: alTocar,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 13),
          child: Column(
            children: [
              Text(texto, style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800)),
              if (detalle != null) ...[
                const SizedBox(height: 2),
                Text(detalle!,
                    textAlign: TextAlign.center, style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave)),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
