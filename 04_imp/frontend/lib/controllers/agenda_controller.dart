import '../models/agenda/agenda_dia_model.dart';
import '../services/agenda_service.dart';

class AgendaController {
  final AgendaService _agendaService;

  AgendaController({AgendaService? agendaService})
      : _agendaService = agendaService ?? FakeAgendaService();

  Future<AgendaDiaModel> cargarAgenda(int medicoId, String fecha) async {
    return await _agendaService.obtenerAgenda(medicoId, fecha);
  }
}