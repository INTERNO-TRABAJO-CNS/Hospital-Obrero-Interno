
import 'dart:async';
import 'package:flutter/material.dart';

import '../../controllers/fichas_controller.dart';
import '../../models/fichas/tv_ficha_model.dart';

class TvFichasView extends StatefulWidget {
  final FichasController controller;

  const TvFichasView({
    super.key,
    required this.controller,
  });

  @override
  State<TvFichasView> createState() => _TvFichasViewState();
}

class _TvFichasViewState extends State<TvFichasView> {
  List<TvFichaModel> _citas = [];
  bool _cargando = true;
  String? _error;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _cargarFichas();
    _timer = Timer.periodic(
      const Duration(seconds: 10),
      (_) => _cargarFichas(),
    );
  }

  Future<void> _cargarFichas() async {
    try {
      final fichas = await widget.controller.obtenerListadoParaTv();

      if (!mounted) return;

      setState(() {
        _citas = fichas;
        _cargando = false;
        _error = null;
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _error = 'No se pudieron cargar las fichas.';
        _cargando = false;
      });
    }
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Fichas del día'),
        centerTitle: true,
      ),
      body: _cargando
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : _citas.isEmpty
                  ? const Center(
                      child: Text('No hay fichas para mostrar'),
                    )
                  : ListView.builder(
                      itemCount: _citas.length,
                      itemBuilder: (context, index) {
                        final cita = _citas[index];

                        return Card(
                          margin: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 8,
                          ),
                          child: ListTile(
                            leading: CircleAvatar(
                              child: Text('${cita.numeroFicha}'),
                            ),
                            title: Text('Ficha: ${cita.numeroFicha}'),
                            subtitle: Text(
                              'Hora: ${cita.hora}\n'
                              'Especialidad: ${cita.especialidad}\n'
                              'Consultorio: ${cita.consultorio}',
                            ),
                            trailing: Text(cita.estado),
                          ),
                        );
                      },
                    ),
    );
  }
}
