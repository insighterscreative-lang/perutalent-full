package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.UsuarioEmpleadorService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/empleadores")
@AllArgsConstructor
public class UsuarioEmpleadorController {

    private final UsuarioEmpleadorService usuarioEmpleadorService;
    private final JwtUtil jwtUtil;

    @PostMapping("/perfil")
    public ResponseEntity<ApiResponse<String>> crearPerfil(
            @Valid @RequestBody UsuarioEmpleadorRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadorService.crearPerfil(dto, idUsuario);

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

    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<String>> editarPerfil(
            @Valid @RequestBody UsuarioEmpleadorRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadorService.editarPerfil(dto, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleador actualizado exitosamente",
                        null
                )
        );
    }
}
