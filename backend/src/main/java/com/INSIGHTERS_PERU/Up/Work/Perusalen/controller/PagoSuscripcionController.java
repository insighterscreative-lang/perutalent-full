package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoPremiumRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoSuscripcionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.PagoSuscripcionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pagos/culqi")
public class PagoSuscripcionController {

    private final PagoSuscripcionService pagoSuscripcionService;
    private final UsuarioRepository usuarioRepository;

    @Value("${CULQI_PUBLIC_KEY:}")
    private String culqiPublicKey;

    public PagoSuscripcionController(
            PagoSuscripcionService pagoSuscripcionService,
            UsuarioRepository usuarioRepository
    ) {
        this.pagoSuscripcionService = pagoSuscripcionService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> obtenerConfigPublica() {
        String publicKey = culqiPublicKey == null ? "" : culqiPublicKey;

        Map<String, Object> response = new HashMap<>();
        response.put("publicKey", publicKey);
        response.put("testMode", publicKey.startsWith("pk_test"));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/premium")
    public ResponseEntity<PagoSuscripcionResponseDTO> pagarPremium(
            @RequestBody PagoPremiumRequestDTO request
    ) {
        Long idUsuario = obtenerIdUsuarioAutenticado();

        return ResponseEntity.ok(
                pagoSuscripcionService.pagarPremium(idUsuario, request)
        );
    }

    private Long obtenerIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado.");
        }

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));

        return usuario.getId();
    }
}