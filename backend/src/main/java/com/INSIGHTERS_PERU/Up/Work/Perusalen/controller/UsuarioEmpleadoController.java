package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoPublicoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.S3StorageService;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.UsuarioEmpleadoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/empleados")
@AllArgsConstructor
public class UsuarioEmpleadoController {

    private final UsuarioEmpleadoService usuarioEmpleadoService;
    private final S3StorageService s3StorageService;
    private final JwtUtil jwtUtil;

    @PostMapping(
            value = "/perfil",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<String>> crearPerfilJson(
            @Valid @RequestBody UsuarioEmpleadoRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadoService.crearPerfil(dto, idUsuario, null, null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Perfil de empleado creado exitosamente",
                        null
                ));
    }

    @PostMapping(
            value = "/perfil",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> crearPerfilMultipart(
            @Valid @RequestPart("perfil") UsuarioEmpleadoRequestDTO dto,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadoService.crearPerfil(dto, idUsuario, cv, fotoPerfil);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Perfil de empleado creado exitosamente",
                        null
                ));
    }

    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioEmpleadoResponseDTO>> obtenerPerfil(
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        UsuarioEmpleadoResponseDTO response =
                usuarioEmpleadoService.obtenerPerfil(idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil obtenido exitosamente",
                        response
                )
        );
    }

    @GetMapping("/perfil/cv")
    public ResponseEntity<?> verCvPerfil(
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        UsuarioEmpleado empleado = usuarioEmpleadoService.obtenerPerfilEntidad(idUsuario);

        if (empleado.getCurriculum() == null || empleado.getCurriculum().isBlank()) {
            throw new RuntimeException("Este perfil no tiene CV registrado");
        }

        if (s3StorageService.esUrlPublica(empleado.getCurriculum())) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, empleado.getCurriculum())
                    .build();
        }

        ResponseInputStream<GetObjectResponse> archivoS3 =
                s3StorageService.descargarArchivo(empleado.getCurriculum());

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

    @GetMapping("/perfil-publico/{idEmpleado}")
    public ResponseEntity<ApiResponse<UsuarioEmpleadoPublicoResponseDTO>> obtenerPerfilPublico(
            @PathVariable Long idEmpleado) {

        UsuarioEmpleadoPublicoResponseDTO response =
                usuarioEmpleadoService.obtenerPerfilPublico(idEmpleado);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil público obtenido exitosamente",
                        response
                )
        );
    }

    @GetMapping("/perfil-publico/{idEmpleado}/foto")
    public ResponseEntity<?> verFotoPerfilPublica(
            @PathVariable Long idEmpleado) {

        UsuarioEmpleado empleado = usuarioEmpleadoService.obtenerPerfilEntidadPorId(idEmpleado);

        if (empleado.getFotoPerfil() == null || empleado.getFotoPerfil().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        return responderArchivoImagen(empleado.getFotoPerfil(), "foto-perfil");
    }

    @PutMapping(
            value = "/perfil",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<String>> editarPerfilJson(
            @Valid @RequestBody UsuarioEmpleadoRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadoService.editarPerfil(dto, idUsuario, null, null);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleado actualizado exitosamente",
                        null
                )
        );
    }

    @PutMapping(
            value = "/perfil",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> editarPerfilMultipart(
            @Valid @RequestPart("perfil") UsuarioEmpleadoRequestDTO dto,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadoService.editarPerfil(dto, idUsuario, cv, fotoPerfil);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleado actualizado exitosamente",
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
