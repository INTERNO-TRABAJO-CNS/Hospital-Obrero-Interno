import 'package:flutter/material.dart';

import '../../controllers/historial_paciente_controller.dart';
import '../../controllers/medico_controller.dart';
import '../../core/api_client.dart';
import '../../core/app_theme.dart';
import '../../core/rutas_medico.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/enfermeria/ficha_dia.dart';
import '../../models/medico/historial_paciente.dart';
import '../../services/cirugias_service.dart';
import '../../services/fichas_internas_service.dart';
import '../../services/medico_service.dart';
import '../comun/cns_widgets.dart';
import 'medico_widgets.dart';

/// "Buscar por N° de afiliado…": solo encuentra pacientes de la agenda del médico.
class BuscadorPaciente extends StatefulWidget {
  const BuscadorPaciente({super.key, required this.servicio});

  final HistorialPacienteService servicio;

  @override
  State<BuscadorPaciente> createState() => _BuscadorPacienteState();
}

class _BuscadorPacienteState extends State<BuscadorPaciente> {
  final _texto = TextEditingController();
  List<PacienteDeAgenda>? _resultados;
  String? _error;

  @override
  void dispose() {
    _texto.dispose();
    super.dispose();
  }

  Future<void> _buscar() async {
    final texto = _texto.text.trim();
    if (texto.isEmpty) {
      return;
    }
    setState(() => _error = null);
    try {
      final r = await widget.servicio.buscar(texto);
      if (!mounted) {
        return;
      }
      if (r.length == 1) {
        irA(context, RutasMedico.historialDe(r.first.pacienteId));
        setState(() => _resultados = null);
      } else {
        setState(() => _resultados = r);
      }
    } on ApiException catch (e) {
      setState(() => _error = e.mensaje);
    }
  }

  @override
  Widget build(BuildContext context) {
    final r = _resultados;
    return SizedBox(
      width: 300,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            key: const Key('buscar_afiliado'),
            controller: _texto,
            textInputAction: TextInputAction.search,
            onSubmitted: (_) => _buscar(),
            decoration: decoracionBlanca('Buscar por N° de afiliado…',
                icono: IconButton(icon: const Icon(Icons.search), onPressed: _buscar, tooltip: 'Buscar')),
          ),
          if (_error != null) TextoError(_error!),
          if (r != null && _error == null)
            Container(
              margin: const EdgeInsets.only(top: 6),
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: ColoresCns.blanco,
                border: Border.all(color: const Color(0xFF9A9A9A)),
                borderRadius: BorderRadius.circular(10),
              ),
              child: r.isEmpty
                  ? const Text('Ningún paciente de tu agenda con ese N° de afiliado.',
                      style: TextStyle(fontSize: 12, color: ColoresCns.textoSuave))
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        for (final p in r)
                          InkWell(
                            onTap: () => irA(context, RutasMedico.historialDe(p.pacienteId)),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(vertical: 6, horizontal: 4),
                              child: Text('${p.nombre} · ${p.titular ? 'Titular' : 'Beneficiario'} · ${p.edad} años',
                                  style: const TextStyle(fontSize: 12)),
                            ),
                          ),
                      ],
                    ),
            ),
        ],
      ),
    );
  }
}

/// "Historial" del menú: buscar al paciente o elegirlo de la agenda.
class HistorialInicioView extends StatefulWidget {
  const HistorialInicioView({super.key, required this.agenda, required this.servicio});

  final AgendaMedicoController agenda;
  final HistorialPacienteService servicio;

  @override
  State<HistorialInicioView> createState() => _HistorialInicioViewState();
}

class _HistorialInicioViewState extends State<HistorialInicioView> {
  @override
  void initState() {
    super.initState();
    if (widget.agenda.agenda == null) {
      widget.agenda.cargar();
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: widget.agenda,
      builder: (context, _) {
        final pacientes = [
          for (final f in widget.agenda.agenda?.fichas ?? const [])
            if (f.estado != EstadoCita.cancelada) f
        ];
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TarjetaBlanca(
              child: Wrap(
                alignment: WrapAlignment.spaceBetween,
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 12,
                runSpacing: 12,
                children: [
                  const Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text('Elige un paciente', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
                      Ayuda('Solo ves pacientes de tu agenda.'),
                    ],
                  ),
                  BuscadorPaciente(servicio: widget.servicio),
                ],
              ),
            ),
            const SizedBox(height: 16),
            TarjetaBlanca(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  TituloTarjeta('Pacientes de la agenda ${widget.agenda.esHoy ? 'de hoy' : 'elegida'}'),
                  if (pacientes.isEmpty) const Ayuda('No hay pacientes en la agenda.'),
                  for (final f in pacientes)
                    Card(
                      color: ColoresCns.filaPar,
                      elevation: 0,
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        dense: true,
                        title: Text(f.nombrePaciente, style: const TextStyle(fontWeight: FontWeight.w800)),
                        subtitle: Text('Ficha ${f.numeroFicha} · ${horaCorta(f.hora)}'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () => irA(context, RutasMedico.historialDe(f.pacienteId)),
                      ),
                    ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }
}

/// Figma "WEB MEDICO HISTORIAL PACIENTE" (CU-14): línea de tiempo con filtros.
class HistorialPacienteView extends StatefulWidget {
  const HistorialPacienteView({
    super.key,
    required this.pacienteId,
    required this.servicio,
    this.fichas,
    this.cirugias,
  });

  final int pacienteId;
  final HistorialPacienteService servicio;
  final FichasInternasService? fichas;
  final CirugiasService? cirugias;

  @override
  State<HistorialPacienteView> createState() => _HistorialPacienteViewState();
}

class _HistorialPacienteViewState extends State<HistorialPacienteView> {
  late final HistorialPacienteController _c =
      HistorialPacienteController(widget.servicio, fichas: widget.fichas, cirugias: widget.cirugias);

  @override
  void initState() {
    super.initState();
    _c.cargar(widget.pacienteId);
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  String get _subtitulo {
    final h = _c.historial;
    if (h == null) {
      return '';
    }
    return [
      if (h.numeroAfiliado != null) 'Afiliado N° ${h.numeroAfiliado}',
      if (h.titular == true) 'Titular',
      if (h.titular == false) 'Beneficiario',
      if (h.edad != null) '${h.edad} años',
      'Solo ves pacientes de tu agenda',
    ].join(' · ');
  }

  Color _colorTipo(TipoEvento t) => switch (t) {
        TipoEvento.atencion => const Color(0xFFCFE8CF),
        TipoEvento.cita => ColoresCns.basica,
        TipoEvento.fichaInterna => ColoresCns.interna,
        TipoEvento.cirugia => const Color(0xFFF7D6A8),
      };

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final nombre = _c.historial?.nombrePaciente ?? (_c.cargando ? 'Cargando…' : 'Paciente ${widget.pacienteId}');
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TarjetaBlanca(
              child: Wrap(
                alignment: WrapAlignment.spaceBetween,
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 12,
                runSpacing: 12,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(nombre,
                          key: const Key('historial_nombre'),
                          style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
                      Ayuda(_subtitulo),
                    ],
                  ),
                  BuscadorPaciente(servicio: widget.servicio),
                ],
              ),
            ),
            const SizedBox(height: 16),
            if (_c.error != null)
              TarjetaBlanca(
                child: TextoError(
                    '${_c.sinPermiso ? 'No puede ver este historial' : 'No se pudo cargar el historial'}. ${_c.error}'),
              )
            else if (_c.historial != null) ...[
              FilaOpciones<FiltroHistorial>(
                opciones: [for (final f in FiltroHistorial.values) (f, f.texto)],
                elegida: _c.filtro,
                alElegir: _c.filtrar,
              ),
              const SizedBox(height: 14),
              if (_c.filtrados.isEmpty)
                const TarjetaBlanca(child: Text('Sin registros para este filtro.'))
              else
                for (final e in _c.filtrados) _evento(e),
            ],
          ],
        );
      },
    );
  }

  Widget _evento(EventoHistorial e) {
    final detalle = Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(color: ColoresCns.blanco, borderRadius: BorderRadius.circular(16)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 6,
            crossAxisAlignment: WrapCrossAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(color: _colorTipo(e.tipo), borderRadius: BorderRadius.circular(20)),
                child: Text(e.tipo.etiqueta, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w800)),
              ),
              Text(e.titulo, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800)),
              if (e.estado != null) ChipEstado(e.estado!),
            ],
          ),
          const SizedBox(height: 6),
          for (final l in e.lineas)
            Padding(padding: const EdgeInsets.only(top: 3), child: Text(l, style: const TextStyle(fontSize: 13))),
        ],
      ),
    );
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: LayoutBuilder(builder: (context, r) {
        final cuando = Text(fechaMesIso(e.fecha), style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800));
        if (r.maxWidth < 640) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [cuando, const SizedBox(height: 4), detalle],
          );
        }
        return Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(width: 110, child: Padding(padding: const EdgeInsets.only(top: 4), child: cuando)),
            const SizedBox(width: 14),
            Expanded(child: detalle),
          ],
        );
      }),
    );
  }
}
