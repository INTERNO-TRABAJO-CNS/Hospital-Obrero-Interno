import 'dart:async';
import 'package:flutter/material.dart';
import '../../controllers/fichas_controller.dart';
import '../../models/fichas/cita_model.dart';

class TvFichasView extends StatefulWidget {
  final FichasController controller;

  const TvFichasView({Key? key, required this.controller}) : super(key: key);

  @override
  State<TvFichasView> createState() => _TvFichasViewState();
}

class _TvFichasViewState extends State<TvFichasView> {
  List<CitaModel> _citas = [];
  Timer? _timer;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _cargarFichasTv();
    // Temporizador de actualización automática cada 10 segundos para la TV
    _timer = Timer.periodic(const Duration(seconds: 10), (timer) {
      _cargarFichasTv();
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _cargarFichasTv() async {
    try {
      final citas = await widget.controller.obtenerListadoParaTv();
      if (mounted) {
        setState(() {
          _citas = citas;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    // Verificación de diseño optimizado para pantallas grandes (>= 1600 px)
    return Scaffold(
      backgroundColor: Colors.black,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Encabezado de la TV (Sin menús ni botones)
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: const [
                  Text(
                    'HOSPITAL OBRERO - FICHAS DEL DÍA',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    'SISTEMA DE LLAMADO',
                    style: TextStyle(
                      color: Colors.amber,
                      fontSize: 24,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              const Divider(color: Colors.white54, thickness: 2),
              const SizedBox(height: 10),
              
              // Cuerpo principal: Tabla grande de solo lectura
              Expanded(
                child: _isLoading
                    ? const Center(
                        child: CircularProgressIndicator(color: Colors.white),
                      )
                    : _citas.isEmpty
                        ? const Center(
                            child: Text(
                              'No hay fichas registradas para el día de hoy',
                              style: TextStyle(color: Colors.white70, fontSize: 28),
                            ),
                          )
                        : ListView.builder(
                            itemCount: _citas.length,
                            itemBuilder: (context, index) {
                              final cita = _citas[index];
                              return Container(
                                margin: const EdgeInsets.symmetric(vertical: 6.0),
                                padding: const EdgeInsets.all(16.0),
                                decoration: BoxDecoration(
                                  color: Colors.grey[900],
                                  borderRadius: BorderRadius.circular(8.0),
                                  border: Border.all(color: Colors.blueAccent, width: 1.5),
                                ),
                                child: Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    _buildTvCell('Ficha: ${cita.ficha}', Colors.cyanAccent, 28),
                                    _buildTvCell('Hora: ${cita.hora}', Colors.white, 24),
                                    _buildTvCell('Esp: ${cita.especialidad}', Colors.white70, 24),
                                    _buildTvCell('Médico: ${cita.medico}', Colors.white70, 24),
                                    _buildTvCell('Cons: ${cita.consultorio}', Colors.amberAccent, 24),
                                    _buildTvCell(cita.estado, Colors.greenAccent, 24, isBold: true),
                                  ],
                                ),
                              );
                            },
                          ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTvCell(String texto, Color color, double fontSize, {bool isBold = false}) {
    return Expanded(
      child: Text(
        texto,
        textAlign: TextAlign.center,
        style: TextStyle(
          color: color,
          fontSize: fontSize,
          fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
        ),
        overflow: TextOverflow.ellipsis,
      ),
    );
  }
}