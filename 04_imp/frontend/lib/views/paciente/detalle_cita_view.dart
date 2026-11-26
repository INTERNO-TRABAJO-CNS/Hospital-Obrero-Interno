import 'package:flutter/material.dart';

import '../../core/api_client.dart';
import '../../core/app_theme.dart';
import '../../models/citas/cita_paciente.dart';
import '../../models/comun/fechas.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/solicitud/solicitud.dart';
import '../../services/cancelacion_service.dart';
import '../citas/dialogo_cancelar.dart';
import '../comun/cns_widgets.dart';

/// Texto del estado para el chip (el codigo del backend, como en el Figma).
String codigoEstado(CitaPaciente c) => c.estado.codigo;

/// "Jueves 12 de noviembre".
String fechaTitulo(DateTime f) {
  final texto = fechaLarga(f);
  return texto.isEmpty ? texto : '${texto[0].toUpperCase()}${texto.substring(1)}';
}

/// Tarjeta "Detalle de cita" del Figma (celular y panel derecho en escritorio): datos, aviso de T-30 y
/// botones Reprogramar / Cancelar (solo si esta PROGRAMADA).
class PanelDetalleCita extends StatelessWidget {
  const PanelDetalleCita({
    super.key,
    required this.cita,
    required this.alReprogramar,
    required this.alCancelar,
    this.conEncabezado = false,
    this.trabajando = false,
  });

  final CitaPaciente cita;
  final VoidCallback alReprogramar;
  final VoidCallback alCancelar;

  /// "Detalle de cita" arriba (panel de escritorio).
  final bool conEncabezado;
  final bool trabajando;

  @override
  Widget build(BuildContext context) {
    Widget dato(String etiqueta, String valor) => Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Row(
            children: [
              Text(etiqueta, style: const TextStyle(color: ColoresCns.textoSuave, fontSize: 13)),
              const SizedBox(width: 12),
              Expanded(
                child: Text(valor,
                    textAlign: TextAlign.right, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13.5)),
              ),
            ],
          ),
        );

    return Column(
      key: Key('detalle_cita_${cita.citaId}'),
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TarjetaBlanca(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (conEncabezado) ...[
                const Text('Detalle de cita',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: ColoresCns.textoSuave)),
                const SizedBox(height: 8),
              ],
              Row(
                children: [
                  Expanded(
                    child: Text('Ficha N° ${cita.numeroFicha}',
                        style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800)),
                  ),
                  ChipEstado(codigoEstado(cita)),
                ],
              ),
              const SizedBox(height: 10),
              dato('Especialidad', cita.especialidad.nombre),
              dato('Médico', cita.medico),
              dato('Fecha', fechaTitulo(cita.inicio)),
              dato('Hora', horaTexto(cita.inicio)),
              dato('Consultorio', cita.consultorio),
              if (cita.sePuedeModificar && conEncabezado) ...[
                const SizedBox(height: 12),
                _AvisoConfirmacion(cita: cita, enCaja: true),
              ],
              if (cita.sePuedeModificar && conEncabezado) ..._botones(),
            ],
          ),
        ),
        if (cita.sePuedeModificar && !conEncabezado) ...[
          const SizedBox(height: 10),
          _AvisoConfirmacion(cita: cita, enCaja: false),
          ..._botones(),
        ],
      ],
    );
  }

  List<Widget> _botones() => [
        const SizedBox(height: 14),
        FilledButton(
          key: const Key('detalle_reprogramar'),
          onPressed: trabajando ? null : alReprogramar,
          child: Text(trabajando ? 'Reprogramando…' : 'Reprogramar cita'),
        ),
        const SizedBox(height: 10),
        FilledButton(
          key: const Key('detalle_cancelar'),
          onPressed: trabajando ? null : alCancelar,
          style: FilledButton.styleFrom(
            backgroundColor: conEncabezado ? ColoresCns.fondoError : ColoresCns.blanco,
            foregroundColor: ColoresCns.peligro,
          ),
          child: const Text('Cancelar cita'),
        ),
      ];
}

/// "Confirma tu asistencia con enfermería hasta 30 min antes (08:55 como máximo). Si no, se cancela."
class _AvisoConfirmacion extends StatelessWidget {
  const _AvisoConfirmacion({required this.cita, required this.enCaja});

  final CitaPaciente cita;
  final bool enCaja;

  @override
  Widget build(BuildContext context) {
    final texto = 'Confirma tu asistencia con enfermería hasta 30 min antes '
        '(${horaTexto(cita.limiteConfirmacion)} como máximo). Si no, se cancela automáticamente.';
    if (!enCaja) {
      return Text(texto, style: const TextStyle(fontSize: 12, color: ColoresCns.nota));
    }
    return Container(
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(color: ColoresCns.filaElegida, borderRadius: BorderRadius.circular(8)),
      child: Text(texto, style: const TextStyle(fontSize: 11.5)),
    );
  }
}

/// Pide confirmacion antes de reprogramar (CU-10 [2a]: si no confirma, la cita sigue PROGRAMADA).
Future<bool> confirmarReprogramacion(BuildContext context, CitaPaciente cita) async {
  final si = await showDialog<bool>(
    context: context,
    builder: (context) => Theme(
      data: AppTheme.claro(),
      child: DialogoAngosto(
        child: AlertDialog(
          title: const Text('¿Reprogramar la cita?'),
          content: Text(
            'Se cancelará la ficha N° ${cita.numeroFicha} del ${fechaLarga(cita.inicio)} y se creará una nueva '
            'solicitud de ${cita.especialidad.nombre} para el siguiente día hábil. El sistema te asignará médico, '
            'hora y consultorio cuando haya cupo (puede quedar en espera hasta 15 días hábiles).',
          ),
          actions: [
            Row(
              children: [
                Expanded(
                  child: FilledButton(
                    key: const Key('reprogramar_volver'),
                    onPressed: () => Navigator.of(context).pop(false),
                    style:
                        FilledButton.styleFrom(backgroundColor: ColoresCns.entrada, foregroundColor: ColoresCns.texto),
                    child: const Text('Volver'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: FilledButton(
                    key: const Key('reprogramar_si'),
                    onPressed: () => Navigator.of(context).pop(true),
                    child: const Text('Reprogramar'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    ),
  );
  return si == true;
}

/// Dialogo de ancho maximo 460 (en escritorio no se estira a toda la pantalla).
class DialogoAngosto extends StatelessWidget {
  const DialogoAngosto({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Center(child: ConstrainedBox(constraints: const BoxConstraints(maxWidth: 460), child: child));
  }
}

/// Resultado de la reprogramacion: la nueva solicitud.
Future<void> mostrarSolicitudReprogramada(BuildContext context, ResultadoSolicitud s) {
  return showDialog<void>(
    context: context,
    builder: (context) => Theme(
      data: AppTheme.claro(),
      child: DialogoAngosto(
        child: AlertDialog(
          title: const Text('Cita reprogramada'),
          content: Text('La cita anterior quedó CANCELADA. Tu nueva solicitud N° ${s.solicitudId} de '
              '${s.especialidad.nombre} busca cupo desde el ${fechaLarga(s.diaSolicitado)}.\n\n${s.mensaje}'),
          actions: [
            FilledButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Entendido')),
          ],
        ),
      ),
    ),
  );
}

/// Acciones del detalle, compartidas por la pagina de celular y el panel de escritorio.
/// Devuelve true si la cita cambio (para recargar la lista).
Future<bool> reprogramarDesdeDetalle(
  BuildContext context,
  CitaPaciente cita,
  Future<ResultadoSolicitud> Function(CitaPaciente) reprogramar,
) async {
  if (!await confirmarReprogramacion(context, cita) || !context.mounted) {
    return false;
  }
  try {
    final solicitud = await reprogramar(cita);
    if (context.mounted) {
      await mostrarSolicitudReprogramada(context, solicitud);
    }
    return true;
  } on ApiException catch (e) {
    if (context.mounted) {
      mostrarAviso(context, e.mensaje);
    }
    return e.codigo == 'ESTADO_NO_PERMITE';
  }
}

Future<bool> cancelarDesdeDetalle(BuildContext context, CitaPaciente cita, CancelacionService servicio) async {
  final cancelada =
      await mostrarDialogoCancelar(context, servicio, cita.citaId, isoDe(cita.inicio), numeroFicha: cita.numeroFicha);
  return cancelada == true;
}

/// Pantalla "Detalle de cita" del celular. Devuelve true al volver si la cita cambio.
class DetalleCitaView extends StatefulWidget {
  const DetalleCitaView({
    super.key,
    required this.cita,
    required this.reprogramar,
    required this.cancelacion,
  });

  final CitaPaciente cita;
  final Future<ResultadoSolicitud> Function(CitaPaciente) reprogramar;
  final CancelacionService cancelacion;

  @override
  State<DetalleCitaView> createState() => _DetalleCitaViewState();
}

class _DetalleCitaViewState extends State<DetalleCitaView> {
  bool _trabajando = false;

  Future<void> _reprogramar() async {
    setState(() => _trabajando = true);
    final cambio = await reprogramarDesdeDetalle(context, widget.cita, widget.reprogramar);
    if (!mounted) {
      return;
    }
    setState(() => _trabajando = false);
    if (cambio) {
      Navigator.of(context).pop(true);
    }
  }

  Future<void> _cancelar() async {
    if (await cancelarDesdeDetalle(context, widget.cita, widget.cancelacion) && mounted) {
      mostrarAviso(context, 'Cita cancelada');
      Navigator.of(context).pop(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Theme(data: AppTheme.claro(), child: Builder(builder: _contenido));
  }

  Widget _contenido(BuildContext context) {
    return Scaffold(
      backgroundColor: ColoresCns.fondo,
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(18, 14, 18, 24),
          children: [
            EncabezadoPaciente(
              titulo: 'Detalle de cita',
              alVolver: () => Navigator.of(context).maybePop(),
            ),
            const SizedBox(height: 18),
            PanelDetalleCita(
              cita: widget.cita,
              trabajando: _trabajando,
              alReprogramar: _reprogramar,
              alCancelar: _cancelar,
            ),
          ],
        ),
      ),
    );
  }
}

/// Logo + titulo grande, como el encabezado de las pantallas del paciente en el Figma (celular).
class EncabezadoPaciente extends StatelessWidget {
  const EncabezadoPaciente({super.key, required this.titulo, this.alVolver});

  final String titulo;
  final VoidCallback? alVolver;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (alVolver != null)
          IconButton(
            tooltip: 'Volver',
            onPressed: alVolver,
            icon: const Icon(Icons.arrow_back, color: ColoresCns.texto),
          ),
        const LogoCns(tamano: 46),
        const SizedBox(width: 12),
        Expanded(
          child: Text(titulo, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800)),
        ),
      ],
    );
  }
}
