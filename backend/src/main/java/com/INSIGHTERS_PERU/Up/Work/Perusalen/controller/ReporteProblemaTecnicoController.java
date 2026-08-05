package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteProblemaTecnicoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteProblemaTecnicoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.ReporteProblemaTecnicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reportes-problemas")
public class ReporteProblemaTecnicoController {

    private final ReporteProblemaTecnicoService service;

    public ReporteProblemaTecnicoController(ReporteProblemaTecnicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReporteProblemaTecnicoResponseDTO>> registrar(
            @Valid @RequestBody ReporteProblemaTecnicoRequestDTO request
    ) {
        ReporteProblemaTecnicoResponseDTO respuesta = service.registrar(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "El problema técnico fue registrado correctamente",
                        respuesta
                ));
    }
}
