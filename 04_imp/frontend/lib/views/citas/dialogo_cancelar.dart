import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../core/api_client.dart';
import '../../models/comun/fechas_iso.dart';
import '../../services/cancelacion_service.dart';

Future<bool?> mostrarDialogoCancelar(
  BuildContext context,
  CancelacionService servicio,
  int citaId,
  String fechaIso,
) {
  return showDialog<bool>(
    context: context,
    builder: (_) => _DialogoCancelar(
      servicio: servicio,
      citaId: citaId,
      fechaIso: fechaIso,
    ),
  );
}

class _DialogoCancelar extends StatefulWidget {
  const _DialogoCancelar({
    required this.servicio,
    required this.citaId,
    required this.fechaIso,
  });

  final CancelacionService servicio;
  final int citaId;
  final String fechaIso;

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
      await widget.servicio.cancelar(widget.citaId);
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
            'Se cancelará la ficha N° ${widget.citaId} del '
            '${_fechaCancelacion(widget.fechaIso)}. '
            'La cita se conserva como CANCELADA y se libera del cupo.',
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
