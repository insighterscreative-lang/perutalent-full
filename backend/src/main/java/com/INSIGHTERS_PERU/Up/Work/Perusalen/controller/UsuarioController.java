package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.AuthResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioLoginRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioRegisterRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.UsuarioService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<Long>> registrar(@Valid @RequestBody UsuarioRegisterRequestDTO dto) {
        Long id = usuarioService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("Usuario registrado exitosamente", id));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody UsuarioLoginRequestDTO loginRequest) {
        AuthResponseDTO response = usuarioService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Usuario logueado exitosamente", response));
    }
}
