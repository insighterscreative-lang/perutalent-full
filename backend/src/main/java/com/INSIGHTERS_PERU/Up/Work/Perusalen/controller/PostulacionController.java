package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PostulacionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.FileStorageService;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.PostulacionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/postulaciones")
@AllArgsConstructor
public class PostulacionController {

    private final PostulacionService postulacionService;
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @PostMapping(
            value = "/ofertas/{idOferta}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> postular(
            @PathVariable Long idOferta,
            @RequestParam("usarCvPerfil") Boolean usarCvPerfil,
            @RequestParam(value = "cv", required = false) MultipartFile cv,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        postulacionService.postular(idOferta, idUsuario, usarCvPerfil, cv);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Postulación realizada exitosamente",
                        null
                ));
    }

    @GetMapping("/mis-ofertas")
    public ResponseEntity<ApiResponse<List<Long>>> listarMisOfertasPostuladas(
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        List<Long> idsOfertas = postulacionService.listarIdsOfertasPostuladas(idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Ofertas postuladas obtenidas exitosamente",
                        idsOfertas
                )
        );
    }

    @GetMapping("/ofertas/{idOferta}/postulantes")
    public ResponseEntity<ApiResponse<List<PostulacionResponseDTO>>> listarPostulantesPorOferta(
            @PathVariable Long idOferta,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        List<PostulacionResponseDTO> postulantes =
                postulacionService.listarPostulantesPorOferta(idOferta, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Postulantes obtenidos exitosamente",
                        postulantes
                )
        );
    }

    @PutMapping("/{idPostulacion}/aceptar")
    public ResponseEntity<ApiResponse<PostulacionResponseDTO>> aceptarPostulacion(
            @PathVariable Long idPostulacion,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        PostulacionResponseDTO postulacion =
                postulacionService.aceptarPostulacion(idPostulacion, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Postulación aceptada exitosamente",
                        postulacion
                )
        );
    }

    @PutMapping("/{idPostulacion}/rechazar")
    public ResponseEntity<ApiResponse<PostulacionResponseDTO>> rechazarPostulacion(
            @PathVariable Long idPostulacion,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        PostulacionResponseDTO postulacion =
                postulacionService.rechazarPostulacion(idPostulacion, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Postulación rechazada exitosamente",
                        postulacion
                )
        );
    }

    @GetMapping("/{idPostulacion}/cv")
    public ResponseEntity<Resource> verCvPostulacion(
            @PathVariable Long idPostulacion
    ) {
        var postulacion = postulacionService.obtenerPostulacionPorId(idPostulacion);

        if (postulacion.getCvUrl() == null || postulacion.getCvUrl().isBlank()) {
            throw new RuntimeException("Esta postulación no tiene CV registrado");
        }

        Resource archivo = fileStorageService.cargarArchivo(postulacion.getCvUrl());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cv.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }
}
