package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReclamoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReclamoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.ReclamoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reclamos")
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReclamoResponseDTO>> registrar(
            @Valid @RequestBody ReclamoRequestDTO request
    ) {
        ReclamoResponseDTO respuesta = reclamoService.registrar(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Tu reclamo o queja fue registrado correctamente",
                        respuesta
                ));
    }
}
