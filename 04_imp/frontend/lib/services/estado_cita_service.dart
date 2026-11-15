import 'dart:convert';
import 'package:http/http.dart' as http;

class EstadoCitaService {
  final String baseUrl;

  EstadoCitaService({this.baseUrl = 'http://localhost:8080/api/v1'});

  Future<String> confirmarAsistencia(int citaId, Map<String, dynamic> usuarioAutenticado) async {
    final response = await http.post(
      Uri.parse('$baseUrl/citas/$citaId/confirmar-asistencia'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode(usuarioAutenticado),
    );
    return response.body;
  }

  Future<String> finalizarAtencion(int citaId, int medicoId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/citas/$citaId/finalizar?medicoId=$medicoId'),
    );
    return response.body;
  }
}