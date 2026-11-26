import 'package:flutter/material.dart';

import '../../controllers/mis_citas_controller.dart';
import '../../core/app_theme.dart';
import '../../models/citas/cita_paciente.dart';
import '../../models/comun/fechas.dart';
import '../../models/solicitud/solicitud.dart';
import '../../services/cancelacion_service.dart';
import '../../services/citas_paciente_service.dart';
import '../../services/solicitud_service.dart';
import '../comun/cns_widgets.dart';
import 'detalle_cita_view.dart';

/// Desde este ancho "Mis citas" se ve como en el Figma web: tabla + detalle a la derecha.
const double anchoMisCitasEscritorio = 900;

/// CU-08/CU-10 (Figma "MIS CITAS" celular y "WEB PACIENTE MIS CITAS"): solo las citas del paciente,
/// con su estado; al tocar una se ve el detalle y, si esta PROGRAMADA, se puede reprogramar o cancelar.
class MisCitasView extends StatefulWidget {
  const MisCitasView({
    super.key,
    required this.citas,
    required this.cancelacion,
    this.solicitudes,
    this.reloj,
  });

  final CitasPacienteService citas;
  final CancelacionService cancelacion;

  /// Para mostrar las solicitudes que siguen buscando cupo (opcional).
  final SolicitudService? solicitudes;
  final DateTime Function()? reloj;

  @override
  State<MisCitasView> createState() => _MisCitasViewState();
}

class _MisCitasViewState extends State<MisCitasView> {
  late final MisCitasController _c =
      MisCitasController(widget.citas, solicitudes: widget.solicitudes, reloj: widget.reloj);
  bool _trabajando = false;

  @override
  void initState() {
    super.initState();
    _c.cargar();
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  Future<void> _abrirDetalle(CitaPaciente cita) async {
    final cambio = await Navigator.of(context).push<bool>(MaterialPageRoute(
      builder: (_) => DetalleCitaView(cita: cita, reprogramar: _c.reprogramar, cancelacion: widget.cancelacion),
    ));
    if (cambio == true && mounted) {
      await _c.cargar();
    }
  }

  Future<void> _reprogramarEnPanel(CitaPaciente cita) async {
    setState(() => _trabajando = true);
    await reprogramarDesdeDetalle(context, cita, _c.reprogramar);
    if (mounted) {
      setState(() => _trabajando = false);
    }
  }

  Future<void> _cancelarEnPanel(CitaPaciente cita) async {
    if (await cancelarDesdeDetalle(context, cita, widget.cancelacion) && mounted) {
      mostrarAviso(context, 'Cita cancelada');
      await _c.cargar();
    }
  }

  @override
  Widget build(BuildContext context) {
    // El Figma no tiene modo oscuro: fondo verde y tarjetas blancas siempre con el tema claro.
    return Theme(
      data: AppTheme.claro(),
      child: Builder(builder: _contenido),
    );
  }

  Widget _contenido(BuildContext context) {
    return Scaffold(
      backgroundColor: ColoresCns.fondo,
      body: SafeArea(
        child: ListenableBuilder(
          listenable: _c,
          builder: (context, _) => LayoutBuilder(
            builder: (context, r) => r.maxWidth >= anchoMisCitasEscritorio ? _escritorio() : _celular(),
          ),
        ),
      ),
    );
  }

  // ---------------------------------------------------------------- celular

  Widget _celular() {
    final volver = Navigator.of(context).canPop() ? () => Navigator.of(context).maybePop() : null;
    return RefreshIndicator(
      onRefresh: _c.cargar,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(18, 14, 18, 24),
        children: [
          EncabezadoPaciente(titulo: 'Mis citas', alVolver: volver),
          const SizedBox(height: 18),
          ..._estadoCarga(),
          for (final s in _c.enCurso) ...[_TarjetaSolicitud(s), const SizedBox(height: 12)],
          for (final c in _c.citas) ...[
            _TarjetaCita(cita: c, alTocar: () => _abrirDetalle(c)),
            const SizedBox(height: 12),
          ],
        ],
      ),
    );
  }

  /// Cargando, error o [2a] sin citas.
  List<Widget> _estadoCarga() {
    if (_c.cargando && _c.citas.isEmpty) {
      return const [Cargando()];
    }
    if (_c.error != null && _c.citas.isEmpty) {
      return [
        TarjetaBlanca(
          child: Column(
            children: [
              Text(_c.error!, textAlign: TextAlign.center, style: const TextStyle(color: ColoresCns.peligro)),
              const SizedBox(height: 12),
              FilledButton(onPressed: _c.cargar, child: const Text('Reintentar')),
            ],
          ),
        ),
      ];
    }
    if (_c.citas.isEmpty && _c.enCurso.isEmpty) {
      return const [
        TarjetaBlanca(
          key: Key('mis_citas_vacio'),
          child: Text('No tienes citas registradas todavía. Solicita una desde «Solicitar cita».',
              textAlign: TextAlign.center),
        ),
      ];
    }
    return const [];
  }

  // ------------------------------------------------------------- escritorio

  Widget _escritorio() {
    final visibles = _c.visibles;
    final elegida = _c.elegida;
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(28, 22, 28, 28),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              if (Navigator.of(context).canPop())
                IconButton(
                  tooltip: 'Volver',
                  onPressed: () => Navigator.of(context).maybePop(),
                  icon: const Icon(Icons.arrow_back),
                ),
              const Expanded(
                child: Text('Mis citas', style: TextStyle(fontSize: 26, fontWeight: FontWeight.w800)),
              ),
              IconButton(tooltip: 'Actualizar', onPressed: _c.cargar, icon: const Icon(Icons.refresh)),
            ],
          ),
          const SizedBox(height: 12),
          FilaOpciones<FiltroCitas>(
            opciones: [for (final f in FiltroCitas.values) (f, f.titulo)],
            elegida: _c.filtro,
            alElegir: _c.cambiarFiltro,
          ),
          const SizedBox(height: 14),
          if (_c.enCurso.isNotEmpty) ...[
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [for (final s in _c.enCurso) SizedBox(width: 340, child: _TarjetaSolicitud(s))],
            ),
            const SizedBox(height: 14),
          ],
          if (_c.error != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 10),
              child: Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro)),
            ),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                flex: 5,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    TablaCns(
                      columnas: const [
                        ColumnaTabla('Ficha'),
                        ColumnaTabla('Fecha'),
                        ColumnaTabla('Hora'),
                        ColumnaTabla('Paciente', flex: 2),
                        ColumnaTabla('Especialidad', flex: 2),
                        ColumnaTabla('Médico', flex: 2),
                        ColumnaTabla('Estado', flex: 2),
                      ],
                      filas: [
                        for (final c in visibles)
                          [
                            Text('N° ${c.numeroFicha}',
                                style: TextStyle(
                                    fontWeight: c.citaId == elegida?.citaId ? FontWeight.w800 : FontWeight.normal)),
                            Text(_fechaCorta(c.inicio)),
                            Text(horaTexto(c.inicio)),
                            Text(c.paciente.isEmpty ? '—' : c.paciente),
                            Text(c.especialidad.nombre),
                            Text(c.medico),
                            ChipEstado(codigoEstado(c)),
                          ],
                      ],
                      seleccionada: elegida == null ? null : visibles.indexOf(elegida),
                      alTocarFila: (i) => _c.elegir(visibles[i]),
                      textoVacio: _c.cargando ? 'Cargando…' : 'No hay citas en esta pestaña',
                      anchoMinimo: 680,
                    ),
                    const NotaPie('Solo se muestran tus citas y las de tus beneficiarios. '
                        'Toca una fila para ver el detalle.'),
                  ],
                ),
              ),
              const SizedBox(width: 18),
              SizedBox(
                width: 320,
                child: elegida == null
                    ? const TarjetaBlanca(child: Text('Elige una cita para ver el detalle.'))
                    : Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          PanelDetalleCita(
                            cita: elegida,
                            conEncabezado: true,
                            trabajando: _trabajando,
                            alReprogramar: () => _reprogramarEnPanel(elegida),
                            alCancelar: () => _cancelarEnPanel(elegida),
                          ),
                          if (elegida.sePuedeModificar) ...[
                            const SizedBox(height: 12),
                            const TarjetaBlanca(
                              padding: EdgeInsets.all(14),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('Al cancelar o reprogramar',
                                      style: TextStyle(
                                          color: ColoresCns.peligro, fontWeight: FontWeight.w800, fontSize: 12)),
                                  SizedBox(height: 4),
                                  Text(
                                    'Se pide confirmar en un diálogo y el cupo vuelve a quedar libre para otro '
                                    'paciente. Reprogramar crea una nueva solicitud para el siguiente día hábil.',
                                    style: TextStyle(fontSize: 11.5),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ],
                      ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// "Jue 12 nov".
String _fechaCorta(DateTime f) {
  const dias = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  return '${dias[f.weekday - 1]} ${diaMesCorto(f)}';
}

/// "Jueves 12 nov · 09:30".
String _fechaHoraTarjeta(DateTime f) {
  const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
  return '${dias[f.weekday - 1]} ${diaMesCorto(f)} · ${horaTexto(f)}';
}

/// Tarjeta de una cita en la lista del celular (Figma "MIS CITAS").
class _TarjetaCita extends StatelessWidget {
  const _TarjetaCita({required this.cita, required this.alTocar});

  final CitaPaciente cita;
  final VoidCallback alTocar;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: ColoresCns.blanco,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        key: Key('cita_${cita.citaId}'),
        borderRadius: BorderRadius.circular(16),
        onTap: alTocar,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text('Ficha N° ${cita.numeroFicha}',
                        style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800)),
                  ),
                  ChipEstado(codigoEstado(cita)),
                ],
              ),
              const SizedBox(height: 6),
              Text(_fechaHoraTarjeta(cita.inicio), style: const TextStyle(fontWeight: FontWeight.w800)),
              const SizedBox(height: 4),
              Text('${cita.especialidad.nombre} · ${cita.medico}',
                  style: const TextStyle(color: ColoresCns.textoSuave, fontSize: 13)),
              if (cita.paciente.isNotEmpty) ...[
                const SizedBox(height: 2),
                Text('Paciente: ${cita.paciente}',
                    key: Key('cita_paciente_${cita.citaId}'),
                    style: const TextStyle(color: ColoresCns.textoSuave, fontSize: 12.5)),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// Solicitud que todavia busca cupo (por ejemplo, la creada al reprogramar).
class _TarjetaSolicitud extends StatelessWidget {
  const _TarjetaSolicitud(this.s);

  final ResultadoSolicitud s;

  @override
  Widget build(BuildContext context) {
    final (codigo, texto) = switch (s.estado) {
      EstadoSolicitudApp.pendiente => ('PENDIENTE', 'BUSCANDO CUPO'),
      EstadoSolicitudApp.enEspera => ('EN_ESPERA', 'EN ESPERA'),
      EstadoSolicitudApp.sinCupo => ('CANCELADA', 'SIN CUPO'),
      _ => (s.estado.name.toUpperCase(), s.estado.name.toUpperCase()),
    };
    return Container(
      key: Key('solicitud_${s.solicitudId}'),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: ColoresCns.blanco.withValues(alpha: .85),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: ColoresCns.amarillo, width: 2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text('Solicitud N° ${s.solicitudId} · ${s.especialidad.nombre}',
                    style: const TextStyle(fontWeight: FontWeight.w800)),
              ),
              ChipEstado(codigo, texto: texto),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            '${s.prioridad == 2 ? 'Reprogramada · ' : ''}Buscando cupo desde el ${fechaLarga(s.diaSolicitado)}',
            style: const TextStyle(fontSize: 12.5, color: ColoresCns.textoSuave),
          ),
        ],
      ),
    );
  }
}
