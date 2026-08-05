package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordResetCodeHasherTest {

    private final PasswordResetCodeHasher hasher =
            new PasswordResetCodeHasher("secreto-largo-exclusivo-de-pruebas-123456789");

    @Test
    void generaHashDeterministaYNoGuardaElCodigoPlano() {
        String hash1 = hasher.hash("persona@correo.com", "123456");
        String hash2 = hasher.hash("persona@correo.com", "123456");

        assertTrue(hash1.equals(hash2));
        assertNotEquals("123456", hash1);
    }

    @Test
    void soloCoincideConCorreoYCodigoCorrectos() {
        String hash = hasher.hash("persona@correo.com", "123456");

        assertTrue(hasher.matches("persona@correo.com", "123456", hash));
        assertFalse(hasher.matches("persona@correo.com", "654321", hash));
        assertFalse(hasher.matches("otra@correo.com", "123456", hash));
        assertFalse(hasher.matches("persona@correo.com", "123456", null));
    }
}
