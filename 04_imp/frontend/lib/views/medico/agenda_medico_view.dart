import 'package:flutter/material.dart';

import '../../controllers/medico_controller.dart';
import '../../core/app_theme.dart';
import '../../core/rutas_medico.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/enfermeria/ficha_dia.dart';
import '../../models/medico/agenda_medico.dart';
import '../../models/medico/medico_actual.dart';
import '../comun/cns_widgets.dart';
import 'medico_widgets.dart';

/// Figma "WEB MEDICO AGENDA" (CU-13): citas del día con "Atender" y "Ticket interno".
class AgendaMedicoView extends StatefulWidget {
  const AgendaMedicoView({super.key, required this.agenda, required this.medico});

  final AgendaMedicoController agenda;
  final MedicoActual medico;

  @override
  State<AgendaMedicoView> createState() => _AgendaMedicoViewState();
}

class _AgendaMedicoViewState extends State<AgendaMedicoView> {
  AgendaMedicoController get _c => widget.agenda;

  @override
  void initState() {
    super.initState();
    _c.cargar();
  }

  String? _derivacion(FichaAgenda f) {
    final ficha = _c.derivaciones[f.citaId];
    return ficha == null ? null : 'Derivada a ${ficha.especialidad.nombre} · Ticket N° ${ficha.ticket}';
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final agenda = _c.agenda;
        final fichas = agenda?.fichas ?? const <FichaAgenda>[];
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilaFecha(
              resumen: _c.resumen,
              fecha: agenda?.fecha ?? _c.fechaElegida ?? _c.hoy,
              alCambiar: _c.cargar,
              alHoy: _c.volverAHoy,
            ),
            const SizedBox(height: 14),
            if (_c.error != null) TextoError(_c.error!),
            if (_c.cargando && agenda == null)
              const Cargando()
            else
              TablaCns(
                anchoMinimo: 760,
                textoVacio: 'No hay citas para esta fecha.',
                columnas: const [
                  ColumnaTabla('Ficha'),
                  ColumnaTabla('Hora'),
                  ColumnaTabla('Paciente', flex: 3),
                  ColumnaTabla('Estado', flex: 3),
                  ColumnaTabla('Acciones', flex: 5),
                ],
                resaltadas: {
                  for (var i = 0; i < fichas.length; i++)
                    if (fichas[i].estado == EstadoCita.asistenciaConfirmada) i
                },
                filas: [for (final f in fichas) _fila(f)],
              ),
            const NotaPie(
                '«Atender» solo con ASISTENCIA_CONFIRMADA. «Ticket interno» solo para citas de hoy confirmadas '
                'o atendidas (una derivación por cita). Las canceladas se muestran marcadas y cuentan en el total.'),
          ],
        );
      },
    );
  }

  List<Widget> _fila(FichaAgenda f) {
    final cancelada = f.estado == EstadoCita.cancelada;
    final tachado = TextStyle(
      decoration: cancelada ? TextDecoration.lineThrough : null,
      color: cancelada ? ColoresCns.textoSuave : null,
    );
    final derivacion = _derivacion(f);
    return [
      Text('${f.numeroFicha}', style: tachado),
      Text(horaCorta(f.hora), style: tachado),
      cancelada
          ? Text(f.nombrePaciente, style: tachado)
          : InkWell(
              key: Key('paciente_${f.citaId}'),
              onTap: () => irA(context, RutasMedico.historialDe(f.pacienteId)),
              child: Text(f.nombrePaciente,
                  style: const TextStyle(decoration: TextDecoration.underline, fontWeight: FontWeight.w700)),
            ),
      ChipEstado(f.estado.codigo),
      Wrap(
        spacing: 6,
        runSpacing: 6,
        crossAxisAlignment: WrapCrossAlignment.center,
        children: [
          if (_c.puedeAtender(f))
            FilledButton(
              key: Key('atender_${f.citaId}'),
              style: FilledButton.styleFrom(minimumSize: const Size(0, 32)),
              onPressed: () => irA(context, RutasMedico.atender(f.citaId)),
              child: const Text('Atender'),
            ),
          if (_c.puedeTicketInterno(f, widget.medico))
            OutlinedButton(
              key: Key('ticket_interno_${f.citaId}'),
              style: OutlinedButton.styleFrom(minimumSize: const Size(0, 32), backgroundColor: ColoresCns.blanco),
              onPressed: () => irA(context, RutasMedico.ticketInternoDe(f.citaId)),
              child: const Text('Ticket interno'),
            ),
          if (derivacion != null)
            Text(derivacion, style: const TextStyle(fontSize: 11.5, color: ColoresCns.textoSuave))
          else if (cancelada)
            const Text('No confirmó su asistencia', style: TextStyle(fontSize: 11.5, color: ColoresCns.textoSuave)),
        ],
      ),
    ];
  }
}
