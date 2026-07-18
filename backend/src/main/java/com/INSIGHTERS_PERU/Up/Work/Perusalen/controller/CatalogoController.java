package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.CatalogoDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.CatalogoService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/catalogos")
@AllArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/departamentos")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarDepartamentos() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Departamentos obtenidos exitosamente",
                        catalogoService.listarDepartamentos()
                )
        );
    }

    @GetMapping("/provincias")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarProvinciasPorDepartamento(
            @RequestParam Long departamentoId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Provincias obtenidas exitosamente",
                        catalogoService.listarProvinciasPorDepartamento(departamentoId)
                )
        );
    }

    @GetMapping("/distritos")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarDistritosPorProvincia(
            @RequestParam Long provinciaId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Distritos obtenidos exitosamente",
                        catalogoService.listarDistritosPorProvincia(provinciaId)
                )
        );
    }

    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarCategorias() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Categorías obtenidas exitosamente",
                        catalogoService.listarCategorias()
                )
        );
    }

    @GetMapping("/habilidades")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarHabilidades() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Habilidades obtenidas exitosamente",
                        catalogoService.listarHabilidades()
                )
        );
    }

    @GetMapping("/herramientas")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarHerramientas() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Herramientas obtenidas exitosamente",
                        catalogoService.listarHerramientas()
                )
        );
    }

    @GetMapping("/modalidades")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarModalidades() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Modalidades obtenidas exitosamente",
                        catalogoService.listarModalidades()
                )
        );
    }

    @GetMapping("/tipos-duracion")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarTiposDuracion() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Tipos de duración obtenidos exitosamente",
                        catalogoService.listarTiposDuracion()
                )
        );
    }

    @GetMapping("/experiencias")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> listarExperiencias() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Experiencias obtenidas exitosamente",
                        catalogoService.listarExperiencias()
                )
        );
    }
}
