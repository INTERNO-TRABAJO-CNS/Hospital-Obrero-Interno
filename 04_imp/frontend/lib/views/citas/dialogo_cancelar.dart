import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../core/api_client.dart';
import '../../models/comun/fechas_iso.dart';
import '../../services/cancelacion_service.dart';

Future<bool?> mostrarDialogoCancelar(
  BuildContext context,
  CancelacionService servicio,
  int citaId,
  String fechaIso, {
  int? numeroFicha,
  int? pacienteId,
}) {
  return showDialog<bool>(
    context: context,
    // El Figma no tiene modo oscuro: el dialogo siempre con el tema claro.
    builder: (_) => Theme(
      data: AppTheme.claro(),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 460),
          child: _DialogoCancelar(
            servicio: servicio,
            citaId: citaId,
            fechaIso: fechaIso,
            numeroFicha: numeroFicha,
            pacienteId: pacienteId,
          ),
        ),
      ),
    ),
  );
}

class _DialogoCancelar extends StatefulWidget {
  const _DialogoCancelar({
    required this.servicio,
    required this.citaId,
    required this.fechaIso,
    this.numeroFicha,
    this.pacienteId,
  });

  final CancelacionService servicio;
  final int citaId;
  final String fechaIso;

  /// Numero de ficha que ve el paciente (si no se conoce, se muestra el de la cita).
  final int? numeroFicha;

  /// Beneficiario dueno de la cita (RN-05); null si es del titular.
  final int? pacienteId;

  @override
  State<_DialogoCancelar> createState() => _DialogoCancelarState();
}

class _DialogoCancelarState extends State<_DialogoCancelar> {
  bool _trabajando = false;
  String? _error;

  Future<void> _cancelar() async {
    setState(() {
      _trabajando = true;
      _error = null;
    });
    try {
      await widget.servicio.cancelar(widget.citaId, pacienteId: widget.pacienteId);
      if (mounted) {
        Navigator.of(context).pop(true);
      }
    } on ApiException catch (e) {
      if (mounted) {
        setState(() {
          _trabajando = false;
          _error = e.mensaje;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _trabajando = false;
          _error = 'No se pudo cancelar la cita';
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('¿Cancelar la cita?'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Se cancelará la ficha N° ${widget.numeroFicha ?? widget.citaId} del '
            '${_fechaCancelacion(widget.fechaIso)}. '
            'La cita se conserva como CANCELADA y se libera el cupo.',
          ),
          if (_error != null) ...[
            const SizedBox(height: 12),
            Text(
              _error!,
              style: const TextStyle(color: ColoresCns.peligro),
            ),
          ],
        ],
      ),
      actions: [
        Row(
          children: [
            Expanded(
              child: FilledButton(
                key: const Key('cancelar_volver'),
                onPressed:
                    _trabajando ? null : () => Navigator.of(context).pop(false),
                style: FilledButton.styleFrom(
                  backgroundColor: ColoresCns.entrada,
                  foregroundColor: ColoresCns.texto,
                ),
                child: const Text('Volver'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: FilledButton(
                key: const Key('cancelar_si'),
                onPressed: _trabajando ? null : _cancelar,
                style: FilledButton.styleFrom(
                  backgroundColor: ColoresCns.peligro,
                  foregroundColor: ColoresCns.blanco,
                ),
                child: Text(_trabajando ? 'Cancelando...' : 'Sí, cancelar'),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

String _fechaCancelacion(String fechaIso) {
  const dias = [
    'lunes',
    'martes',
    'miércoles',
    'jueves',
    'viernes',
    'sábado',
    'domingo',
  ];
  const meses = [
    'enero',
    'febrero',
    'marzo',
    'abril',
    'mayo',
    'junio',
    'julio',
    'agosto',
    'septiembre',
    'octubre',
    'noviembre',
    'diciembre',
  ];
  final fecha = fechaDeIso(fechaIso);
  return '${dias[fecha.weekday - 1]} ${fecha.day} de ${meses[fecha.month - 1]}';
}
