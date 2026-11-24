import 'dart:async';

import 'package:flutter/material.dart';

import '../../controllers/fichas_internas_controller.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/medico/ficha_interna.dart';
import '../../models/medico/medico_actual.dart';
import '../../services/fichas_internas_service.dart';
import '../comun/cns_widgets.dart';

/// Figma "WEB MEDICO COLA DE PRIORIDAD" (CU-25): solo lectura, se actualiza cada 15 s.
class ColaPrioridadView extends StatefulWidget {
  const ColaPrioridadView({super.key, required this.medico, required this.fichas, this.cadaCuanto});

  final MedicoActual medico;
  final FichasInternasService fichas;

  /// null = 15 s. Las pruebas lo apagan con [Duration.zero].
  final Duration? cadaCuanto;

  @override
  State<ColaPrioridadView> createState() => _ColaPrioridadViewState();
}

class _ColaPrioridadViewState extends State<ColaPrioridadView> {
  late final ColaPrioridadController _c = ColaPrioridadController(widget.medico, widget.fichas);
  Timer? _reloj;

  @override
  void initState() {
    super.initState();
    _c.cargar();
    final cada = widget.cadaCuanto ?? const Duration(seconds: 15);
    if (cada > Duration.zero) {
      _reloj = Timer.periodic(cada, (_) => _c.cargar());
    }
  }

  @override
  void dispose() {
    _reloj?.cancel();
    _c.dispose();
    super.dispose();
  }

  String _asignada(FichaInterna f) {
    if (f.fecha == null) {
      return '';
    }
    final dia = f.fecha == hoyIso() ? 'Hoy' : fechaMesIso(f.fecha!, conAnio: false);
    return '$dia ${f.hora} · ${f.medico}';
  }

  String _llegada(FichaInterna f) => '${fechaMesIso(isoDe(f.llegada), conAnio: false)} ${horaActual(f.llegada)}';

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final pediatrica = _c.tabla == TablaFicha.pediatria;
        final ultima = _c.ultimaMia;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilaOpciones<Especialidad>(
              opciones: [for (final e in Especialidad.internas) (e, e.nombre)],
              elegida: _c.especialidad,
              alElegir: _c.elegir,
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 10,
              runSpacing: 8,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                const PildoraEnVivo(texto: 'Se actualiza cada 15 s'),
                ChipEstado(_c.tabla.codigo, texto: pediatrica ? 'TABLA PEDIÁTRICA' : 'TABLA GENERAL'),
                Text(
                  pediatrica
                      ? 'Ves la tabla pediátrica porque derivas desde Pediatría. El médico general ve la tabla general.'
                      : 'Ves la tabla general porque derivas desde Consultorio Familiar. El pediatra ve la tabla '
                          'pediátrica.',
                  style: const TextStyle(fontSize: 12),
                ),
              ],
            ),
            const SizedBox(height: 12),
            TablaCns(
              anchoMinimo: 860,
              textoVacio: _c.cargando ? 'Cargando…' : 'No hay fichas en espera ni asignadas.',
              columnas: const [
                ColumnaTabla('Pos.'),
                ColumnaTabla('Paciente', flex: 3),
                ColumnaTabla('Urgencia', flex: 2),
                ColumnaTabla('Gravedad', flex: 2),
                ColumnaTabla('60 o más'),
                ColumnaTabla('Fecha / hora asignada', flex: 4),
                ColumnaTabla('Llegada', flex: 2),
              ],
              resaltadas: {
                for (var i = 0; i < _c.fichas.length; i++)
                  if (_c.fichas[i].ticket == ultima) i
              },
              filas: [
                for (var i = 0; i < _c.fichas.length; i++)
                  [
                    Text('${i + 1}', style: const TextStyle(fontWeight: FontWeight.w800)),
                    Text(_c.fichas[i].nombrePaciente),
                    _c.fichas[i].urgencia ? const ChipEstado('URGENCIA') : const Text('—'),
                    ChipEstado(_c.fichas[i].gravedad?.codigo ?? '—'),
                    Text(_c.fichas[i].edad >= edadAdultoMayor ? 'Sí' : 'No'),
                    _c.fichas[i].estado == EstadoFicha.enEspera
                        ? const Wrap(spacing: 6, children: [
                            ChipEstado('EN_ESPERA'),
                            Text('sin cupo en 15 días hábiles', style: TextStyle(fontSize: 11)),
                          ])
                        : Text(_asignada(_c.fichas[i])),
                    Text(_llegada(_c.fichas[i])),
                  ],
              ],
            ),
            const NotaPie('Solo lectura. Orden: urgencia › gravedad (ALTA › MEDIA › BAJA) › 60 años o más › orden de '
                'llegada. Las fichas EN ESPERA se vuelven a buscar cada día hábil a las 06:00.'),
          ],
        );
      },
    );
  }
}
