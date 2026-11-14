import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/fichas/cita_model.dart';

class FichasService {
  final String baseUrl;

  FichasService({this.baseUrl = 'http://localhost:8080/api/v1'});

  Future<List<CitaModel>> obtenerFichasDelDia({String? criterio, String? valor}) async {
    String url = '$baseUrl/fichas/hoy';
    if (criterio != null && valor != null && valor.isNotEmpty) {
      url += '?criterio=$criterio&valor=$valor';
    }

    final response = await http.get(Uri.parse(url));

    if (response.statusCode == 200) {
      Iterable data = json.decode(response.body);
      return data.map((json) => CitaModel.fromJson(json)).toList();
    } else if (response.statusCode == 422) {
      throw Exception('Fecha distinta de hoy no permitida (422)');
    } else {
      throw Exception('Error al cargar las fichas del día: ${response.statusCode}');
    }
  }

  Future<List<CitaModel>> obtenerListadoParaTv() async {
    final response = await http.get(Uri.parse('$baseUrl/tv/fichas'));

    if (response.statusCode == 200) {
      Iterable data = json.decode(response.body);
      return data.map((json) => CitaModel.fromJson(json)).toList();
    } else {
      throw Exception('Error al cargar el listado para TV: ${response.statusCode}');
    }
  }
}