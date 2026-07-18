package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.FiltrosResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.FiltrosService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/filtros/ofertas")
@AllArgsConstructor
public class FiltrosController {

    private final FiltrosService filtrosService;

    @GetMapping
    public ResponseEntity<FiltrosResponseDTO> getFiltros() {
        return ResponseEntity.ok(filtrosService.getFiltros());
    }
}
