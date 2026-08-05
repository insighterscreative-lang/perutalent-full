package com.INSIGHTERS_PERU.Up.Work.Perusalen.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ActualizarEmailRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ActualizarPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.AuthResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.EliminarCuentaRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.RestablecerPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SolicitarRecuperacionPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioCuentaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioLoginRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioRegisterRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.VerificarCodigoRecuperacionPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.response.ApiResponse;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.service.UsuarioService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

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

    @PostMapping("/password/solicitar-codigo")
    public ResponseEntity<ApiResponse<Void>> solicitarCodigoRecuperacion(
            @Valid @RequestBody SolicitarRecuperacionPasswordRequestDTO dto
    ) {
        usuarioService.solicitarCodigoRecuperacionPassword(dto);
        return ResponseEntity.ok(new ApiResponse<>("Código de recuperación enviado correctamente", null));
    }

    @PostMapping("/password/verificar-codigo")
    public ResponseEntity<ApiResponse<Void>> verificarCodigoRecuperacion(
            @Valid @RequestBody VerificarCodigoRecuperacionPasswordRequestDTO dto
    ) {
        usuarioService.verificarCodigoRecuperacionPassword(dto);
        return ResponseEntity.ok(new ApiResponse<>("Código verificado correctamente", null));
    }

    @PostMapping("/password/restablecer")
    public ResponseEntity<ApiResponse<Void>> restablecerPassword(
            @Valid @RequestBody RestablecerPasswordRequestDTO dto
    ) {
        usuarioService.restablecerPassword(dto);
        return ResponseEntity.ok(new ApiResponse<>("Contraseña restablecida correctamente", null));
    }

    @GetMapping("/cuenta")
    public ResponseEntity<ApiResponse<UsuarioCuentaResponseDTO>> obtenerCuenta(HttpServletRequest request) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);
        UsuarioCuentaResponseDTO response = usuarioService.obtenerCuenta(idUsuario);
        return ResponseEntity.ok(new ApiResponse<>("Información de cuenta obtenida correctamente", response));
    }

    @PutMapping("/cuenta/email")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> actualizarEmail(
            @Valid @RequestBody ActualizarEmailRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);
        AuthResponseDTO response = usuarioService.actualizarEmail(idUsuario, dto);
        return ResponseEntity.ok(new ApiResponse<>("Correo actualizado correctamente", response));
    }

    @PutMapping("/cuenta/password")
    public ResponseEntity<ApiResponse<Void>> actualizarPassword(
            @Valid @RequestBody ActualizarPasswordRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);
        usuarioService.actualizarPassword(idUsuario, dto);
        return ResponseEntity.ok(new ApiResponse<>("Contraseña actualizada correctamente", null));
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<ApiResponse<Void>> eliminarCuenta(
            @Valid @RequestBody EliminarCuentaRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = jwtUtil.getIdFromRequest(request);
        usuarioService.eliminarCuenta(idUsuario, dto);
        return ResponseEntity.ok(new ApiResponse<>("Cuenta eliminada correctamente", null));
    }
}
