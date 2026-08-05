package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
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
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.FiltrosPostulantesResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiPostulacionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PaginaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PostulacionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.PostulacionService;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.S3StorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/postulaciones")
@AllArgsConstructor
public class PostulacionController {

    private final PostulacionService postulacionService;
    private final S3StorageService s3StorageService;
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

    @GetMapping("/mis-postulaciones")
    public ResponseEntity<ApiResponse<PaginaResponseDTO<MiPostulacionResponseDTO>>> listarMisPostulaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        PaginaResponseDTO<MiPostulacionResponseDTO> postulaciones =
                postulacionService.listarMisPostulaciones(idUsuario, page, size);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Postulaciones obtenidas exitosamente",
                        postulaciones
                )
        );
    }

    @GetMapping("/ofertas/{idOferta}/postulantes")
    public ResponseEntity<ApiResponse<PaginaResponseDTO<PostulacionResponseDTO>>> listarPostulantesPorOferta(
            @PathVariable Long idOferta,
            @RequestParam(defaultValue = "TODOS") String estado,
            @RequestParam(defaultValue = "") String texto,
            @RequestParam(required = false) Long distritoId,
            @RequestParam(required = false) Long modalidadId,
            @RequestParam(required = false) Long habilidadId,
            @RequestParam(required = false) Long herramientaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        PaginaResponseDTO<PostulacionResponseDTO> postulantes =
                postulacionService.listarPostulantesPorOfertaPaginados(
                        idOferta,
                        idUsuario,
                        estado,
                        texto,
                        distritoId,
                        modalidadId,
                        habilidadId,
                        herramientaId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Postulantes obtenidos exitosamente",
                        postulantes
                )
        );
    }

    @GetMapping("/ofertas/{idOferta}/postulantes/filtros")
    public ResponseEntity<ApiResponse<FiltrosPostulantesResponseDTO>> listarFiltrosPostulantes(
            @PathVariable Long idOferta,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        FiltrosPostulantesResponseDTO filtros =
                postulacionService.listarFiltrosPostulantes(idOferta, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>("Filtros de postulantes obtenidos exitosamente", filtros)
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
                        "Postulación preseleccionada exitosamente",
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
    public ResponseEntity<?> verCvPostulacion(
            @PathVariable Long idPostulacion,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);

        var postulacion = postulacionService.obtenerPostulacionPorIdAutorizada(
                idPostulacion,
                idUsuario
        );

        if (postulacion.getCvUrl() == null || postulacion.getCvUrl().isBlank()) {
            throw new RuntimeException("Esta postulación no tiene CV registrado");
        }

        if (s3StorageService.esUrlPublica(postulacion.getCvUrl())) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, postulacion.getCvUrl())
                    .build();
        }

        ResponseInputStream<GetObjectResponse> archivoS3 =
                s3StorageService.descargarArchivo(postulacion.getCvUrl());

        GetObjectResponse respuestaS3 = archivoS3.response();

        String contentType = respuestaS3.contentType() != null
                ? respuestaS3.contentType()
                : MediaType.APPLICATION_PDF_VALUE;

        Long contentLength = respuestaS3.contentLength();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cv.pdf\"")
                .contentType(MediaType.parseMediaType(contentType));

        if (contentLength != null && contentLength > 0) {
            responseBuilder.contentLength(contentLength);
        }

        return responseBuilder.body(new InputStreamResource(archivoS3));
    }
}