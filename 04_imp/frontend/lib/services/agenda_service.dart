import '../models/agenda/agenda_dia_model.dart';

class AgendaService {
  Future<AgendaDiaModel> obtenerAgenda(int medicoId, String fecha) async {
    // Conexión real con el backend en /api/v1/medico/agenda?fecha=...
    throw UnimplementedError();
  }
}

class FakeAgendaService extends AgendaService {
  @override
  Future<AgendaDiaModel> obtenerAgenda(int medicoId, String fecha) async {
    await Future.delayed(const Duration(milliseconds: 200));
    return AgendaDiaModel(
      fecha: fecha,
      cantidad: 2,
      fichas: [
        FichaAgendaModel(
          citaId: 1,
          numeroFicha: 1,
          hora: '08:30',
          nombrePaciente: 'Juan Pérez',
          estado: 'PROGRAMADA',
        ),
        FichaAgendaModel(
          citaId: 2,
          numeroFicha: 2,
          hora: '09:00',
          nombrePaciente: 'María López',
          estado: 'CANCELADA',
        ),
      ],
    );
  }
}