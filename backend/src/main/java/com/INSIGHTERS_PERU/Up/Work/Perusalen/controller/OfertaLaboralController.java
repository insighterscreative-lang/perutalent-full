package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PaginaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.OfertaLaboralService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/ofertas-laborales")
@AllArgsConstructor
public class OfertaLaboralController {

    private final OfertaLaboralService ofertaLaboralService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<OfertaLaboralResponseDTO>> crearOferta(
            @Valid @RequestBody OfertaLaboralRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        OfertaLaboralResponseDTO ofertaCreada = ofertaLaboralService.crearOferta(dto, idUsuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Oferta laboral creada exitosamente",
                        ofertaCreada
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OfertaLaboralResponseDTO>> editarOferta(
            @PathVariable Long id,
            @Valid @RequestBody OfertaLaboralRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        OfertaLaboralResponseDTO ofertaEditada =
                ofertaLaboralService.editarOferta(id, dto, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Oferta laboral actualizada exitosamente",
                        ofertaEditada
                )
        );
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ApiResponse<OfertaLaboralResponseDTO>> finalizarOferta(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        OfertaLaboralResponseDTO oferta =
                ofertaLaboralService.finalizarOferta(id, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Oferta laboral finalizada exitosamente",
                        oferta
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarOferta(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        ofertaLaboralService.eliminarOferta(id, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Oferta laboral eliminada exitosamente",
                        null
                )
        );
    }

    @GetMapping("/paginadas")
    public ResponseEntity<PaginaResponseDTO<OfertaLaboralResponseDTO>> getOfertasLaboralesActivasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(
                ofertaLaboralService.getOfertasLaboralesActivasPaginadas(page, size)
        );
    }

    @GetMapping("/mias")
    public ResponseEntity<ApiResponse<PaginaResponseDTO<OfertaLaboralResponseDTO>>> getMisOfertas(
            @RequestParam(defaultValue = "ACTIVAS") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        PaginaResponseDTO<OfertaLaboralResponseDTO> pagina =
                ofertaLaboralService.listarMisOfertasPaginadas(
                        idUsuario,
                        estado,
                        page,
                        size
                );

        return ResponseEntity.ok(
                new ApiResponse<>("Ofertas del empleador obtenidas exitosamente", pagina)
        );
    }

    @GetMapping
    public ResponseEntity<List<OfertaLaboralResponseDTO>> getOfertasLaboralesActivas() {
        List<OfertaLaboralResponseDTO> ofertas =
                ofertaLaboralService.getOfertasLaboralesActivas();

        return ResponseEntity.ok(ofertas);
    }

    @GetMapping("/filtrar/paginadas")
    public ResponseEntity<PaginaResponseDTO<OfertaLaboralResponseDTO>> filtrarPaginadas(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Long modalidad,
            @RequestParam(required = false) Long experiencia,
            @RequestParam(required = false) BigDecimal montoMin,
            @RequestParam(required = false) BigDecimal montoMax,
            @RequestParam(required = false) String palabraClave,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(
                ofertaLaboralService.filtrarOfertasPaginadas(
                        categoria,
                        modalidad,
                        experiencia,
                        montoMin,
                        montoMax,
                        palabraClave,
                        ubicacion,
                        sortBy,
                        order,
                        page,
                        size
                )
        );
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<OfertaLaboralResponseDTO>> filtrar(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Long modalidad,
            @RequestParam(required = false) Long experiencia,
            @RequestParam(required = false) BigDecimal montoMin,
            @RequestParam(required = false) BigDecimal montoMax,
            @RequestParam(required = false) String palabraClave,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {

        return ResponseEntity.ok(
                ofertaLaboralService.filtrarOfertas(
                        categoria,
                        modalidad,
                        experiencia,
                        montoMin,
                        montoMax,
                        palabraClave,
                        ubicacion,
                        sortBy,
                        order
                )
        );
    }

    @GetMapping("/para-ti")
    public ResponseEntity<ApiResponse<List<OfertaLaboralResponseDTO>>> getOfertasParaTi(
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        List<OfertaLaboralResponseDTO> ofertas =
                ofertaLaboralService.getOfertasParaTi(idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Ofertas para ti obtenidas exitosamente",
                        ofertas
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaLaboralResponseDTO> getOfertaLaboralById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ofertaLaboralService.getOfertaById(id));
    }
}
