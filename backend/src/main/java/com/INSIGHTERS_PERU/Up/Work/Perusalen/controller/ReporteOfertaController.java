package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteOfertaRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteOfertaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.ReporteOfertaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reportes/ofertas")
public class ReporteOfertaController {

    private final ReporteOfertaService reporteOfertaService;
    private final JwtUtil jwtUtil;

    public ReporteOfertaController(
            ReporteOfertaService reporteOfertaService,
            JwtUtil jwtUtil
    ) {
        this.reporteOfertaService = reporteOfertaService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/{idOferta}")
    public ResponseEntity<ApiResponse<ReporteOfertaResponseDTO>> reportar(
            @PathVariable Long idOferta,
            @Valid @RequestBody ReporteOfertaRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(httpRequest);
        ReporteOfertaResponseDTO respuesta = reporteOfertaService.reportar(
                idOferta,
                idUsuario,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Reporte enviado correctamente. El equipo de PeruTalent lo revisará.",
                        respuesta
                ));
    }
}
