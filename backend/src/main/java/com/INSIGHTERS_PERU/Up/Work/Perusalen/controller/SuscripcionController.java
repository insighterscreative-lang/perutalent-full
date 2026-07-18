package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PlanSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsoPlanUsuarioDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.SuscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;
    private final UsuarioRepository usuarioRepository;

    public SuscripcionController(SuscripcionService suscripcionService,
                                 UsuarioRepository usuarioRepository) {
        this.suscripcionService = suscripcionService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/planes")
    public ResponseEntity<List<PlanSuscripcionDTO>> listarPlanes() {
        return ResponseEntity.ok(suscripcionService.listarPlanesActivos());
    }

    @GetMapping("/mi-suscripcion")
    public ResponseEntity<MiSuscripcionDTO> obtenerMiSuscripcion() {
        Long idUsuario = obtenerIdUsuarioAutenticado();
        return ResponseEntity.ok(suscripcionService.obtenerMiSuscripcion(idUsuario));
    }

    @GetMapping("/mi-uso")
    public ResponseEntity<UsoPlanUsuarioDTO> obtenerMiUso() {
        Long idUsuario = obtenerIdUsuarioAutenticado();
        return ResponseEntity.ok(suscripcionService.obtenerMiUso(idUsuario));
    }

    @PostMapping("/cambiar-plan/{idPlan}")
    public ResponseEntity<MiSuscripcionDTO> cambiarPlan(@PathVariable Long idPlan) {
        Long idUsuario = obtenerIdUsuarioAutenticado();
        return ResponseEntity.ok(suscripcionService.cambiarPlan(idUsuario, idPlan));
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