import 'dart:convert';

import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../core/impresion.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/medico/cirugia.dart';
import '../../models/medico/ficha_interna.dart';
import '../comun/cns_widgets.dart';

/// Ticket de 80 mm (T100-30): mismo formato para fichas internas y cirugías. Nunca se imprime la gravedad.
class TicketMedico {
  const TicketMedico({required this.tipo, required this.numero, required this.filas, required this.indicaciones});

  final String tipo;
  final int numero;
  final List<(String, String)> filas;
  final String indicaciones;

  factory TicketMedico.deFicha(FichaInterna f) {
    final espera = f.estado == EstadoFicha.enEspera;
    return TicketMedico(
      tipo: espera ? 'FICHA INTERNA · EN ESPERA' : 'FICHA INTERNA',
      numero: f.ticket,
      filas: [
        ('Paciente', f.nombrePaciente),
        ('N° afiliado', f.numeroAfiliado.isEmpty ? '—' : f.numeroAfiliado),
        ('Especialidad', f.especialidad.nombre),
        ('Profesional', f.medico ?? 'Por asignar'),
        ('Lugar', f.consultorio != null ? 'Consultorio ${f.consultorio}' : 'Por asignar'),
        ('Fecha', f.fecha != null ? fechaTicketIso(f.fecha!) : 'Sin fecha todavía'),
        ('Hora', f.hora ?? '—'),
      ],
      indicaciones: espera
          ? 'Su ficha está en espera. Le avisaremos en la app cuando tenga fecha y hora; puede pedir que le '
              'reimpriman el ticket.'
          : 'Derivado por su médico. No necesita confirmar asistencia. Si no se presenta, pierde este horario.',
    );
  }

  factory TicketMedico.deCirugia(Cirugia c) => TicketMedico(
        tipo: 'CIRUGÍA',
        numero: c.numero,
        filas: [
          ('Paciente', c.nombrePaciente),
          ('N° afiliado', c.numeroAfiliado.isEmpty ? '—' : c.numeroAfiliado),
          ('Especialidad', c.especialidad.nombre),
          ('Profesional', c.cirujano),
          ('Lugar', 'Quirófano ${c.quirofano}'),
          ('Fecha', fechaTicketIso(c.fecha)),
          ('Hora', c.inicio),
        ],
        indicaciones: 'Ayuno de 8 horas. Llegue 1 hora antes con su carnet y un acompañante.',
      );
}

Future<void> mostrarTicketMedico(BuildContext context, TicketMedico ticket) {
  return showDialog<void>(context: context, builder: (_) => TicketMedicoDialog(ticket: ticket));
}

/// Vista previa del ticket con el botón "Imprimir" (en la web abre el diálogo de impresión).
class TicketMedicoDialog extends StatefulWidget {
  const TicketMedicoDialog({super.key, required this.ticket, this.imprimir = imprimirHtml, this.ahora});

  final TicketMedico ticket;

  /// Se puede cambiar en las pruebas.
  final bool Function(String html) imprimir;
  final DateTime Function()? ahora;

  @override
  State<TicketMedicoDialog> createState() => _TicketMedicoDialogState();
}

class _TicketMedicoDialogState extends State<TicketMedicoDialog> {
  String? _aviso;

  String get _emitido {
    final ahora = (widget.ahora ?? DateTime.now)();
    return '${fechaCortaIso(isoDe(ahora))} ${horaActual(ahora)}';
  }

  void _imprimir() {
    final ok = widget.imprimir(htmlTicketMedico(widget.ticket, _emitido));
    if (ok) {
      Navigator.of(context).pop();
    } else {
      setState(() => _aviso = 'La impresión directa funciona en la versión web de la app; '
          'en esta plataforma todavía no hay impresora configurada.');
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = widget.ticket;
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
              const Text('Vista de impresión · ticket de 80 mm',
                  style: TextStyle(fontSize: 17, fontWeight: FontWeight.w800)),
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
                    Text(t.tipo, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800)),
                    Text('N° ${t.numero}',
                        key: const Key('ticket_medico_numero'),
                        style: const TextStyle(fontSize: 21, fontWeight: FontWeight.w800)),
                    const SizedBox(height: 8),
                    for (final (etiqueta, valor) in t.filas) _Dato(etiqueta, valor),
                    const SizedBox(height: 8),
                    Text(t.indicaciones, textAlign: TextAlign.center, style: const TextStyle(fontSize: 10.5)),
                    const SizedBox(height: 6),
                    Text('Emitido: $_emitido', style: const TextStyle(fontSize: 9.5, color: ColoresCns.textoSuave)),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'Mismo formato para citas comunes, fichas internas y cirugías. Nunca se imprime la gravedad.',
                style: TextStyle(fontSize: 11.5, color: ColoresCns.textoSuave),
              ),
              if (_aviso != null) ...[
                const SizedBox(height: 10),
                Text(_aviso!, style: const TextStyle(fontSize: 12, color: ColoresCns.peligro)),
              ],
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: FilledButton(
                        key: const Key('ticket_medico_imprimir'), onPressed: _imprimir, child: const Text('Imprimir')),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton(
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

/// Ticket de 80 mm para la impresora térmica.
String htmlTicketMedico(TicketMedico t, String emitido) {
  const e = HtmlEscape();
  String dato(String etiqueta, String valor) =>
      '<tr><td>${e.convert(etiqueta)}</td><td class="v">${e.convert(valor)}</td></tr>';
  return '<!doctype html><html lang="es"><head><meta charset="utf-8">'
      '<title>Ticket ${t.numero}</title><style>'
      '@page{size:80mm auto;margin:4mm}body{font-family:Arial,sans-serif;width:72mm;margin:0 auto;color:#000}'
      '.c{text-align:center}.logo{width:14mm;height:14mm;border-radius:3mm}'
      '.inst{font-size:8pt;font-weight:bold}.tipo{font-size:9pt;font-weight:bold;margin-top:2mm}'
      'h1{font-size:16pt;margin:1mm 0 2mm}'
      'table{width:100%;font-size:8.5pt;border-collapse:collapse}td{padding:1mm 0;vertical-align:top}'
      '.v{text-align:right;font-weight:bold}.ind{font-size:8pt;margin-top:2mm}.pie{font-size:7pt;margin-top:2mm}'
      '</style></head><body>'
      '<div class="c"><img class="logo" src="${e.convert(urlDeAsset('assets/logo_cns.jpg'))}" alt="">'
      '<div class="inst">CAJA NACIONAL DE SALUD · Hospital Obrero N° 1</div>'
      '<div class="tipo">${e.convert(t.tipo)}</div><h1>N° ${t.numero}</h1></div><table>'
      '${t.filas.map((f) => dato(f.$1, f.$2)).join()}'
      '</table><p class="ind">${e.convert(t.indicaciones)}</p>'
      '<p class="pie c">Emitido: ${e.convert(emitido)}<br>Si no tiene internet, este ticket es su comprobante.</p>'
      '</body></html>';
}
