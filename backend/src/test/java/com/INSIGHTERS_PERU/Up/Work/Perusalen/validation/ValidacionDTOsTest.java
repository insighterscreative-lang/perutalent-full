package com.INSIGHTERS_PERU.Up.Work.Perusalen.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.RestablecerPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioRegisterRequestDTO;

class ValidacionDTOsTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    @Test
    void registroAceptaPasswordSeguraYCorreoValido() {
        UsuarioRegisterRequestDTO dto = new UsuarioRegisterRequestDTO(
                "persona@correo.com", "Prueba123!", true, false, true
        );
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void registroRechazaPasswordDebilYCorreoInvalido() {
        UsuarioRegisterRequestDTO dto = new UsuarioRegisterRequestDTO(
                "correo-invalido", "abcdef12", true, false, true
        );
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void restablecimientoExigeCodigoDeSeisDigitosYPasswordSegura() {
        RestablecerPasswordRequestDTO valido = new RestablecerPasswordRequestDTO(
                "persona@correo.com", "123456", "Nueva123!", "Nueva123!"
        );
        RestablecerPasswordRequestDTO invalido = new RestablecerPasswordRequestDTO(
                "persona@correo.com", "123", "sinseguridad", "sinseguridad"
        );

        assertTrue(validator.validate(valido).isEmpty());
        assertFalse(validator.validate(invalido).isEmpty());
    }
}
