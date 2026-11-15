import 'package:flutter/material.dart';

import '../../controllers/fichas_internas_controller.dart';
import '../../controllers/medico_controller.dart';
import '../../core/app_theme.dart';
import '../../core/rutas_medico.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/medico/ficha_interna.dart';
import '../../models/medico/medico_actual.dart';
import '../../services/fichas_internas_service.dart';
import '../../services/medico_service.dart';
import '../comun/cns_widgets.dart';
import 'medico_widgets.dart';
import 'ticket_medico_dialog.dart';

/// Figma "WEB MEDICO TICKET INTERNO" (CU-21): nueva ficha interna, su resultado o el rechazo.
class TicketInternoView extends StatefulWidget {
  const TicketInternoView({
    super.key,
    required this.medico,
    required this.agenda,
    required this.fichas,
    required this.historial,
    this.citaId,
  });

  final MedicoActual medico;
  final AgendaMedicoController agenda;
  final FichasInternasService fichas;
  final HistorialPacienteService historial;
  final int? citaId;

  @override
  State<TicketInternoView> createState() => _TicketInternoViewState();
}

class _TicketInternoViewState extends State<TicketInternoView> {
  late final TicketInternoController _c = TicketInternoController(
      medico: widget.medico, agenda: widget.agenda, fichas: widget.fichas, historial: widget.historial);
  final _busqueda = TextEditingController();
  final _motivo = TextEditingController();

  @override
  void initState() {
    super.initState();
    _c.iniciar(widget.citaId);
  }

  @override
  void dispose() {
    _c.dispose();
    _busqueda.dispose();
    _motivo.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: Listenable.merge([_c, widget.agenda]),
      builder: (context, _) {
        final ficha = _c.ficha;
        if (ficha != null) {
          return Columnas(principal: _resultado(ficha), lateral: _queSigue(ficha));
        }
        return Columnas(principal: _formulario(), lateral: _c.rechazado ? _otrosRechazos() : _reglas());
      },
    );
  }

  Widget _formulario() {
    final rechazo = _c.rechazado ? _c.resultado : null;
    final bloqueado = rechazo != null;
    final cita = _c.cita;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (rechazo != null)
            CajaError(codigo: 'No se pudo generar la ficha · ${rechazo.codigo}', mensaje: rechazo.mensaje ?? '')
          else
            const TituloTarjeta('Nueva ficha interna (derivación)', grande: true),
          const SizedBox(height: 6),
          if (_c.urgencia || cita != null)
            DatoFila('Cita de origen', _c.datoCita)
          else
            CampoCns(
              etiqueta: 'Cita de origen',
              ayuda: _c.citasDerivables.isEmpty ? 'Hoy no hay citas confirmadas ni atendidas.' : null,
              child: DropdownButtonFormField<int>(
                key: const Key('cita_origen'),
                isExpanded: true,
                hint: const Text('Elegir una cita de hoy confirmada o atendida'),
                items: [
                  for (final f in _c.citasDerivables)
                    DropdownMenuItem(
                      value: f.citaId,
                      child: Text('Ficha ${f.numeroFicha} · ${horaCorta(f.hora)} · ${f.nombrePaciente}'),
                    ),
                ],
                onChanged: (id) {
                  if (id != null) {
                    _c.elegirCita(id);
                  }
                },
              ),
            ),
          if (!_c.urgencia)
            CampoCns(
              etiqueta: 'Paciente',
              ayuda: 'Se carga desde la cita (solo lectura).',
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                decoration: BoxDecoration(color: ColoresCns.entrada, borderRadius: BorderRadius.circular(8)),
                child: Text(_c.datoPaciente.isEmpty ? '—' : _c.datoPaciente, key: const Key('dato_paciente')),
              ),
            ),
          Casilla(
            key: const Key('urgencia'),
            valor: _c.urgencia,
            texto: 'Urgencia (sin cita: se busca al paciente y se asigna hoy)',
            deshabilitada: bloqueado,
            alCambiar: _c.cambiarUrgencia,
          ),
          if (_c.urgencia) ...[
            const SizedBox(height: 8),
            Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Expanded(
                  child: CampoCns(
                    etiqueta: 'Buscar paciente por N° de afiliado',
                    child: TextField(
                      key: const Key('buscar_urgencia'),
                      controller: _busqueda,
                      enabled: !bloqueado,
                      onSubmitted: _c.buscar,
                      decoration: decoracionBlanca('7781-03'),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: FilledButton(
                    onPressed: bloqueado ? null : () => _c.buscar(_busqueda.text),
                    child: const Text('Buscar'),
                  ),
                ),
              ],
            ),
            if (_c.paciente != null)
              Text(
                '${_c.paciente!.nombre} · ${_c.paciente!.edad} años'
                '${_c.paciente!.edad >= edadAdultoMayor ? ' · adulto mayor' : ''}',
                key: const Key('paciente_encontrado'),
                style: const TextStyle(fontWeight: FontWeight.w800, color: ColoresCns.menu),
              ),
            if (_c.errorBusqueda != null) TextoError(_c.errorBusqueda!),
          ],
          const SizedBox(height: 10),
          CampoCns(
            etiqueta: 'Especialidad interna',
            ayuda: _c.urgencia ? null : 'Terapia Intensiva solo se habilita con Urgencia.',
            child: SelectorEspecialidadInterna(
              valor: _c.especialidad,
              urgencia: _c.urgencia,
              deshabilitado: bloqueado,
              alCambiar: _c.elegirEspecialidad,
            ),
          ),
          CampoCns(
            etiqueta: 'Gravedad',
            child: SelectorGravedad(valor: _c.gravedad, deshabilitado: bloqueado, alCambiar: _c.elegirGravedad),
          ),
          CampoCns(
            etiqueta: 'Motivo',
            ayuda: '${_c.motivo.length} / $maxMotivo caracteres',
            child: TextField(
              key: const Key('motivo'),
              controller: _motivo,
              enabled: !bloqueado,
              minLines: 3,
              maxLines: 6,
              maxLength: maxMotivo,
              buildCounter: (_, {required currentLength, required isFocused, maxLength}) => null,
              onChanged: _c.escribirMotivo,
            ),
          ),
          if (_c.error != null) TextoError(_c.error!),
          if (bloqueado)
            OutlinedButton(
                onPressed: () => volverA(context, RutasMedico.agenda), child: const Text('Volver a la agenda'))
          else
            FilledButton(
              key: const Key('generar_ficha'),
              onPressed: _c.listo && !_c.enviando ? _c.generar : null,
              child: Text(_c.enviando ? 'Generando…' : 'Generar'),
            ),
        ],
      ),
    );
  }

  Widget _reglas() {
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const TituloTarjeta('Cómo se asigna'),
          const Text(
              '1. Urgencia\n2. Gravedad (ALTA > MEDIA > BAJA)\n3. Adulto mayor (60 años o más)\n4. Orden de llegada',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w700, height: 1.5)),
          const SizedBox(height: 8),
          Ayuda(_c.urgencia
              ? 'Con urgencia la ficha se asigna HOY desde ahora. Si no hay hueco, toma el lugar de la ficha de hoy de '
                  'menor prioridad que todavía no empezó (al desplazado se le notifica y se le reimprime el ticket). '
                  'Terapia Intensiva se asigna ya, al médico de TI que se libere antes.'
              : 'Sin urgencia se busca desde mañana, hasta 15 días hábiles, con el médico de la especialidad que '
                  'tenga menos fichas. Si no hay hueco, la ficha queda EN ESPERA.'),
          const Ayuda(
              'La gravedad solo la ves tú (médico general o pediatra). No sale en el ticket ni en la '
              'notificación.',
              color: ColoresCns.peligro),
          const SizedBox(height: 8),
          const Text('Especialidades internas · duración de la ficha',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w800)),
          const SizedBox(height: 4),
          for (final e in Especialidad.internas)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 2),
              child: Row(
                children: [
                  Expanded(child: Text(e.nombre, style: const TextStyle(fontSize: 12.5))),
                  Text(
                    '${e.duracionMin}–${e.duracionMax} min${e == Especialidad.terapiaIntensiva ? ' · solo urgencia' : ''}',
                    style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _otrosRechazos() {
    final codigo = _c.resultado?.codigo;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const TituloTarjeta('Otros rechazos posibles'),
          for (final (c, descripcion) in rechazosFicha)
            if (c != codigo)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(c, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800)),
                    Text(descripcion, style: const TextStyle(fontSize: 12)),
                  ],
                ),
              ),
        ],
      ),
    );
  }

  Widget _resultado(FichaInterna f) {
    final asignada = f.estado == EstadoFicha.asignada;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              ChipEstado(f.estado.codigo),
              const SizedBox(width: 10),
              Expanded(
                child: Text(asignada ? 'Ficha interna generada' : 'Ficha interna en espera',
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text('Ticket N° ${f.ticket}',
              key: const Key('ticket_generado'),
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w800, color: ColoresCns.menu)),
          const SizedBox(height: 8),
          DatoFila('Paciente', '${f.nombrePaciente} · ${f.numeroAfiliado}'),
          DatoFila('Especialidad', f.especialidad.nombre),
          DatoFila('Médico', f.medico ?? 'Por asignar'),
          if (asignada) ...[
            DatoFila('Consultorio', f.consultorio ?? ''),
            DatoFila('Fecha', capitalizada(fechaLargaIso(f.fecha!))),
            DatoFila('Hora', f.hora ?? ''),
            DatoFila('Duración aproximada', '${f.duracionMin} min'),
          ] else ...[
            const DatoFila('Fecha y hora', 'Sin fecha todavía'),
            const Ayuda('No hay cupo en los próximos 15 días hábiles y todas las fichas asignadas tienen mayor '
                'prioridad.'),
          ],
          Botones([
            FilledButton(
              key: const Key('imprimir_ficha'),
              onPressed: () => mostrarTicketMedico(context, TicketMedico.deFicha(f)),
              child: Text(asignada ? 'Imprimir ticket' : 'Imprimir constancia'),
            ),
            OutlinedButton(
                onPressed: () => volverA(context, RutasMedico.agenda), child: const Text('Volver a la agenda')),
          ]),
        ],
      ),
    );
  }

  Widget _queSigue(FichaInterna f) {
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const TituloTarjeta('Qué pasa ahora'),
          ListaPuntos(f.estado == EstadoFicha.asignada
              ? const [
                  'El paciente recibe el aviso «Ficha interna asignada» en la app.',
                  'Imprime el ticket por si el paciente no tiene internet. La gravedad no se imprime.',
                  'No hay confirmación de asistencia: si no se presenta, el especialista la marca como «No se '
                      'presentó» y ese horario no se reasigna.',
                  'Si un caso más grave necesita este horario, el sistema lo mueve al siguiente hueco libre, avisa al '
                      'paciente y se reimprime el ticket.',
                ]
              : const [
                  'Cada día hábil a las 06:00 el sistema vuelve a buscar un hueco, en orden de prioridad y sin '
                      'desplazar a nadie.',
                  'Cuando lo encuentre, el paciente recibe el aviso «Ficha interna asignada» y se puede reimprimir '
                      'el ticket.',
                  'La ficha aparece como EN ESPERA en la cola de prioridad.',
                ]),
        ],
      ),
    );
  }
}
