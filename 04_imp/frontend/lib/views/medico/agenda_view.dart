import 'package:flutter/material.dart';
import '../../controllers/agenda_controller.dart';
import '../../models/agenda/agenda_dia_model.dart';

class AgendaView extends StatefulWidget {
  const AgendaView({Key? key}) : super(key: key);

  @override
  State<AgendaView> createState() => _AgendaViewState();
}

class _AgendaViewState extends State<AgendaView> {
  final AgendaController _controller = AgendaController();
  late Future<AgendaDiaModel> _agendaFuture;
  DateTime _fechaSeleccionada = DateTime.now();
  FichaAgendaModel? _fichaSeleccionada;

  @override
  void initState() {
    super.initState();
    _cargarDatos();
  }

  void _cargarDatos() {
    String fechaStr = _fechaSeleccionada.toLocal().toString().split(' ')[0];
    _agendaFuture = _controller.cargarAgenda(1, fechaStr);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Agenda Médica - ${_fechaSeleccionada.toLocal().toString().split(' ')[0]}'),
        actions: [
          IconButton(
            icon: const Icon(Icons.calendar_today),
            tooltip: 'Otras fechas',
            onPressed: () async {
              DateTime? picked = await showDatePicker(
                context: context,
                initialDate: _fechaSeleccionada,
                firstDate: DateTime(2025),
                lastDate: DateTime(2030),
              );
              if (picked != null && picked != _fechaSeleccionada) {
                setState(() {
                  _fechaSeleccionada = picked;
                  _fichaSeleccionada = null;
                  _cargarDatos();
                });
              }
            },
          ),
        ],
      ),
      body: FutureBuilder<AgendaDiaModel>(
        future: _agendaFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Error al cargar la agenda: ${snapshot.error}'));
          } else if (!snapshot.hasData || snapshot.data!.fichas.isEmpty) {
            return const Center(child: Text('0 citas hoy'));
          }

          final agenda = snapshot.data!;

          return LayoutBuilder(
            builder: (context, constraints) {
              bool esEscritorio = constraints.maxWidth > 800;

              if (esEscritorio) {
                // Escritorio: Lista a la izquierda y panel de detalle a la derecha
                return Row(
                  children: [
                    Expanded(flex: 2, child: _construirLista(agenda)),
                    const VerticalDivider(width: 1),
                    Expanded(flex: 3, child: _construirPanelDetalle()),
                  ],
                );
              } else {
                // Celular: Solo lista vertical
                return _construirLista(agenda);
              }
            },
          );
        },
      ),
    );
  }

  Widget _construirLista(AgendaDiaModel agenda) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.all(16.0),
          child: Text(
            '${agenda.cantidad} citas hoy',
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
        ),
        Expanded(
          child: ListView.builder(
            itemCount: agenda.fichas.length,
            itemBuilder: (context, index) {
              final ficha = agenda.fichas[index];
              bool esCancelada = ficha.estado == 'CANCELADA';

              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: esCancelada ? Colors.grey : Colors.blue,
                  child: Text('${ficha.numeroFicha}'),
                ),
                title: Text(
                  ficha.nombrePaciente,
                  style: TextStyle(
                    decoration: esCancelada ? TextDecoration.lineThrough : null,
                  ),
                ),
                subtitle: Text('Hora: ${ficha.hora} · Ficha #${ficha.numeroFicha}'),
                trailing: Chip(
                  label: Text(ficha.estado),
                  backgroundColor: esCancelada ? Colors.red.shade100 : Colors.green.shade100,
                ),
                onTap: () {
                  setState(() {
                    _fichaSeleccionada = ficha;
                  });
                },
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _construirPanelDetalle() {
    if (_fichaSeleccionada == null) {
      return const Center(
        child: Text(
          'Seleccione una cita para ver el detalle',
          style: TextStyle(color: Colors.grey, fontSize: 16),
        ),
      );
    }

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Detalle de la Cita',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          Text('ID de Cita: ${_fichaSeleccionada!.citaId}', style: const TextStyle(fontSize: 16)),
          const SizedBox(height: 10),
          Text('Número de Ficha: ${_fichaSeleccionada!.numeroFicha}', style: const TextStyle(fontSize: 16)),
          const SizedBox(height: 10),
          Text('Paciente: ${_fichaSeleccionada!.nombrePaciente}', style: const TextStyle(fontSize: 16)),
          const SizedBox(height: 10),
          Text('Hora programada: ${_fichaSeleccionada!.hora}', style: const TextStyle(fontSize: 16)),
          const SizedBox(height: 10),
          Text('Estado: ${_fichaSeleccionada!.estado}', style: const TextStyle(fontSize: 16)),
        ],
      ),
    );
  }
}