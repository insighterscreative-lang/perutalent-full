package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.PagoSuscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoSuscripcionControllerTest {

    @Mock private PagoSuscripcionService pagoSuscripcionService;
    @Mock private UsuarioRepository usuarioRepository;

    private PagoSuscripcionController controller;

    @BeforeEach
    void preparar() {
        controller = new PagoSuscripcionController(pagoSuscripcionService, usuarioRepository);
        ReflectionTestUtils.setField(controller, "culqiEnabled", true);
        ReflectionTestUtils.setField(controller, "culqiPublicKey", "pk_live_llavePublicaFicticia123");
        ReflectionTestUtils.setField(controller, "culqiWebhookToken", "token-webhook-seguro");
    }

    @Test
    void rechazaWebhookSinTokenCorrecto() {
        ResponseEntity<Map<String, Object>> response = controller.recibirWebhookCulqi(
                "{\"id\":\"evt_1\",\"type\":\"subscription.created\"}",
                "token-incorrecto",
                null
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(pagoSuscripcionService, never()).procesarWebhookCulqi(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void aceptaWebhookConTokenCorrecto() {
        String payload = "{\"id\":\"evt_1\",\"type\":\"subscription.created\"}";
        when(pagoSuscripcionService.procesarWebhookCulqi(payload))
                .thenReturn(Map.of("procesado", true));

        ResponseEntity<Map<String, Object>> response = controller.recibirWebhookCulqi(
                payload,
                "token-webhook-seguro",
                null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.TRUE.equals(response.getBody().get("procesado")));
    }

    @Test
    void configuracionPublicaNoExponeLlavePrivada() {
        ResponseEntity<Map<String, Object>> response = controller.obtenerConfigPublica();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pk_live_llavePublicaFicticia123", response.getBody().get("publicKey"));
        assertEquals(false, response.getBody().get("testMode"));
        assertEquals(true, response.getBody().get("enabled"));
    }
}
