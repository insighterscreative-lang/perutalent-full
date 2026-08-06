package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoPremiumRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoSuscripcionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.PagoSuscripcionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/pagos/culqi")
public class PagoSuscripcionController {

    private final PagoSuscripcionService pagoSuscripcionService;
    private final UsuarioRepository usuarioRepository;

    @Value("${culqi.public-key:}")
    private String culqiPublicKey;

    @Value("${culqi.enabled:false}")
    private boolean culqiEnabled;

    @Value("${culqi.webhook-token:}")
    private String culqiWebhookToken;

    public PagoSuscripcionController(
            PagoSuscripcionService pagoSuscripcionService,
            UsuarioRepository usuarioRepository
    ) {
        this.pagoSuscripcionService = pagoSuscripcionService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> obtenerConfigPublica() {
        String publicKey = culqiPublicKey == null ? "" : culqiPublicKey.trim();
        boolean llaveValida = publicKey.startsWith("pk_test_") || publicKey.startsWith("pk_live_");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("publicKey", llaveValida ? publicKey : "");
        response.put("testMode", publicKey.startsWith("pk_test_"));
        response.put("enabled", culqiEnabled && llaveValida);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/premium")
    public ResponseEntity<PagoSuscripcionResponseDTO> pagarPremium(
            @RequestBody PagoPremiumRequestDTO request
    ) {
        Long idUsuario = obtenerIdUsuarioAutenticado();
        return ResponseEntity.ok(pagoSuscripcionService.pagarPremium(idUsuario, request));
    }

    @PostMapping("/premium/cancelar")
    public ResponseEntity<MiSuscripcionDTO> cancelarPremium() {
        Long idUsuario = obtenerIdUsuarioAutenticado();
        return ResponseEntity.ok(pagoSuscripcionService.cancelarSuscripcionPremium(idUsuario));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> recibirWebhookCulqi(
            @RequestBody(required = false) String payload,
            @RequestParam(value = "token", required = false) String tokenQuery,
            @RequestHeader(value = "X-Culqi-Webhook-Token", required = false) String tokenHeader
    ) {
        if (!culqiEnabled) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("mensaje", "La integración de Culqi está deshabilitada."));
        }

        if (culqiWebhookToken == null || culqiWebhookToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("mensaje", "El webhook de Culqi no está configurado."));
        }

        String tokenRecibido = tokenHeader != null && !tokenHeader.isBlank()
                ? tokenHeader
                : tokenQuery;

        if (!tokenSeguroValido(tokenRecibido, culqiWebhookToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Webhook no autorizado."));
        }

        try {
            return ResponseEntity.ok(
                    pagoSuscripcionService.procesarWebhookCulqi(payload == null ? "" : payload)
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    private boolean tokenSeguroValido(String recibido, String esperado) {
        if (recibido == null || recibido.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                recibido.getBytes(StandardCharsets.UTF_8),
                esperado.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Long obtenerIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));

        return usuario.getId();
    }
}
