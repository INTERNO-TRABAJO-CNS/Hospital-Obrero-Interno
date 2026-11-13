import 'dart:convert';

import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../core/impresion.dart';
import '../../models/comun/fechas.dart';
import '../../models/enfermeria/ficha_dia.dart';
import '../comun/cns_widgets.dart';

/// Figma "ENFERMERIA IMPRIMIR TICKET": vista previa del ticket de una cita comun.
Future<void> mostrarTicketCita(BuildContext context, FichaDelDia ficha) {
  return showDialog<void>(context: context, builder: (_) => TicketCitaDialog(ficha: ficha));
}

class TicketCitaDialog extends StatefulWidget {
  const TicketCitaDialog({super.key, required this.ficha, this.imprimir = imprimirHtml});

  final FichaDelDia ficha;

  /// Se puede cambiar en las pruebas.
  final bool Function(String html) imprimir;

  @override
  State<TicketCitaDialog> createState() => _TicketCitaDialogState();
}

class _TicketCitaDialogState extends State<TicketCitaDialog> {
  String? _aviso;

  void _imprimir() {
    final ok = widget.imprimir(htmlTicket(widget.ficha));
    if (ok) {
      Navigator.of(context).pop();
    } else {
      setState(() => _aviso = 'La impresión directa funciona en la versión web de la app; '
          'en esta plataforma todavía no hay impresora configurada.');
    }
  }

  @override
  Widget build(BuildContext context) {
    final f = widget.ficha;
    return Dialog(
      backgroundColor: ColoresCns.blanco,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 380),
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Imprimir ticket · Ficha N° ${f.numeroFicha}',
                  style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800)),
              const SizedBox(height: 14),
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: const Color(0xFFFAFAFA),
                  border: Border.all(color: ColoresCns.entrada),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Column(
                  children: [
                    const LogoCns(tamano: 34),
                    const SizedBox(height: 6),
                    const Text('CAJA NACIONAL DE SALUD · Hospital Obrero N° 1',
                        textAlign: TextAlign.center, style: TextStyle(fontSize: 9.5, fontWeight: FontWeight.w800)),
                    const SizedBox(height: 6),
                    Text('CITA  ·  N° ${f.numeroFicha}',
                        key: const Key('ticket_numero'),
                        style: const TextStyle(fontSize: 21, fontWeight: FontWeight.w800)),
                    const SizedBox(height: 8),
                    _Dato('Paciente', '${f.paciente} · ${f.numeroAfiliado}'),
                    _Dato('Especialidad', f.especialidad.nombre),
                    _Dato('Profesional', '${f.medico} · Consultorio ${f.consultorio}'),
                    _Dato('Fecha y hora', '${fechaConDia(f.inicio)} · ${horaTexto(f.inicio)}'),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'Para pacientes sin internet. Enfermería imprime tickets de citas comunes; los de fichas internas '
                'y cirugías los imprime el médico o especialista desde la web.',
                style: TextStyle(fontSize: 11.5, color: ColoresCns.textoSuave),
              ),
              if (_aviso != null) ...[
                const SizedBox(height: 10),
                Text(_aviso!,
                    key: const Key('ticket_aviso'), style: const TextStyle(fontSize: 12, color: ColoresCns.peligro)),
              ],
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: FilledButton(
                        key: const Key('ticket_imprimir'), onPressed: _imprimir, child: const Text('Imprimir')),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton(
                      key: const Key('ticket_cerrar'),
                      onPressed: () => Navigator.of(context).pop(),
                      child: const Text('Cerrar'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Dato extends StatelessWidget {
  const _Dato(this.etiqueta, this.valor);

  final String etiqueta;
  final String valor;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(etiqueta, style: const TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
          const SizedBox(width: 12),
          Expanded(
            child: Text(valor,
                textAlign: TextAlign.right, style: const TextStyle(fontSize: 11.5, fontWeight: FontWeight.w800)),
          ),
        ],
      ),
    );
  }
}

/// Ticket de 80 mm para la impresora termica.
String htmlTicket(FichaDelDia f) {
  const e = HtmlEscape();
  String dato(String etiqueta, String valor) =>
      '<tr><td>${e.convert(etiqueta)}</td><td class="v">${e.convert(valor)}</td></tr>';
  return '<!doctype html><html lang="es"><head><meta charset="utf-8">'
      '<title>Ticket ficha ${f.numeroFicha}</title><style>'
      '@page{size:80mm auto;margin:4mm}body{font-family:Arial,sans-serif;width:72mm;margin:0 auto;color:#000}'
      '.c{text-align:center}.logo{width:14mm;height:14mm;border-radius:3mm}'
      '.inst{font-size:8pt;font-weight:bold}h1{font-size:16pt;margin:2mm 0}'
      'table{width:100%;font-size:8.5pt;border-collapse:collapse}td{padding:1mm 0;vertical-align:top}'
      '.v{text-align:right;font-weight:bold}</style></head><body>'
      '<div class="c"><img class="logo" src="${e.convert(urlDeAsset('assets/logo_cns.jpg'))}" alt="">'
      '<div class="inst">CAJA NACIONAL DE SALUD · Hospital Obrero N° 1</div>'
      '<h1>CITA · N° ${f.numeroFicha}</h1></div><table>'
      '${dato('Paciente', '${f.paciente} · ${f.numeroAfiliado}')}'
      '${dato('Especialidad', f.especialidad.nombre)}'
      '${dato('Profesional', '${f.medico} · Consultorio ${f.consultorio}')}'
      '${dato('Fecha y hora', '${fechaConDia(f.inicio)} · ${horaTexto(f.inicio)}')}'
      '</table></body></html>';
}
