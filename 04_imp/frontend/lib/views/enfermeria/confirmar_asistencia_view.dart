import 'dart:async';

import 'package:flutter/material.dart';

import '../../controllers/enfermeria_controller.dart';
import '../../core/app_theme.dart';
import '../../models/comun/fechas.dart';
import '../../models/enfermeria/ficha_dia.dart';
import '../../services/enfermeria_service.dart';
import '../comun/cns_widgets.dart';

/// Figma "ENFERMERIA CONFIRMAR ASISTENCIA": citas que todavia se pueden confirmar, con el
/// tiempo que falta. Cada [refresco] se vuelve a pedir la lista (asi desaparecen las vencidas).
class ConfirmarAsistenciaView extends StatefulWidget {
  const ConfirmarAsistenciaView({
    super.key,
    required this.servicio,
    required this.usuario,
    this.reloj,
    this.refresco = const Duration(seconds: 30),
  });

  final EnfermeriaService servicio;
  final String usuario;
  final DateTime Function()? reloj;
  final Duration refresco;

  @override
  State<ConfirmarAsistenciaView> createState() => _ConfirmarAsistenciaViewState();
}

class _ConfirmarAsistenciaViewState extends State<ConfirmarAsistenciaView> {
  late final EnfermeriaController _c =
      EnfermeriaController(widget.servicio, usuario: widget.usuario, reloj: widget.reloj);
  Timer? _temporizador;
  final Set<int> _enviando = {};

  @override
  void initState() {
    super.initState();
    _c.cargar();
    _temporizador = Timer.periodic(widget.refresco, (_) => _c.cargar());
  }

  @override
  void dispose() {
    _temporizador?.cancel();
    _c.dispose();
    super.dispose();
  }

  Future<void> _confirmar(FichaDelDia f) async {
    setState(() => _enviando.add(f.citaId));
    final error = await _c.confirmar(f);
    if (!mounted) {
      return;
    }
    setState(() => _enviando.remove(f.citaId));
    mostrarAviso(context, error ?? 'Llegada confirmada: ${f.paciente} (ficha ${f.numeroFicha}).');
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final lista = _c.porConfirmar;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Confirma la llegada del paciente hasta $minutosAntesParaConfirmar minutos antes de su cita. '
              'Pasado ese tiempo, la cita se cancela automáticamente.',
              style: TextStyle(fontSize: 13),
            ),
            const SizedBox(height: 14),
            if (_c.error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro)),
              ),
            if (_c.cargando && _c.fichas.isEmpty)
              const Cargando()
            else if (lista.isEmpty)
              const TarjetaBlanca(
                child: Text('No hay citas por confirmar en este momento.', key: Key('confirmar_vacio')),
              )
            else
              for (final f in lista)
                Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: _TarjetaConfirmar(
                    ficha: f,
                    minutos: _c.minutosRestantes(f),
                    enviando: _enviando.contains(f.citaId),
                    alConfirmar: () => _confirmar(f),
                  ),
                ),
            const AvisoDatosPrueba(),
          ],
        );
      },
    );
  }
}

class _TarjetaConfirmar extends StatelessWidget {
  const _TarjetaConfirmar({
    required this.ficha,
    required this.minutos,
    required this.enviando,
    required this.alConfirmar,
  });

  final FichaDelDia ficha;
  final int minutos;
  final bool enviando;
  final VoidCallback alConfirmar;

  @override
  Widget build(BuildContext context) {
    final f = ficha;
    final datos = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text('Ficha ${f.numeroFicha} · ${horaTexto(f.inicio)}',
            style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800)),
        const SizedBox(height: 3),
        Text('${f.paciente} · ${f.medico} · Consultorio ${f.consultorio}',
            style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave)),
      ],
    );
    final plazo = Column(
      crossAxisAlignment: CrossAxisAlignment.end,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text('Confirmar hasta ${horaTexto(f.limiteConfirmacion)}',
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700)),
        Text(
          'Faltan $minutos min',
          key: Key('faltan_${f.citaId}'),
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: minutos <= 10 ? ColoresCns.peligro : ColoresCns.menu,
          ),
        ),
      ],
    );
    final boton = FilledButton(
      key: Key('confirmar_${f.citaId}'),
      onPressed: enviando ? null : alConfirmar,
      child: const Text('Confirmó llegada'),
    );
    return TarjetaBlanca(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
      child: LayoutBuilder(builder: (context, r) {
        if (r.maxWidth < 560) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              datos,
              const SizedBox(height: 10),
              Align(alignment: Alignment.centerLeft, child: plazo),
              const SizedBox(height: 10),
              boton
            ],
          );
        }
        return Row(
          children: [
            Expanded(child: datos),
            plazo,
            const SizedBox(width: 18),
            boton,
          ],
        );
      }),
    );
  }
}
