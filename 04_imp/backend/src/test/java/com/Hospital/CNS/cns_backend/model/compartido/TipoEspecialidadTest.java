package com.Hospital.CNS.cns_backend.model.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class TipoEspecialidadTest {

    @Test
    void lasTresBasicasNoCambian() {
        assertEquals(30, TipoEspecialidad.CONSULTORIO_FAMILIAR.duracionMinMinutos());
        assertEquals(45, TipoEspecialidad.CONSULTORIO_FAMILIAR.duracionMaxMinutos());
        assertEquals(45, TipoEspecialidad.ODONTOLOGIA.duracionMinMinutos());
        assertEquals(60, TipoEspecialidad.ODONTOLOGIA.duracionMaxMinutos());
        assertEquals(30, TipoEspecialidad.PEDIATRIA.duracionMinMinutos());
        assertEquals(45, TipoEspecialidad.PEDIATRIA.duracionMaxMinutos());
        assertEquals(3, Arrays.stream(TipoEspecialidad.values()).filter(TipoEspecialidad::esBasica).count());
        assertEquals(10, Arrays.stream(TipoEspecialidad.values()).filter(TipoEspecialidad::esInterna).count());
    }

    @Test
    void soloMedicoGeneralYPediatriaDerivan() {
        List<TipoEspecialidad> derivan = Arrays.stream(TipoEspecialidad.values())
                .filter(TipoEspecialidad::puedeDerivar).toList();
        assertEquals(List.of(TipoEspecialidad.CONSULTORIO_FAMILIAR, TipoEspecialidad.PEDIATRIA), derivan);
        assertFalse(TipoEspecialidad.ODONTOLOGIA.puedeDerivar());
    }

    @Test
    void terapiaIntensivaSoloUrgencia() {
        assertTrue(TipoEspecialidad.TERAPIA_INTENSIVA.soloUrgencia());
        assertEquals(1, Arrays.stream(TipoEspecialidad.values()).filter(TipoEspecialidad::soloUrgencia).count());
    }

    @Test
    void operanSoloGastroHematoYDerma() {
        List<TipoEspecialidad> operan = Arrays.stream(TipoEspecialidad.values())
                .filter(TipoEspecialidad::operaEnQuirofano).toList();
        assertEquals(List.of(TipoEspecialidad.GASTROENTEROLOGIA, TipoEspecialidad.HEMATOLOGIA,
                TipoEspecialidad.DERMATOLOGIA), operan);
        assertEquals(15, TipoEspecialidad.DERMATOLOGIA.cirugiaMinMinutos());
        assertEquals(120, TipoEspecialidad.HEMATOLOGIA.cirugiaMaxMinutos());
        assertEquals(0, TipoEspecialidad.INFECTOLOGIA.cirugiaMaxMinutos());
    }
}
