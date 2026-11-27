import 'package:flutter/material.dart';

import '../../controllers/fichas_internas_controller.dart';
import '../../core/app_theme.dart';
import '../../core/rutas_medico.dart';
import '../../models/medico/ficha_interna.dart';
import '../../models/medico/medico_actual.dart';
import '../../services/fichas_internas_service.dart';
import '../comun/cns_widgets.dart';
import 'medico_widgets.dart';

/// Figma "WEB ESPECIALISTA FICHAS INTERNAS" (CU-23): agenda de fichas internas del especialista.
class FichasInternasView extends StatefulWidget {
  const FichasInternasView({super.key, required this.medico, required this.fichas, this.reloj});

  final MedicoActual medico;
  final FichasInternasService fichas;
  final DateTime Function()? reloj;

  @override
  State<FichasInternasView> createState() => _FichasInternasViewState();
}

class _FichasInternasViewState extends State<FichasInternasView> {
  late final FichasEspecialistaController _c =
      FichasEspecialistaController(widget.medico, widget.fichas, reloj: widget.reloj);
  final _observaciones = TextEditingController();

  @override
  void initState() {
    super.initState();
    _c.cargar();
  }

  @override
  void dispose() {
    _c.dispose();
    _observaciones.dispose();
    super.dispose();
  }

  Future<void> _atendida(FichaInterna f) async {
    await _c.atendida(f);
    _observaciones.clear();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final fichas = _c.fichas;
        final elegida = _c.elegida;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilaFecha(
                resumen: _c.resumen, fecha: _c.fecha, alCambiar: _c.cambiarFecha, alHoy: () => _c.cambiarFecha(_c.hoy)),
            const SizedBox(height: 14),
            if (_c.error != null) TextoError(_c.error!),
            TablaCns(
              anchoMinimo: 900,
              textoVacio: _c.cargando ? 'Cargando…' : 'No tienes fichas internas este día.',
              columnas: const [
                ColumnaTabla('Hora'),
                ColumnaTabla('Paciente', flex: 2),
                ColumnaTabla('Tabla', flex: 2),
                ColumnaTabla('Urgencia', flex: 2),
                ColumnaTabla('Motivo', flex: 3),
                ColumnaTabla('Estado', flex: 2),
                ColumnaTabla('Acciones', flex: 4),
              ],
              seleccionada: elegida == null ? null : fichas.indexWhere((f) => f.ticket == elegida.ticket),
              alTocarFila: (i) => _c.elegir(fichas[i]),
              filas: [for (final f in fichas) _fila(f)],
            ),
            const SizedBox(height: 16),
            Columnas(principal: elegida == null ? const SizedBox.shrink() : _detalle(elegida), lateral: _notas()),
          ],
        );
      },
    );
  }

  List<Widget> _fila(FichaInterna f) {
    return [
      Text(f.hora ?? ''),
      Text(f.nombrePaciente),
      ChipEstado(f.tabla.codigo, texto: f.tabla.texto),
      f.urgencia ? const ChipEstado('URGENCIA') : const Text('—'),
      Text(f.motivo, maxLines: 2, overflow: TextOverflow.ellipsis),
      ChipEstado(f.estado.codigo),
      Wrap(
        spacing: 6,
        runSpacing: 6,
        children: [
          if (f.estado == EstadoFicha.atendida && _c.opera)
            FilledButton(
              key: Key('programar_${f.ticket}'),
              style: FilledButton.styleFrom(minimumSize: const Size(0, 32)),
              onPressed: () => irA(context, RutasMedico.programarDe(f.ticket)),
              child: const Text('Programar cirugía'),
            ),
          if (f.estado == EstadoFicha.asignada) ...[
            FilledButton(
              key: Key('atendida_${f.ticket}'),
              style: FilledButton.styleFrom(minimumSize: const Size(0, 32)),
              onPressed: () => _atendida(f),
              child: const Text('Atendida'),
            ),
            BotonPeligro(texto: 'No se presentó', alTocar: _c.yaPasoLaHora(f) ? () => _c.noSePresento(f) : null),
          ],
          if (f.estado == EstadoFicha.cancelada)
            const Text('No se presentó', style: TextStyle(fontSize: 11.5, color: ColoresCns.textoSuave)),
        ],
      ),
    ];
  }

  Widget _detalle(FichaInterna f) {
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TituloTarjeta('Ficha interna N° ${f.ticket} · ${f.nombrePaciente} · ${f.edad} años'),
          DatoFila('Derivado por', '${f.derivadoPor} · ${f.especialidadOrigen.nombre}'),
          DatoFila('Motivo', f.motivo),
          if (f.estado == EstadoFicha.asignada) ...[
            const SizedBox(height: 8),
            CampoCns(
              etiqueta: 'Observaciones (opcional)',
              ayuda: 'Hasta 2000 caracteres.',
              child: TextField(
                key: const Key('observaciones'),
                controller: _observaciones,
                minLines: 3,
                maxLines: 6,
                maxLength: 2000,
                buildCounter: (_, {required currentLength, required isFocused, maxLength}) => null,
                onChanged: _c.escribirObservaciones,
                decoration: const InputDecoration(hintText: 'Escribe tus observaciones…'),
              ),
            ),
            Botones([
              FilledButton(onPressed: () => _atendida(f), child: const Text('Marcar atendida')),
              BotonPeligro(texto: 'No se presentó', alTocar: _c.yaPasoLaHora(f) ? () => _c.noSePresento(f) : null),
            ]),
            Ayuda('«No se presentó» se habilita después de las ${f.hora}.'),
          ] else if (f.observaciones != null)
            Ayuda('Observaciones: ${f.observaciones}'),
        ],
      ),
    );
  }

  Widget _notas() {
    return const TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TituloTarjeta('Para tener en cuenta'),
          ListaPuntos([
            'No ves la gravedad: solo la ve el médico que derivó. Sí ves si fue urgencia y el motivo.',
            'En fichas internas no hay confirmación de asistencia.',
            '«No se presentó» deja la ficha CANCELADA y ese horario no se reasigna.',
            '«Programar cirugía» aparece cuando la ficha está ATENDIDA y tu especialidad opera.',
          ]),
          Ayuda('Motivo de hasta $maxMotivo caracteres, escrito por el médico que derivó.'),
        ],
      ),
    );
  }
}
