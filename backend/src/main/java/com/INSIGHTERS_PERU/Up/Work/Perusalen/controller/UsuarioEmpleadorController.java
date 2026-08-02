package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.S3StorageService;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.UsuarioEmpleadorService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/empleadores")
@AllArgsConstructor
public class UsuarioEmpleadorController {

    private final UsuarioEmpleadorService usuarioEmpleadorService;
    private final S3StorageService s3StorageService;
    private final JwtUtil jwtUtil;

    @PostMapping(
            value = "/perfil",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<String>> crearPerfilJson(
            @Valid @RequestBody UsuarioEmpleadorRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadorService.crearPerfil(dto, idUsuario, null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Perfil de empleador creado exitosamente",
                        null
                ));
    }

    @PostMapping(
            value = "/perfil",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> crearPerfilMultipart(
            @Valid @RequestPart("perfil") UsuarioEmpleadorRequestDTO dto,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadorService.crearPerfil(dto, idUsuario, logo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Perfil de empleador creado exitosamente",
                        null
                ));
    }

    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioEmpleadorResponseDTO>> obtenerPerfil(
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        UsuarioEmpleadorResponseDTO response =
                usuarioEmpleadorService.obtenerPerfil(idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleador obtenido exitosamente",
                        response
                )
        );
    }


    @GetMapping("/perfil-publico/{idEmpleador}")
    public ResponseEntity<ApiResponse<UsuarioEmpleadorResponseDTO>> obtenerPerfilPublico(
            @PathVariable Long idEmpleador) {

        UsuarioEmpleadorResponseDTO response =
                usuarioEmpleadorService.obtenerPerfilPublico(idEmpleador);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil público de empleador obtenido exitosamente",
                        response
                )
        );
    }

    @GetMapping("/perfil-publico/{idEmpleador}/logo")
    public ResponseEntity<?> verLogoPublico(
            @PathVariable Long idEmpleador) {

        UsuarioEmpleador empleador = usuarioEmpleadorService.obtenerPerfilEntidadPorId(idEmpleador);

        if (empleador.getLogoEmpleador() == null || empleador.getLogoEmpleador().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        return responderArchivoImagen(empleador.getLogoEmpleador(), "logo-empleador");
    }

    @PutMapping(
            value = "/perfil",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<String>> editarPerfilJson(
            @Valid @RequestBody UsuarioEmpleadorRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadorService.editarPerfil(dto, idUsuario, null);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleador actualizado exitosamente",
                        null
                )
        );
    }

    @PutMapping(
            value = "/perfil",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> editarPerfilMultipart(
            @Valid @RequestPart("perfil") UsuarioEmpleadorRequestDTO dto,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadorService.editarPerfil(dto, idUsuario, logo);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleador actualizado exitosamente",
                        null
                )
        );
    }

    private ResponseEntity<?> responderArchivoImagen(String key, String nombreArchivo) {
        if (s3StorageService.esUrlPublica(key)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, key)
                    .build();
        }

        ResponseInputStream<GetObjectResponse> archivoS3 =
                s3StorageService.descargarArchivo(key);

        GetObjectResponse respuestaS3 = archivoS3.response();

        String contentType = respuestaS3.contentType() != null
                ? respuestaS3.contentType()
                : MediaType.IMAGE_JPEG_VALUE;

        Long contentLength = respuestaS3.contentLength();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType(contentType));

        if (contentLength != null && contentLength > 0) {
            responseBuilder.contentLength(contentLength);
        }

        return responseBuilder.body(new InputStreamResource(archivoS3));
    }
}
