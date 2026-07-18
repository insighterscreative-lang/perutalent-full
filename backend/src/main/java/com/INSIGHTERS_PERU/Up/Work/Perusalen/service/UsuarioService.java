package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.AuthResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioLoginRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioRegisterRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.BadRequestException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.UnauthorizedException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEstado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEstadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEstadoRepository usuarioEstadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final String ESTADO_ACTIVO = "Activo";

    private void validarRoles(UsuarioRegisterRequestDTO dto) {
        boolean esEmpleado = Boolean.TRUE.equals(dto.getEsEmpleado());
        boolean esEmpleador = Boolean.TRUE.equals(dto.getEsEmpleador());

        if (esEmpleado == esEmpleador) {
            throw new BadRequestException("Debe seleccionar un único tipo de usuario");
        }
    }

    private String obtenerRol(Usuario usuario) {
        if (usuario.isEsEmpleado()) return "EMPLEADO";
        if (usuario.isEsEmpleador()) return "EMPLEADOR";
        throw new IllegalStateException("Usuario sin rol definido");
    }

    public Long register(UsuarioRegisterRequestDTO dto) {

        validarRoles(dto);

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Este correo ya está registrado en la plataforma");
        }

        UsuarioEstado estadoActivo = usuarioEstadoRepository
                .findByNombreEstado(ESTADO_ACTIVO)
                .orElseThrow(() -> new RuntimeException("Estado 'Activo' no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());

        boolean esEmpleado = Boolean.TRUE.equals(dto.getEsEmpleado());
        boolean esEmpleador = Boolean.TRUE.equals(dto.getEsEmpleador());

        usuario.setEsEmpleado(esEmpleado);
        usuario.setEsEmpleador(esEmpleador);

        String passwordHash = passwordEncoder.encode(dto.getPassword());
        usuario.setPassword(passwordHash);

        usuario.setFechaRegistro(LocalDate.now());
        usuario.setEstado(estadoActivo);
        usuario.setVerificado(false);

        Usuario saved = usuarioRepository.save(usuario);

        enviarCorreoBienvenida(saved);

        return saved.getId();
    }

    private void enviarCorreoBienvenida(Usuario usuario) {
        try {
            if (usuario.isEsEmpleado()) {
                emailService.enviarCorreoBienvenidaEmpleado(usuario.getEmail());
                return;
            }

            if (usuario.isEsEmpleador()) {
                emailService.enviarCorreoBienvenidaEmpleador(usuario.getEmail());
                return;
            }

            System.out.println("Usuario registrado sin rol válido. No se envió correo de bienvenida.");

        } catch (Exception e) {
            System.out.println("No se pudo enviar el correo de bienvenida: " + e.getMessage());
        }
    }

    public AuthResponseDTO login(UsuarioLoginRequestDTO dto) {

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (!ESTADO_ACTIVO.equals(usuario.getEstado().getNombreEstado())) {
            throw new UnauthorizedException("Cuenta no activa. Contacte al soporte.");
        }

        String rol = obtenerRol(usuario);

        String token = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getId(),
                rol
        );

        return new AuthResponseDTO(
                token,
                usuario.getId(),
                usuario.getEmail(),
                usuario.isEsEmpleado(),
                usuario.isEsEmpleador()
        );
    }
}