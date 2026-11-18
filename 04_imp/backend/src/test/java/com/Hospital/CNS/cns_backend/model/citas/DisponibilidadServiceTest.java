package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-06 con Fakes: un test por cada flujo del diagrama CU-06_ValidarDisponibilidadRecursos ([2a], [3a], [4a], [5a])
 * mas los bordes. Lunes 23/11/2026; el Dr. Ramos (Medicina General, 30 min) atiende en el consultorio 1.
 */
class DisponibilidadServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 23);
    private static final long RAMOS = 1;
    private static final long VARGAS = 2;
    private static final int CONSULTORIO_RAMOS = 1;
    private static final int OTRO_CONSULTORIO = 2;
    private static final long PACIENTE = 100;
    private static final int DURACION = 30;

    private final FakeCitaRepository citas = new FakeCitaRepository();
    private final FakeAgendaRepository agenda = new FakeAgendaRepository(citas);
    private final DisponibilidadService service = new DisponibilidadService(agenda, citas);

    // ---- Camino principal ----

    @Test
    void validar_disponible() {
        cita(RAMOS, 200, CONSULTORIO_RAMOS, LocalTime.of(8, 0), EstadoCita.PROGRAMADA);
        cita(RAMOS, 201, CONSULTORIO_RAMOS, LocalTime.of(8, 30), EstadoCita.ASISTENCIA_CONFIRMADA);

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
        assertEquals(1, agenda.intentosDeBloqueo, "siempre intenta tomar la agenda del medico");
    }

    // ---- [2a] Medico u horario ocupado ----

    @Test
    void validar_medicoOcupado() {
        cita(RAMOS, 200, CONSULTORIO_RAMOS, LocalTime.of(9, 0), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.MEDICO_OCUPADO, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_horaDeCitaCancelada_quedaLibre() {
        cita(RAMOS, 200, CONSULTORIO_RAMOS, LocalTime.of(9, 0), EstadoCita.CANCELADA);

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
    }

    // ---- [3a] Consultorio ocupado ----

    @Test
    void validar_consultorioOcupado() {
        agenda.duracion(VARGAS, 30);
        cita(VARGAS, 200, CONSULTORIO_RAMOS, LocalTime.of(9, 15), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.CONSULTORIO_OCUPADO, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_consultorioLibre_siLaOtraTerminaJustoAntes() {
        agenda.duracion(VARGAS, 30);
        cita(VARGAS, 200, CONSULTORIO_RAMOS, LocalTime.of(8, 30), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
    }

    // ---- [4a] Medico con 7 fichas o paciente con otra cita ese dia ----

    @Test
    void validar_medicoCompleto7() {
        sieteCitasDeRamos();

        assertEquals(ResultadoDisponibilidad.MEDICO_COMPLETO, service.validar(candidata(LocalTime.of(12, 0))));
    }

    @Test
    void validar_seisCitas_todaviaDisponible() {
        for (int i = 0; i < 6; i++) {
            cita(RAMOS, 200 + i, CONSULTORIO_RAMOS, LocalTime.of(8, 0).plusMinutes(30L * i), EstadoCita.PROGRAMADA);
        }

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(12, 0))));
    }

    @Test
    void validar_citasCanceladasNoCuentanParaLas7() {
        sieteCitasDeRamos();
        citas.cambiarEstado(citas.citas.get(0).citaId(), EstadoCita.PROGRAMADA, EstadoCita.CANCELADA);

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(12, 0))));
    }

    @Test
    void validar_pacienteYaTieneCita() {
        CitaDTO enPediatria = new CitaDTO(citas.nextIdentity(), PACIENTE, "", "", VARGAS, "", TipoEspecialidad.PEDIATRIA,
                OTRO_CONSULTORIO, DIA, LocalTime.of(15, 0), 1, EstadoCita.PROGRAMADA);
        citas.store(enPediatria);

        assertEquals(ResultadoDisponibilidad.PACIENTE_YA_TIENE_CITA, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_pacienteConCitaCanceladaEseDia_disponible() {
        cita(VARGAS, PACIENTE, OTRO_CONSULTORIO, LocalTime.of(15, 0), EstadoCita.CANCELADA);

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_pacienteConCitaOtroDia_disponible() {
        citas.store(new CitaDTO(citas.nextIdentity(), PACIENTE, "", "", RAMOS, "", TipoEspecialidad.CONSULTORIO_FAMILIAR,
                CONSULTORIO_RAMOS, DIA.plusDays(1), LocalTime.of(9, 0), 1, EstadoCita.PROGRAMADA));

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
    }

    // ---- [5a] Cita solapada o solicitud simultanea ganadora ----

    @Test
    void validar_solapada() {
        // El medico tiene una cita 08:45-09:15 (en otro consultorio): la candidata 09:00-09:30 se cruza.
        cita(RAMOS, 200, OTRO_CONSULTORIO, LocalTime.of(8, 45), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.SOLAPADA, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_citaQueEmpiezaAntesDeTerminar_solapa() {
        cita(RAMOS, 200, OTRO_CONSULTORIO, LocalTime.of(9, 20), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.SOLAPADA, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_citasSeguidas_noSolapan() {
        cita(RAMOS, 200, CONSULTORIO_RAMOS, LocalTime.of(8, 30), EstadoCita.PROGRAMADA);
        cita(RAMOS, 201, CONSULTORIO_RAMOS, LocalTime.of(9, 30), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_bloqueadaPorOtro() {
        agenda.tomarPorOtro(RAMOS, DIA);

        assertEquals(ResultadoDisponibilidad.BLOQUEADA_POR_OTRO, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_bloqueoDeOtroDia_noAfecta() {
        agenda.tomarPorOtro(RAMOS, DIA.plusDays(1));

        assertEquals(ResultadoDisponibilidad.DISPONIBLE, service.validar(candidata(LocalTime.of(9, 0))));
    }

    // ---- Orden: se devuelve el primer motivo que falla ----

    @Test
    void validar_variosMotivos_devuelveElPrimeroEnOrden() {
        sieteCitasDeRamos(); // incluye 09:00 (medico ocupado) y llena el dia (medico completo)
        agenda.tomarPorOtro(RAMOS, DIA);
        cita(VARGAS, PACIENTE, OTRO_CONSULTORIO, LocalTime.of(15, 0), EstadoCita.PROGRAMADA);

        assertEquals(ResultadoDisponibilidad.PACIENTE_YA_TIENE_CITA, service.validar(candidata(LocalTime.of(9, 0))));

        citas.cambiarEstado(citas.citas.get(citas.citas.size() - 1).citaId(), EstadoCita.PROGRAMADA,
                EstadoCita.CANCELADA);
        assertEquals(ResultadoDisponibilidad.MEDICO_COMPLETO, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void validar_medicoOcupado_antesQueConsultorioYSolapada() {
        cita(RAMOS, 200, CONSULTORIO_RAMOS, LocalTime.of(9, 0), EstadoCita.PROGRAMADA);
        agenda.tomarPorOtro(RAMOS, DIA);

        assertEquals(ResultadoDisponibilidad.MEDICO_OCUPADO, service.validar(candidata(LocalTime.of(9, 0))));
    }

    @Test
    void combinacion_conDuracionCero_esInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> new CombinacionCandidata(PACIENTE, RAMOS, CONSULTORIO_RAMOS, DIA, LocalTime.of(9, 0), 0));
    }

    private CombinacionCandidata candidata(LocalTime hora) {
        return new CombinacionCandidata(PACIENTE, RAMOS, CONSULTORIO_RAMOS, DIA, hora, DURACION);
    }

    /** 08:00, 08:30, ..., 11:00 en el consultorio de Ramos, con otros pacientes. */
    private void sieteCitasDeRamos() {
        for (int i = 0; i < 7; i++) {
            cita(RAMOS, 200 + i, CONSULTORIO_RAMOS, LocalTime.of(8, 0).plusMinutes(30L * i), EstadoCita.PROGRAMADA);
        }
    }

    private void cita(long medicoId, long pacienteId, int consultorio, LocalTime hora, EstadoCita estado) {
        TipoEspecialidad especialidad = medicoId == RAMOS ? TipoEspecialidad.CONSULTORIO_FAMILIAR
                : TipoEspecialidad.PEDIATRIA;
        citas.store(new CitaDTO(citas.nextIdentity(), pacienteId, "", "", medicoId, "", especialidad, consultorio, DIA,
                hora, 1, estado));
    }
}
