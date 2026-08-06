package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CulqiServiceValidacionTest {

    private CulqiService culqiService;

    @BeforeEach
    void preparar() {
        culqiService = new CulqiService(new ObjectMapper());
        ReflectionTestUtils.setField(
            culqiService,
            "culqiSecretKey",
            "sk_" + "live_" + "llaveFicticiaSoloParaPruebas123456"
    );
        ReflectionTestUtils.setField(
                culqiService,
                "culqiApiUrl",
                "https://api.culqi.com/v2"
        );
    }

    @Test
    void aceptaPlanYTokenDelMismoAmbienteLive() {
        assertDoesNotThrow(() -> culqiService.validarAmbientePlanYToken(
                "pln_live_planFicticio123",
                "tkn_live_tokenFicticio123"
        ));
    }

    @Test
    void rechazaPlanTestConLlaveLive() {
        assertThrows(RuntimeException.class, () -> culqiService.validarAmbientePlanYToken(
                "pln_test_planFicticio123",
                "tkn_live_tokenFicticio123"
        ));
    }

    @Test
    void validaSuscripcionActivaDelPlanEsperado() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", "sxn_live_suscripcionFicticia123");
        respuesta.put("status", 3);
        respuesta.put("plan_id", "pln_live_planFicticio123");

        assertTrue(culqiService.suscripcionActivaYCorrespondeAlPlan(
                respuesta,
                "pln_live_planFicticio123"
        ));
        assertFalse(culqiService.suscripcionActivaYCorrespondeAlPlan(
                respuesta,
                "pln_live_otroPlan123"
        ));
    }

    @Test
    void ocultaDatosSensiblesAntesDePersistirRespuesta() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", "sxn_live_suscripcionFicticia123");
        respuesta.put("token_id", "tkn_live_noDebeGuardarse");
        respuesta.put("email", "persona@correo.com");

        String json = culqiService.convertirRespuestaAJson(respuesta);

        assertTrue(json.contains("sxn_live_suscripcionFicticia123"));
        assertFalse(json.contains("tkn_live_noDebeGuardarse"));
        assertFalse(json.contains("persona@correo.com"));
    }
}
