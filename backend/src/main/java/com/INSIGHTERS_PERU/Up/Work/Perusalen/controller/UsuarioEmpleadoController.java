package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.UsuarioEmpleadoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/empleados")
@AllArgsConstructor
public class UsuarioEmpleadoController {

    private final UsuarioEmpleadoService usuarioEmpleadoService;
    private final JwtUtil jwtUtil;

    @PostMapping("/perfil")
    public ResponseEntity<ApiResponse<String>> crearPerfil(
            @Valid @RequestBody UsuarioEmpleadoRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadoService.crearPerfil(dto, idUsuario);

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

    @GetMapping("/perfil-publico/{idEmpleado}")
    public ResponseEntity<ApiResponse<UsuarioEmpleadoResponseDTO>> obtenerPerfilPublico(
            @PathVariable Long idEmpleado) {

        UsuarioEmpleadoResponseDTO response =
                usuarioEmpleadoService.obtenerPerfilPublico(idEmpleado);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil público obtenido exitosamente",
                        response
                )
        );
    }

    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<String>> editarPerfil(
            @Valid @RequestBody UsuarioEmpleadoRequestDTO dto,
            HttpServletRequest request) {

        Long idUsuario = jwtUtil.getIdFromRequest(request);

        usuarioEmpleadoService.editarPerfil(dto, idUsuario);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Perfil de empleado actualizado exitosamente",
                        null
                )
        );
    }
}
