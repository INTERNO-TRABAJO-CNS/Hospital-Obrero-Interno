import 'package:flutter/material.dart';

import '../../controllers/cirugias_controller.dart';
import '../../core/app_theme.dart';
import '../../core/rutas_medico.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/medico/cirugia.dart';
import '../../models/medico/ficha_interna.dart';
import '../../models/medico/medico_actual.dart';
import '../../services/cirugias_service.dart';
import '../../services/fichas_internas_service.dart';
import '../comun/cns_widgets.dart';
import 'medico_widgets.dart';
import 'ticket_medico_dialog.dart';

const int _abre = 7 * 60;
const int _cierra = 19 * 60;
const Color _colorCirugia = Color(0xFF8FC98B);
const Color _colorUrgente = Color(0xFFF0A8A0);

/// Posición (0..1) de una hora en la línea 07:00-19:00.
double _x(String hora) => ((minutosDe(hora).clamp(_abre, _cierra) - _abre) / (_cierra - _abre)).toDouble();

/// Figma "WEB ESPECIALISTA CIRUGIAS" (CU-24): ocupación de los quirófanos y mis cirugías del día.
class CirugiasView extends StatefulWidget {
  const CirugiasView({super.key, required this.medico, required this.servicio, this.reloj});

  final MedicoActual medico;
  final CirugiasService servicio;
  final DateTime Function()? reloj;

  @override
  State<CirugiasView> createState() => _CirugiasViewState();
}

class _CirugiasViewState extends State<CirugiasView> {
  late final CirugiasController _c = CirugiasController(widget.medico, widget.servicio, reloj: widget.reloj);

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

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilaFecha(
              resumen: '${fechaTituloIso(_c.fecha)} · Quirófanos de ${widget.medico.especialidad.nombre}',
              fecha: _c.fecha,
              alCambiar: _c.cambiarFecha,
              alHoy: () => _c.cambiarFecha(_c.hoy),
            ),
            const SizedBox(height: 14),
            if (_c.error != null) TextoError(_c.error!),
            TarjetaBlanca(child: _lineaDeTiempo()),
            const SizedBox(height: 16),
            Subtitulo('Mis cirugías ${_c.esHoy ? 'de hoy' : 'del día'}'),
            TablaCns(
              anchoMinimo: 860,
              textoVacio: 'No tienes cirugías este día.',
              columnas: const [
                ColumnaTabla('Hora', flex: 2),
                ColumnaTabla('Paciente', flex: 2),
                ColumnaTabla('Quirófano'),
                ColumnaTabla('Duración'),
                ColumnaTabla('Estado', flex: 2),
                ColumnaTabla('Acciones', flex: 4),
              ],
              filas: [for (final c in _c.mias) _fila(c)],
            ),
            const NotaPie('Solo ves los quirófanos de tu especialidad (no se comparten). «No se presentó» deja la '
                'cirugía CANCELADA y el horario no se reasigna.'),
          ],
        );
      },
    );
  }

  List<Widget> _fila(Cirugia c) {
    final cancelada = c.estado == EstadoCirugia.cancelada;
    final estilo = TextStyle(decoration: cancelada ? TextDecoration.lineThrough : null);
    return [
      Text('${c.inicio} – ${c.fin}', style: estilo),
      Text(c.nombrePaciente, style: estilo),
      Text(c.quirofano),
      Text('${c.duracionMin} min'),
      Wrap(spacing: 4, runSpacing: 4, children: [
        ChipEstado(c.estado.codigo),
        if (c.urgente) const ChipEstado('URGENCIA'),
      ]),
      if (c.estado == EstadoCirugia.programada)
        Wrap(spacing: 6, runSpacing: 6, children: [
          FilledButton(
            key: Key('realizada_${c.numero}'),
            style: FilledButton.styleFrom(minimumSize: const Size(0, 32)),
            onPressed: () => _c.marcar(c, EstadoCirugia.realizada),
            child: const Text('Realizada'),
          ),
          BotonPeligro(texto: 'No se presentó', alTocar: () => _c.marcar(c, EstadoCirugia.cancelada)),
          OutlinedButton(
            style: OutlinedButton.styleFrom(minimumSize: const Size(0, 32)),
            onPressed: () => mostrarTicketMedico(context, TicketMedico.deCirugia(c)),
            child: const Text('Ticket'),
          ),
        ])
      else
        const SizedBox.shrink(),
    ];
  }

  Widget _lineaDeTiempo() {
    const anchoNombre = 110.0;
    final horas = [for (var h = 7; h <= 19; h++) '${h.toString().padLeft(2, '0')}:00'];
    final ahora = _c.ahora;
    final mostrarAhora = _c.esHoy && minutosDe(ahora) >= _abre && minutosDe(ahora) <= _cierra;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Wrap(
          alignment: WrapAlignment.spaceBetween,
          spacing: 12,
          runSpacing: 6,
          children: [
            Text('Ocupación del día', style: TextStyle(fontSize: 15, fontWeight: FontWeight.w800)),
            Wrap(spacing: 12, children: [
              _Muestra(_colorCirugia, 'Cirugía'),
              _Muestra(_colorUrgente, 'Urgente'),
              _Muestra(ColoresCns.limpieza, 'Limpieza (30–45 min)'),
            ]),
          ],
        ),
        const SizedBox(height: 12),
        LayoutBuilder(builder: (context, r) {
          // En pantallas angostas la línea mide 760 px y se desplaza a los costados.
          final total = r.maxWidth.isFinite && r.maxWidth > 760 ? r.maxWidth : 760.0;
          final ancho = total - anchoNombre;
          return SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: SizedBox(
              width: anchoNombre + ancho,
              child: Stack(
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      SizedBox(
                        height: 18,
                        child: Stack(children: [
                          for (final h in horas)
                            Positioned(
                              left: anchoNombre + _x(h) * ancho - 14,
                              child: Text(h, style: const TextStyle(fontSize: 10, color: ColoresCns.textoSuave)),
                            ),
                        ]),
                      ),
                      for (final q in _c.quirofanos)
                        Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: Row(children: [
                            SizedBox(
                              width: anchoNombre,
                              child: Text('Quirófano $q',
                                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800)),
                            ),
                            SizedBox(
                              width: ancho,
                              height: 44,
                              child: DecoratedBox(
                                decoration:
                                    BoxDecoration(color: ColoresCns.filaPar, borderRadius: BorderRadius.circular(6)),
                                child: Stack(children: [
                                  for (final c in _c.cirugias)
                                    if (c.quirofano == q && c.estado != EstadoCirugia.cancelada) ...[
                                      _bloque(c.inicio, c.fin, ancho,
                                          color: c.urgente ? _colorUrgente : _colorCirugia,
                                          titulo: c.nombrePaciente,
                                          detalle: '${c.urgente ? 'URGENTE · ' : ''}${c.inicio} – ${c.fin}'),
                                      _bloque(c.fin, c.limpiezaHasta, ancho, color: ColoresCns.limpieza),
                                    ],
                                ]),
                              ),
                            ),
                          ]),
                        ),
                    ],
                  ),
                  if (mostrarAhora)
                    Positioned(
                      left: anchoNombre + _x(ahora) * ancho,
                      top: 0,
                      bottom: 0,
                      child: Container(width: 2, color: ColoresCns.peligro),
                    ),
                ],
              ),
            ),
          );
        }),
        if (mostrarAhora)
          Padding(
            padding: const EdgeInsets.only(top: 6),
            child: Text('Línea roja: ahora $ahora', style: const TextStyle(fontSize: 11, color: ColoresCns.peligro)),
          ),
      ],
    );
  }

  Widget _bloque(String inicio, String fin, double ancho, {required Color color, String? titulo, String? detalle}) {
    final izquierda = _x(inicio) * ancho;
    final largo = ((_x(fin) - _x(inicio)) * ancho).clamp(2.0, ancho);
    return Positioned(
      left: izquierda,
      top: 2,
      bottom: 2,
      width: largo,
      child: Tooltip(
        message: titulo == null ? 'Limpieza hasta $fin' : '$titulo $inicio–$fin',
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 4),
          decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(4)),
          child: titulo == null
              ? null
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(titulo,
                        maxLines: 1,
                        overflow: TextOverflow.clip,
                        style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w800)),
                    Text(detalle ?? '', maxLines: 1, overflow: TextOverflow.clip, style: const TextStyle(fontSize: 9)),
                  ],
                ),
        ),
      ),
    );
  }
}

class _Muestra extends StatelessWidget {
  const _Muestra(this.color, this.texto);

  final Color color;
  final String texto;

  @override
  Widget build(BuildContext context) {
    return Row(mainAxisSize: MainAxisSize.min, children: [
      Container(width: 12, height: 12, decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(3))),
      const SizedBox(width: 5),
      Text(texto, style: const TextStyle(fontSize: 11)),
    ]);
  }
}

/// Figma "WEB ESPECIALISTA PROGRAMAR CIRUGIA" (CU-24): proponer, confirmar o ver el choque.
class ProgramarCirugiaView extends StatefulWidget {
  const ProgramarCirugiaView({
    super.key,
    required this.medico,
    required this.cirugias,
    required this.fichas,
    required this.fichaTicket,
    this.reloj,
  });

  final MedicoActual medico;
  final CirugiasService cirugias;
  final FichasInternasService fichas;
  final int? fichaTicket;
  final DateTime Function()? reloj;

  @override
  State<ProgramarCirugiaView> createState() => _ProgramarCirugiaViewState();
}

class _ProgramarCirugiaViewState extends State<ProgramarCirugiaView> {
  late final ProgramarCirugiaController _c = ProgramarCirugiaController(widget.medico, widget.cirugias, widget.fichas);
  late final TextEditingController _duracion = TextEditingController(text: _c.duracion?.toString() ?? '');

  @override
  void initState() {
    super.initState();
    _c.cargar(widget.fichaTicket);
  }

  @override
  void dispose() {
    _c.dispose();
    _duracion.dispose();
    super.dispose();
  }

  String get _hoy => hoyIso((widget.reloj ?? DateTime.now)());

  String _inicioTexto(PropuestaCirugia p) {
    final dia = fechaLargaIso(p.fecha).replaceAll(RegExp(r' de \d{4}$'), '');
    return p.fecha == _hoy ? 'Hoy, $dia · ${p.inicio}' : '${capitalizada(dia)} · ${p.inicio}';
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final ficha = _c.ficha;
        if (!_c.cargada) {
          return const Cargando();
        }
        if (ficha == null) {
          return const TarjetaBlanca(child: Text('Elige una ficha ATENDIDA desde la agenda de fichas internas.'));
        }
        final Widget? lateral = _c.confirmada != null
            ? _confirmada(_c.confirmada!)
            : _c.rechazo != null
                ? _rechazo()
                : _c.propuesta != null
                    ? _propuesta(_c.propuesta!)
                    : null;
        return Columnas(principal: _datos(ficha), lateral: lateral);
      },
    );
  }

  Widget _datos(FichaInterna ficha) {
    final esp = widget.medico.especialidad;
    final bloqueado = _c.confirmada != null;
    final cuando = ficha.fecha == _hoy ? 'hoy' : (ficha.fecha == null ? '' : fechaMesIso(ficha.fecha!, conAnio: false));
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const TituloTarjeta('Datos de la cirugía', grande: true),
          DatoFila('Paciente', '${ficha.nombrePaciente} · ${ficha.edad} años'),
          DatoFila('Ficha interna', 'N° ${ficha.ticket} · ${ficha.estado.codigo} $cuando ${ficha.hora ?? ''}'),
          DatoFila('Cirujano', '${widget.medico.nombre} (tú)'),
          const SizedBox(height: 8),
          CampoCns(
            etiqueta: 'Duración estimada (minutos)',
            ayuda: esp.operaEnQuirofano
                ? '${esp.nombre}: de ${esp.cirugiaMin} a ${esp.cirugiaMax} min, según qué tan problemático es el caso.'
                : null,
            child: TextField(
              key: const Key('duracion'),
              controller: _duracion,
              enabled: !bloqueado,
              keyboardType: TextInputType.number,
              onChanged: _c.cambiarDuracion,
            ),
          ),
          CampoCns(
            etiqueta: 'Gravedad',
            child: SelectorGravedad(valor: _c.gravedad, deshabilitado: bloqueado, alCambiar: _c.elegirGravedad),
          ),
          Casilla(
            valor: _c.urgente,
            texto: 'Urgente (se busca desde ahora, también fuera de horario)',
            deshabilitada: bloqueado,
            alCambiar: _c.cambiarUrgente,
          ),
          if (!_c.choque && !bloqueado) ...[
            const SizedBox(height: 10),
            FilledButton(
              key: const Key('proponer'),
              onPressed: _c.trabajando || _c.duracion == null ? null : _c.proponer,
              child: const Text('Proponer horario'),
            ),
            const Ayuda('Proponer no guarda nada: solo calcula el primer hueco sin choques.'),
          ],
        ],
      ),
    );
  }

  Widget _confirmada(Cirugia c) {
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Row(children: [
            Expanded(child: TituloTarjeta('Cirugía programada', grande: true)),
            ChipEstado('PROGRAMADA'),
          ]),
          Text('Cirugía N° ${c.numero} · Quirófano ${c.quirofano}',
              key: const Key('cirugia_confirmada'),
              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800, color: ColoresCns.menu)),
          const SizedBox(height: 6),
          DatoFila('Inicio', '${capitalizada(fechaLargaIso(c.fecha))} · ${c.inicio}'),
          DatoFila('Fin', c.fin),
          DatoFila('Limpieza del quirófano', 'hasta ${c.limpiezaHasta}'),
          Botones([
            FilledButton(
              onPressed: () => mostrarTicketMedico(context, TicketMedico.deCirugia(c)),
              child: const Text('Imprimir ticket'),
            ),
            OutlinedButton(
                onPressed: () => volverA(context, RutasMedico.cirugias), child: const Text('Ver cirugías del día')),
          ]),
          const Ayuda('El paciente recibe «Cirugía programada» en la app.'),
        ],
      ),
    );
  }

  Widget _rechazo() {
    final (codigo, mensaje) = _c.rechazo!;
    final p = _c.propuesta;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CajaError(codigo: codigo, mensaje: mensaje),
          if (_c.choque && p != null) ...[
            const SizedBox(height: 12),
            const Text('Propuesta anterior (ya no disponible)',
                style: TextStyle(fontSize: 14, fontWeight: FontWeight.w800, color: ColoresCns.textoSuave)),
            DatoFila('Quirófano', '${p.quirofano} · ${p.especialidad.nombre}'),
            DatoFila('Inicio', _inicioTexto(p)),
            DatoFila('Fin y limpieza', '${p.fin} · limpieza hasta ${p.limpiezaHasta}'),
          ],
          Botones([
            const FilledButton(onPressed: null, child: Text('Confirmar')),
            FilledButton(onPressed: _c.trabajando ? null : _c.proponer, child: const Text('Proponer de nuevo')),
          ]),
          const SizedBox(height: 12),
          const Text('Otros rechazos posibles', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w800)),
          const SizedBox(height: 6),
          for (final (c, descripcion) in rechazosCirugia)
            if (c != codigo)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text(c, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800)),
                  Text(descripcion, style: const TextStyle(fontSize: 12)),
                ]),
              ),
        ],
      ),
    );
  }

  Widget _propuesta(PropuestaCirugia p) {
    final total = minutosDe(p.limpiezaHasta) - minutosDe(p.inicio);
    final parte = total <= 0 ? 1.0 : (minutosDe(p.fin) - minutosDe(p.inicio)) / total;
    final d = p.desplaza;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(children: [
            const Expanded(child: TituloTarjeta('Propuesta del sistema', grande: true)),
            ChipEstado(d != null ? 'MEDIA' : 'GENERAL', texto: d != null ? 'DESPLAZA 1' : 'NO DESPLAZA'),
          ]),
          Text('Quirófano ${p.quirofano} · ${p.especialidad.nombre}',
              key: const Key('propuesta_quirofano'),
              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800, color: ColoresCns.menu)),
          const SizedBox(height: 6),
          DatoFila('Inicio', _inicioTexto(p)),
          DatoFila('Fin', p.fin),
          DatoFila('Limpieza del quirófano', 'hasta ${p.limpiezaHasta} (${p.limpiezaMin} min)'),
          const DatoFila('Choques', 'Ninguno: quirófano, cirujano y paciente libres'),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: SizedBox(
              height: 18,
              child: Row(children: [
                Expanded(
                    flex: (parte * 100).round(), child: Container(color: _c.urgente ? _colorUrgente : _colorCirugia)),
                Expanded(flex: 100 - (parte * 100).round(), child: Container(color: ColoresCns.limpieza)),
              ]),
            ),
          ),
          Ayuda('${p.inicio} – ${p.fin} ${_c.urgente ? 'urgente' : 'cirugía'} · limpieza hasta ${p.limpiezaHasta}'),
          if (d != null)
            Container(
              margin: const EdgeInsets.only(top: 8),
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(color: const Color(0xFFFDF0D8), borderRadius: BorderRadius.circular(10)),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                const Text('Esta cirugía desplaza a otra', style: TextStyle(fontWeight: FontWeight.w800)),
                const SizedBox(height: 4),
                Text(
                  'Cirugía N° ${d.cirugia.numero} · ${d.cirugia.nombrePaciente} · ${d.cirugia.inicio} – ${d.cirugia.fin} · '
                  'no urgente y todavía no empieza. Al confirmar se reprograma al siguiente hueco libre '
                  '(${fechaLargaIso(d.nuevaFecha).replaceAll(RegExp(r' de \d{4}$'), '')} · ${d.nuevoInicio}) y se le '
                  'avisa «Cirugía reprogramada».',
                  style: const TextStyle(fontSize: 12),
                ),
              ]),
            ),
          Botones([
            FilledButton(
                key: const Key('confirmar_cirugia'),
                onPressed: _c.trabajando ? null : _c.confirmar,
                child: const Text('Confirmar')),
            OutlinedButton(onPressed: _c.trabajando ? null : _c.proponer, child: const Text('Proponer de nuevo')),
          ]),
          const Ayuda('Se guarda solo al confirmar. El paciente recibe «Cirugía programada» en la app y se puede '
              'imprimir el ticket.'),
        ],
      ),
    );
  }
}
