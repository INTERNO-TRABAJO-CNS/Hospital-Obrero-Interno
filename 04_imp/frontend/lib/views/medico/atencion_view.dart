import 'package:flutter/material.dart';

import '../../controllers/atencion_controller.dart';
import '../../controllers/historial_paciente_controller.dart';
import '../../controllers/medico_controller.dart';
import '../../core/app_theme.dart';
import '../../core/rutas_medico.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/enfermeria/ficha_dia.dart';
import '../../models/medico/atencion.dart';
import '../../services/cirugias_service.dart';
import '../../services/fichas_internas_service.dart';
import '../../services/medico_service.dart';
import '../comun/cns_widgets.dart';
import 'medico_widgets.dart';

/// "Atención" del menú: abre al primer paciente de hoy con asistencia confirmada; si no hay, lo explica.
class AtencionInicioView extends StatefulWidget {
  const AtencionInicioView({super.key, required this.agenda});

  final AgendaMedicoController agenda;

  @override
  State<AtencionInicioView> createState() => _AtencionInicioViewState();
}

class _AtencionInicioViewState extends State<AtencionInicioView> {
  AgendaMedicoController get _c => widget.agenda;

  @override
  void initState() {
    super.initState();
    _iniciar();
  }

  Future<void> _iniciar() async {
    await _c.volverAHoy();
    if (!mounted || !_c.esHoy) {
      return;
    }
    final siguiente = _c.agenda?.fichas.where(_c.puedeAtender).firstOrNull;
    if (siguiente != null) {
      Navigator.of(context).pushReplacementNamed(RutasMedico.atender(siguiente.citaId));
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final confirmadas = (_c.agenda?.fichas ?? const []).where(_c.puedeAtender).toList();
        return Align(
          alignment: Alignment.topLeft,
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 560),
            child: TarjetaBlanca(
              child: _c.cargando
                  ? const Text('Buscando al siguiente paciente…')
                  : confirmadas.isEmpty
                      ? Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const TituloTarjeta('No hay pacientes con la asistencia confirmada'),
                            const Ayuda('«Atender» se habilita cuando enfermería confirma la asistencia. '
                                'Revisa la agenda del día.'),
                            Botones([
                              FilledButton(
                                  onPressed: () => volverA(context, RutasMedico.agenda),
                                  child: const Text('Ir a la agenda')),
                            ]),
                          ],
                        )
                      : Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            const TituloTarjeta('Pacientes listos para atender'),
                            for (final f in confirmadas)
                              Padding(
                                padding: const EdgeInsets.only(bottom: 8),
                                child: OutlinedButton(
                                  onPressed: () => irA(context, RutasMedico.atender(f.citaId)),
                                  child: Text('Ficha ${f.numeroFicha} · ${horaCorta(f.hora)} · ${f.nombrePaciente}'),
                                ),
                              ),
                          ],
                        ),
            ),
          ),
        );
      },
    );
  }
}

/// Figma "WEB MEDICO ATENCION" (CU-15): datos de la cita, receta y historial reciente.
class AtencionView extends StatefulWidget {
  const AtencionView({
    super.key,
    required this.citaId,
    required this.servicio,
    required this.historial,
    this.fichas,
    this.cirugias,
  });

  final int citaId;
  final AtencionService servicio;
  final HistorialPacienteService historial;
  final FichasInternasService? fichas;
  final CirugiasService? cirugias;

  @override
  State<AtencionView> createState() => _AtencionViewState();
}

class _AtencionViewState extends State<AtencionView> {
  late final AtencionController _c = AtencionController(widget.servicio);
  late final HistorialPacienteController _h =
      HistorialPacienteController(widget.historial, fichas: widget.fichas, cirugias: widget.cirugias);
  final _receta = TextEditingController();

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  Future<void> _cargar() async {
    await _c.cargar(widget.citaId);
    final d = _c.datos;
    if (d != null) {
      await _h.cargar(d.pacienteId);
    }
  }

  @override
  void dispose() {
    _c.dispose();
    _h.dispose();
    _receta.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: Listenable.merge([_c, _h]),
      builder: (context, _) {
        final d = _c.datos;
        if (_c.cargando) {
          return const TarjetaBlanca(child: Text('Cargando la cita…'));
        }
        if (d == null) {
          return TarjetaBlanca(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                TextoError(_c.error ?? 'No se encontró la cita.'),
                Botones([
                  OutlinedButton(
                      onPressed: () => volverA(context, RutasMedico.agenda), child: const Text('Volver a la agenda')),
                ]),
              ],
            ),
          );
        }
        return Columnas(principal: _principal(d), lateral: _lateral(d));
      },
    );
  }

  Widget _principal(DatosAtencion d) {
    final tipo = d.titular == true
        ? ' · Titular'
        : d.titular == false
            ? ' · Beneficiario'
            : '';
    final paciente = d.edad != null ? '${d.nombrePaciente} · ${d.edad} años' : d.nombrePaciente;
    final excedido = _c.caracteres > maxReceta;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TituloTarjeta('Ficha N° ${d.numeroFicha} · ${fechaDiaMesIso(d.fecha)} · ${horaCorta(d.hora)}', grande: true),
          DatoFila('Afiliado', '${d.numeroAfiliado}$tipo'),
          DatoFila('Paciente', paciente),
          DatoFila('Consulta', '${d.consulta.nombre} · ${d.nombreConsultorio ?? 'Consultorio ${d.consultorio}'}'),
          DatoFila('Estado', '', child: ChipEstado((d.estado ?? EstadoCita.asistenciaConfirmada).codigo)),
          const SizedBox(height: 10),
          if (!_c.finalizada)
            CampoCns(
              etiqueta: 'Medicación / receta',
              ayuda: 'Entre 1 y $maxReceta caracteres.${_c.caracteres > 0 ? ' (${_c.caracteres})' : ''}',
              child: TextField(
                key: const Key('receta'),
                controller: _receta,
                minLines: 5,
                maxLines: 10,
                onChanged: _c.escribir,
                decoration: InputDecoration(
                  hintText: 'Medicamento, dosis, frecuencia y duración',
                  errorText: excedido ? 'Pasa de $maxReceta caracteres' : null,
                ),
              ),
            )
          else
            const TextoOk('Atención finalizada: la receta quedó en el historial y la cita pasó a ATENDIDA.'),
          if (_c.error != null) TextoError(_c.error!),
          Botones([
            if (!_c.finalizada)
              FilledButton(
                key: const Key('finalizar_atencion'),
                onPressed: _c.puedeFinalizar ? _c.finalizar : null,
                child: Text(_c.enviando ? 'Guardando…' : 'Finalizar atención'),
              )
            else
              FilledButton(
                  onPressed: () => volverA(context, RutasMedico.agenda), child: const Text('Volver a la agenda')),
            if (d.puedeDerivar)
              OutlinedButton(
                onPressed: () => irA(context, RutasMedico.ticketInternoDe(d.citaId)),
                child: const Text('Generar ticket interno'),
              ),
          ]),
          const Ayuda('Al finalizar, la receta se agrega al historial y la cita pasa a ATENDIDA. El ticket interno '
              'se puede generar antes o después de finalizar, siempre el mismo día.'),
        ],
      ),
    );
  }

  Widget _lateral(DatosAtencion d) {
    final recientes = _h.recientes;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const TituloTarjeta('Historial reciente'),
          if (_h.cargando)
            const Ayuda('Cargando…')
          else if (recientes.isEmpty)
            const Ayuda('Sin atenciones anteriores.'),
          for (final e in recientes)
            Container(
              margin: const EdgeInsets.only(bottom: 8),
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: e.tipo == TipoEvento.cirugia ? const Color(0xFFFBEBD3) : ColoresCns.filaPar,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    e.tipo == TipoEvento.cirugia
                        ? '${fechaMesIso(e.fecha, conAnio: false)} · Cirugía'
                        : '${fechaMesIso(e.fecha, conAnio: false)} · ${e.titulo}',
                    style: const TextStyle(fontSize: 12.5, fontWeight: FontWeight.w800),
                  ),
                  if (e.tipo == TipoEvento.cirugia)
                    Text('${e.lineas.first.split(' · ').first} · ${e.estado}', style: const TextStyle(fontSize: 12))
                  else
                    for (final r in e.recetas) Text('Receta: $r', style: const TextStyle(fontSize: 12)),
                ],
              ),
            ),
          const SizedBox(height: 6),
          OutlinedButton(
            onPressed: () => irA(context, RutasMedico.historialDe(d.pacienteId)),
            child: const Text('Ver historial completo'),
          ),
        ],
      ),
    );
  }
}
