package com.INSIGHTERS_PERU.Up.Work.Perusalen.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class FechaPeruTest {

    @Test
    void usaLaFechaActualDeLaZonaHorariaDePeru() {
        assertEquals(
                LocalDate.now(ZoneId.of("America/Lima")),
                FechaPeru.hoy()
        );
    }

    @Test
    void consideraVencidaSoloUnaFechaAnteriorAHoy() {
        assertTrue(FechaPeru.estaVencida(FechaPeru.hoy().minusDays(1)));
        assertFalse(FechaPeru.estaVencida(FechaPeru.hoy()));
        assertFalse(FechaPeru.estaVencida(FechaPeru.hoy().plusDays(1)));
    }

    @Test
    void consideraVencidaUnaFechaLimiteAusente() {
        assertTrue(FechaPeru.estaVencida(null));
    }
}
