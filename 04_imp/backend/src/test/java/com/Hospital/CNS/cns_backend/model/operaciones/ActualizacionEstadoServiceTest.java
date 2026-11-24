package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.*;
import com.Hospital.CNS.cns_backend.controller.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActualizacionEstadoServiceTest {

    private CitaRepository citaRepository;
    private CambioCitaRepository cambioRepository;
    private AgendaEventos agendaEventos;
    private Reloj reloj;
    private ActualizacionEstadoService service;

    @BeforeEach
    void setUp() {
        citaRepository = mock(CitaRepository.class);
        cambioRepository = mock(CambioCitaRepository.class);
        agendaEventos = mock(AgendaEventos.class);
        reloj = mock(Reloj.class);
        service = new ActualizacionEstadoService(citaRepository, cambioRepository, agendaEventos, reloj);
    }

    @Test
    void confirmar_aT31min_ok() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        LocalTime horaCita = LocalTime.of(10, 0);
        LocalDateTime relojFijo = hoy.atTime(9, 29); // 31 minutos antes (OK)

        when(reloj.ahora()).thenReturn(relojFijo);
        
        CitaDTO cita = new CitaDTO(
            1L, 10L, "Paciente Test", "123", 
            100L, "Dr Test", null, 1, 
            hoy, horaCita, 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.porId(1L)).thenReturn(Optional.of(cita));
        when(citaRepository.cambiarEstado(1L, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA)).thenReturn(true);

        UsuarioAutenticado enfermera = mock(UsuarioAutenticado.class);
        when(enfermera.rol()).thenReturn(RolUsuario.ENFERMERIA);

        ResultadoConfirmacion res = service.confirmarAsistencia(1L, enfermera);

        assertEquals(ResultadoConfirmacion.CONFIRMADA, res);
    }

    @Test
    void confirmar_aT29min_fueraDePlazo() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        LocalTime horaCita = LocalTime.of(10, 0);
        LocalDateTime relojFijo = hoy.atTime(9, 31); // 29 minutos antes (Fuera de plazo)

        when(reloj.ahora()).thenReturn(relojFijo);
        
        CitaDTO cita = new CitaDTO(
            1L, 10L, "Paciente Test", "123", 
            100L, "Dr Test", null, 1, 
            hoy, horaCita, 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.porId(1L)).thenReturn(Optional.of(cita));

        UsuarioAutenticado enfermera = mock(UsuarioAutenticado.class);
        when(enfermera.rol()).thenReturn(RolUsuario.ENFERMERIA);

        ResultadoConfirmacion res = service.confirmarAsistencia(1L, enfermera);

        assertEquals(ResultadoConfirmacion.FUERA_DE_PLAZO, res);
    }

    @Test
    void confirmar_citaDeOtroDia_rechaza() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        LocalDate otroDia = LocalDate.of(2026, 11, 15);
        LocalDateTime relojFijo = hoy.atTime(9, 0);

        when(reloj.ahora()).thenReturn(relojFijo);
        
        CitaDTO cita = new CitaDTO(
            1L, 10L, "Paciente Test", "123", 
            100L, "Dr Test", null, 1, 
            otroDia, LocalTime.of(10, 0), 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.porId(1L)).thenReturn(Optional.of(cita));

        UsuarioAutenticado enfermera = mock(UsuarioAutenticado.class);
        when(enfermera.rol()).thenReturn(RolUsuario.ENFERMERIA);

        ResultadoConfirmacion res = service.confirmarAsistencia(1L, enfermera);

        assertEquals(ResultadoConfirmacion.NO_ES_DE_HOY, res);
    }

    @Test
    void confirmar_rolMedico_rechaza() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        LocalDateTime relojFijo = hoy.atTime(9, 0);

        when(reloj.ahora()).thenReturn(relojFijo);
        
        CitaDTO cita = new CitaDTO(
            1L, 10L, "Paciente Test", "123", 
            100L, "Dr Test", null, 1, 
            hoy, LocalTime.of(10, 0), 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.porId(1L)).thenReturn(Optional.of(cita));

        UsuarioAutenticado medico = mock(UsuarioAutenticado.class);
        when(medico.rol()).thenReturn(RolUsuario.MEDICO);

        ResultadoConfirmacion res = service.confirmarAsistencia(1L, medico);

        assertEquals(ResultadoConfirmacion.ROL_NO_AUTORIZADO, res);
    }

    @Test
    void finalizar_desdeConfirmada_atendida() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        
        CitaDTO cita = new CitaDTO(
            1L, 10L, "Paciente Test", "123", 
            100L, "Dr Test", null, 1, 
            hoy, LocalTime.of(10, 0), 1, EstadoCita.ASISTENCIA_CONFIRMADA
        );
        
        when(citaRepository.porId(1L)).thenReturn(Optional.of(cita));
        when(citaRepository.cambiarEstado(1L, EstadoCita.ASISTENCIA_CONFIRMADA, EstadoCita.ATENDIDA)).thenReturn(true);

        ResultadoFinalizacion res = service.finalizar(1L, 100L);

        assertEquals(ResultadoFinalizacion.FINALIZADA, res);
    }

    @Test
    void finalizar_desdeProgramada_rechaza() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        
        CitaDTO cita = new CitaDTO(
            1L, 10L, "Paciente Test", "123", 
            100L, "Dr Test", null, 1, 
            hoy, LocalTime.of(10, 0), 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.porId(1L)).thenReturn(Optional.of(cita));

        ResultadoFinalizacion res = service.finalizar(1L, 100L);

        assertEquals(ResultadoFinalizacion.ESTADO_NO_PERMITE, res);
    }
}