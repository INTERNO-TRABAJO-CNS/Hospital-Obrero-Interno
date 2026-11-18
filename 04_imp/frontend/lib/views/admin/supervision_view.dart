import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../controllers/supervision_controller.dart';
import '../../core/app_theme.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/catalogo/horario.dart';
import '../../models/comun/fechas.dart';
import '../../models/supervision/supervision.dart';
import '../../services/supervision_service.dart';
import '../comun/cns_widgets.dart';

/// Figma "ADMIN SUPERVISION TIEMPO REAL v2", "... FICHAS INTERNAS" y "... QUIROFANOS".
/// El administrador solo observa.
class SupervisionView extends StatefulWidget {
  const SupervisionView({super.key, required this.servicio, this.intervalo = const Duration(seconds: 15)});

  final SupervisionService servicio;
  final Duration intervalo;

  @override
  State<SupervisionView> createState() => _SupervisionViewState();
}

class _SupervisionViewState extends State<SupervisionView> {
  late final SupervisionController _c = SupervisionController(widget.servicio, intervalo: widget.intervalo);

  @override
  void initState() {
    super.initState();
    _c.iniciar();
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
      builder: (context, _) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          FilaOpciones<PestanaSupervision>(
            opciones: [for (final p in PestanaSupervision.values) (p, p.titulo)],
            elegida: _c.pestana,
            alElegir: _c.elegirPestana,
            estilo: EstiloOpciones.verde,
          ),
          const SizedBox(height: 14),
          if (_c.error != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro)),
            ),
          switch (_c.pestana) {
            PestanaSupervision.citas => _citas(),
            PestanaSupervision.internas => _internas(),
            PestanaSupervision.quirofanos => _quirofanos(),
          },
          const AvisoDatosPrueba(),
        ],
      ),
    );
  }

  Widget _citas() {
    final r = _c.citas;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Wrap(
          spacing: 8,
          runSpacing: 8,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            const PildoraEnVivo(),
            FilaOpciones<Especialidad?>(
              opciones: [(null, 'Todas'), for (final e in Especialidad.basicas) (e, e.nombre)],
              elegida: _c.basica,
              alElegir: _c.elegirBasica,
            ),
          ],
        ),
        const SizedBox(height: 14),
        if (r == null)
          const Cargando()
        else ...[
          FilaIndicadores(indicadores: [
            TarjetaIndicador(valor: '${r.fichasHoy}', etiqueta: 'Fichas hoy'),
            TarjetaIndicador(valor: '${r.atendidas}', etiqueta: 'Atendidas'),
            TarjetaIndicador(valor: '${r.confirmadas}', etiqueta: 'Confirmadas'),
            TarjetaIndicador(valor: '${r.canceladas}', etiqueta: 'Canceladas'),
          ]),
          const SizedBox(height: 18),
          const Subtitulo('Agenda por médico (solo lectura)'),
          TablaCns(
            columnas: const [
              ColumnaTabla('Médico', flex: 2),
              ColumnaTabla('Especialidad / ficha', flex: 3),
              ColumnaTabla('Fichas / paciente', flex: 2),
              ColumnaTabla('Horario / cons.', flex: 2),
              ColumnaTabla('Estado', flex: 4),
            ],
            filas: [
              for (final f in r.filas)
                [
                  Text(f.medico),
                  Text(f.especialidadOFicha),
                  Text(f.fichasOPaciente),
                  Text(f.horarioOConsultorio),
                  ChipEstado(f.estado, anchoCompleto: true),
                ],
            ],
            textoVacio: 'No hay fichas hoy para esta especialidad',
          ),
          const NotaPie('El administrador solo observa: no puede modificar citas desde esta vista.'),
        ],
      ],
    );
  }

  Widget _internas() {
    final r = _c.internas;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        FilaOpciones<Especialidad>(
          opciones: [for (final e in Especialidad.internas) (e, e.nombre)],
          elegida: _c.interna,
          alElegir: _c.elegirInterna,
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 10,
          runSpacing: 8,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            const PildoraEnVivo(),
            _SelectorTabla(elegida: _c.tabla, alElegir: _c.elegirTabla),
            if (r != null)
              Text('${_c.interna.nombre} · ${_c.tabla.nombre} · ${fechaLarga(r.fecha)}',
                  style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w800)),
          ],
        ),
        const SizedBox(height: 14),
        if (r == null)
          const Cargando()
        else ...[
          FilaIndicadores(indicadores: [
            TarjetaIndicador(valor: '${r.contar(EstadoFichaInterna.asignada)}', etiqueta: 'Asignadas'),
            TarjetaIndicador(valor: '${r.contar(EstadoFichaInterna.enEspera)}', etiqueta: 'En espera'),
            TarjetaIndicador(valor: '${r.contar(EstadoFichaInterna.atendida)}', etiqueta: 'Atendidas'),
            TarjetaIndicador(valor: '${r.contar(EstadoFichaInterna.cancelada)}', etiqueta: 'No se presentó'),
            TarjetaIndicador(valor: '${r.contar(EstadoFichaInterna.desplazada)}', etiqueta: 'Desplazadas'),
          ]),
          const SizedBox(height: 16),
          TablaCns(
            columnas: const [
              ColumnaTabla('Hora'),
              ColumnaTabla('Paciente', flex: 2),
              ColumnaTabla('Especialista', flex: 2),
              ColumnaTabla('Urgencia', flex: 2),
              ColumnaTabla('Derivado por', flex: 2),
              ColumnaTabla('Estado', flex: 4),
            ],
            filas: [
              for (final f in r.fichas)
                [
                  Text(f.hora ?? '—'),
                  Text(f.paciente),
                  Text(f.especialista ?? '—'),
                  f.urgencia ? const ChipEstado('URGENCIA') : const Text('—'),
                  Text(f.derivadoPor),
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      ChipEstado(f.estado.codigo),
                      if (f.detalle != null) ...[
                        const SizedBox(width: 6),
                        Flexible(
                          child: Text(f.detalle!,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
                        ),
                      ],
                    ],
                  ),
                ],
            ],
            textoVacio: 'No hay fichas internas en esta tabla',
            anchoMinimo: 720,
          ),
          const NotaPie('Solo lectura. Cada tabla (especialidad × General/Pediatría) se revisa por separado, sin '
              'mezclarse con las citas comunes. El administrador no ve la gravedad.'),
        ],
      ],
    );
  }

  Widget _quirofanos() {
    final r = _c.quirofanos;
    if (r == null) {
      return const Cargando();
    }
    String horas(double h) => h == h.roundToDouble() ? h.toStringAsFixed(0) : h.toStringAsFixed(1);
    final dia = fechaLarga(r.fecha);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Wrap(
          spacing: 10,
          runSpacing: 8,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            const PildoraEnVivo(),
            Text('${dia[0].toUpperCase()}${dia.substring(1)} · todos los quirófanos',
                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w800)),
          ],
        ),
        const SizedBox(height: 14),
        FilaIndicadores(indicadores: [
          TarjetaIndicador(valor: '${r.cirugiasHoy}', etiqueta: 'Cirugías hoy'),
          TarjetaIndicador(
              valor: '${horas(r.horasUsadas)} h / ${horas(r.horasAbiertas)} h', etiqueta: 'Horas usadas / abiertas'),
          TarjetaIndicador(valor: '${r.urgentes}', etiqueta: 'Urgentes'),
          TarjetaIndicador(valor: '${r.desplazamientos}', etiqueta: 'Desplazamientos'),
        ]),
        const SizedBox(height: 16),
        TarjetaBlanca(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Wrap(
                alignment: WrapAlignment.spaceBetween,
                spacing: 12,
                runSpacing: 6,
                children: [
                  Text('Ocupación por quirófano', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w800)),
                  _Leyenda(),
                ],
              ),
              const SizedBox(height: 12),
              _LineaTiempo(quirofanos: r.quirofanos),
            ],
          ),
        ),
        const NotaPie('Solo lectura. Cero choques: cada quirófano respeta su limpieza (30–45 min) antes de la '
            'siguiente cirugía. Horas usadas incluye solo el tiempo de cirugía.'),
      ],
    );
  }
}

/// "Tabla General | Tabla Pediatría".
class _SelectorTabla extends StatelessWidget {
  const _SelectorTabla({required this.elegida, required this.alElegir});

  final TablaInterna elegida;
  final ValueChanged<TablaInterna> alElegir;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(2),
      decoration: BoxDecoration(
        color: ColoresCns.blanco,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: ColoresCns.menu),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          for (final t in TablaInterna.values)
            Material(
              color: t == elegida ? ColoresCns.amarillo : Colors.transparent,
              borderRadius: BorderRadius.circular(6),
              child: InkWell(
                borderRadius: BorderRadius.circular(6),
                onTap: () => alElegir(t),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  child: Text(t.titulo,
                      style: TextStyle(fontSize: 12, fontWeight: t == elegida ? FontWeight.w800 : FontWeight.w400)),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _Leyenda extends StatelessWidget {
  const _Leyenda();

  @override
  Widget build(BuildContext context) {
    Widget muestra(Color color, String texto) => Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
                width: 12, height: 12, decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(3))),
            const SizedBox(width: 5),
            Text(texto, style: const TextStyle(fontSize: 12)),
          ],
        );
    return Wrap(
      spacing: 14,
      children: [
        muestra(ColoresCns.amarillo, 'Cirugía'),
        muestra(ColoresCns.cancelada, 'Urgente'),
        muestra(ColoresCns.limpieza, 'Limpieza'),
      ],
    );
  }
}

/// Linea de tiempo de 07:00 a 19:00 con las cirugias y su limpieza.
class _LineaTiempo extends StatelessWidget {
  const _LineaTiempo({required this.quirofanos});

  final List<QuirofanoDelDia> quirofanos;

  static const int _abre = 7 * 60;
  static const int _cierra = 19 * 60;
  static const double _anchoNombre = 170;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, r) {
      final anchoPista = math.max(r.maxWidth - _anchoNombre, 480.0);
      double x(int minutos) => (minutos.clamp(_abre, _cierra) - _abre) / (_cierra - _abre) * anchoPista;
      final horas = [for (var h = 7; h <= 19; h++) h];

      final contenido = SizedBox(
        width: _anchoNombre + anchoPista,
        child: Column(
          children: [
            SizedBox(
              height: 18,
              child: Stack(
                clipBehavior: Clip.none,
                children: [
                  for (final h in horas)
                    Positioned(
                      left: _anchoNombre + x(h * 60) - 14,
                      child: Text('${h.toString().padLeft(2, '0')}:00',
                          style: const TextStyle(fontSize: 10, color: ColoresCns.textoSuave)),
                    ),
                ],
              ),
            ),
            for (final q in quirofanos)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 5),
                child: Row(
                  children: [
                    SizedBox(
                      width: _anchoNombre,
                      child: Text('${q.codigo} · ${q.especialidad.nombre}',
                          style: const TextStyle(fontSize: 11.5, fontWeight: FontWeight.w800)),
                    ),
                    SizedBox(
                      width: anchoPista,
                      height: 36,
                      child: Stack(
                        children: [
                          Positioned.fill(
                            child: DecoratedBox(
                              decoration:
                                  BoxDecoration(color: ColoresCns.filaPar, borderRadius: BorderRadius.circular(6)),
                            ),
                          ),
                          for (final h in horas.skip(1).take(horas.length - 2))
                            Positioned(
                              left: x(h * 60),
                              top: 0,
                              bottom: 0,
                              child: Container(width: 1, color: const Color(0x14000000)),
                            ),
                          for (final c in q.cirugias) ...[
                            _bloque(x(minutosDe(c.inicio)), x(minutosDe(c.fin)),
                                c.urgente ? ColoresCns.cancelada : ColoresCns.amarillo,
                                texto: c.paciente),
                            _bloque(x(minutosDe(c.fin)), x(minutosDe(c.fin) + q.limpiezaMin), ColoresCns.limpieza),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
      );
      if (r.maxWidth >= _anchoNombre + 480) {
        return contenido;
      }
      return SingleChildScrollView(scrollDirection: Axis.horizontal, child: contenido);
    });
  }

  Widget _bloque(double desde, double hasta, Color color, {String? texto}) {
    return Positioned(
      left: desde,
      width: math.max(hasta - desde, 2),
      top: 4,
      bottom: 4,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 4),
        alignment: Alignment.centerLeft,
        decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(4)),
        child: texto == null
            ? null
            : Text(texto,
                maxLines: 1,
                overflow: TextOverflow.clip,
                style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w800)),
      ),
    );
  }
}
