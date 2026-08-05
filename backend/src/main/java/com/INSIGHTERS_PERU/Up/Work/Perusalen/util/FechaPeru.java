package com.INSIGHTERS_PERU.Up.Work.Perusalen.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Centraliza la fecha de negocio de la plataforma en la zona horaria de Perú.
 * Render suele ejecutar en UTC, por lo que LocalDate.now() puede avanzar al día
 * siguiente desde las 7:00 p. m. en Perú.
 */
public final class FechaPeru {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private FechaPeru() {
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA_PERU);
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_PERU);
    }

    public static boolean estaVencida(LocalDate fechaLimite) {
        return fechaLimite == null || fechaLimite.isBefore(hoy());
    }
}
