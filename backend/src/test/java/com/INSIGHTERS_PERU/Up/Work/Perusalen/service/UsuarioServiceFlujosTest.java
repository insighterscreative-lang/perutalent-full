package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.AuthResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ActualizarPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioLoginRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioRegisterRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.VerificarCodigoRecuperacionPasswordRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.BadRequestException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.TooManyRequestsException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.UnauthorizedException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PasswordResetCodigo;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEstado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PasswordResetCodigoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEstadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.JwtUtil;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.security.PasswordResetCodeHasher;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceFlujosTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioEstadoRepository usuarioEstadoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailService emailService;
    @Mock private S3StorageService s3StorageService;
    @Mock private EntityManager entityManager;
    @Mock private PasswordResetCodigoRepository passwordResetCodigoRepository;
    @Mock private PasswordResetCodeHasher passwordResetCodeHasher;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioEstado estadoActivo;

    @BeforeEach
    void preparar() {
        estadoActivo = new UsuarioEstado(1L, "Activo");
    }

    @Test
    void registraEmpleadoNormalizandoCorreoYCifrandoPassword() {
        UsuarioRegisterRequestDTO request = registroBase();
        request.setEmail("  PERSONA@Correo.COM ");
        request.setEsEmpleado(true);
        request.setEsEmpleador(false);

        when(usuarioRepository.existsByEmail("persona@correo.com")).thenReturn(false);
        when(usuarioEstadoRepository.findByNombreEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(passwordEncoder.encode("Prueba123!")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(15L);
            return usuario;
        });

        Long id = usuarioService.register(request);

        assertEquals(15L, id);
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("persona@correo.com", guardado.getEmail());
        assertEquals("hash-seguro", guardado.getPassword());
        assertTrue(guardado.isEsEmpleado());
        assertFalse(guardado.isEsEmpleador());
        assertFalse(guardado.isVerificado());
        verify(emailService).enviarCorreoBienvenidaEmpleado("persona@correo.com");
    }

    @Test
    void rechazaRegistroConAmbosRoles() {
        UsuarioRegisterRequestDTO request = registroBase();
        request.setEsEmpleado(true);
        request.setEsEmpleador(true);

        assertThrows(BadRequestException.class, () -> usuarioService.register(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaRegistroSinAceptarTerminos() {
        UsuarioRegisterRequestDTO request = registroBase();
        request.setEsEmpleado(true);
        request.setEsEmpleador(false);
        request.setAceptaTerminos(false);

        assertThrows(BadRequestException.class, () -> usuarioService.register(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaCorreoDuplicado() {
        UsuarioRegisterRequestDTO request = registroBase();
        request.setEsEmpleado(false);
        request.setEsEmpleador(true);
        when(usuarioRepository.existsByEmail("persona@correo.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> usuarioService.register(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginDevuelveTokenYRolCorrectos() {
        Usuario usuario = usuarioEmpleado();
        UsuarioLoginRequestDTO request = new UsuarioLoginRequestDTO(" PERSONA@CORREO.COM ", "Prueba123!");

        when(usuarioRepository.findByEmail("persona@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Prueba123!", "hash-actual")).thenReturn(true);
        when(jwtUtil.generateToken("persona@correo.com", 7L, "EMPLEADO")).thenReturn("jwt-prueba");

        AuthResponseDTO response = usuarioService.login(request);

        assertEquals("jwt-prueba", response.getToken());
        assertEquals(7L, response.getId());
        assertTrue(response.isEsEmpleado());
        assertFalse(response.isEsEmpleador());
    }

    @Test
    void loginRechazaPasswordIncorrecta() {
        Usuario usuario = usuarioEmpleado();
        when(usuarioRepository.findByEmail("persona@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash-actual")).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> usuarioService.login(new UsuarioLoginRequestDTO("persona@correo.com", "incorrecta"))
        );
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginRechazaCuentaInactiva() {
        Usuario usuario = usuarioEmpleado();
        usuario.setEstado(new UsuarioEstado(2L, "Bloqueado"));
        when(usuarioRepository.findByEmail("persona@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Prueba123!", "hash-actual")).thenReturn(true);

        assertThrows(
                UnauthorizedException.class,
                () -> usuarioService.login(new UsuarioLoginRequestDTO("persona@correo.com", "Prueba123!"))
        );
    }

    @Test
    void bloqueaCodigoDespuesDeCincoIntentosIncorrectos() {
        Usuario usuario = usuarioEmpleado();
        PasswordResetCodigo codigo = codigoActivo(usuario);
        when(passwordResetCodigoRepository
                .findFirstByUsuarioEmailIgnoreCaseAndUsadoFalseOrderByFechaCreacionDesc("persona@correo.com"))
                .thenReturn(Optional.of(codigo));
        when(passwordResetCodeHasher.matches(eq("persona@correo.com"), eq("000000"), any()))
                .thenReturn(false);

        VerificarCodigoRecuperacionPasswordRequestDTO request =
                new VerificarCodigoRecuperacionPasswordRequestDTO("persona@correo.com", "000000");

        for (int intento = 1; intento <= 4; intento++) {
            assertThrows(BadRequestException.class,
                    () -> usuarioService.verificarCodigoRecuperacionPassword(request));
            assertEquals(intento, codigo.getIntentosFallidos());
            assertFalse(codigo.isUsado());
        }

        assertThrows(TooManyRequestsException.class,
                () -> usuarioService.verificarCodigoRecuperacionPassword(request));
        assertEquals(5, codigo.getIntentosFallidos());
        assertTrue(codigo.isUsado());
        verify(passwordResetCodigoRepository, times(5)).save(codigo);
    }

    @Test
    void invalidaCodigoVencido() {
        Usuario usuario = usuarioEmpleado();
        PasswordResetCodigo codigo = codigoActivo(usuario);
        codigo.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        when(passwordResetCodigoRepository
                .findFirstByUsuarioEmailIgnoreCaseAndUsadoFalseOrderByFechaCreacionDesc("persona@correo.com"))
                .thenReturn(Optional.of(codigo));

        VerificarCodigoRecuperacionPasswordRequestDTO request =
                new VerificarCodigoRecuperacionPasswordRequestDTO("persona@correo.com", "123456");

        assertThrows(BadRequestException.class,
                () -> usuarioService.verificarCodigoRecuperacionPassword(request));
        assertTrue(codigo.isUsado());
        verify(passwordResetCodigoRepository).save(codigo);
    }

    @Test
    void actualizaPasswordCuandoLasCredencialesSonValidas() {
        Usuario usuario = usuarioEmpleado();
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Actual123!", "hash-actual")).thenReturn(true);
        when(passwordEncoder.matches("Nueva123!", "hash-actual")).thenReturn(false);
        when(passwordEncoder.encode("Nueva123!")).thenReturn("hash-nuevo");

        usuarioService.actualizarPassword(
                7L,
                new ActualizarPasswordRequestDTO("Actual123!", "Nueva123!", "Nueva123!")
        );

        assertEquals("hash-nuevo", usuario.getPassword());
        verify(usuarioRepository).save(usuario);
    }

    private UsuarioRegisterRequestDTO registroBase() {
        return new UsuarioRegisterRequestDTO(
                "persona@correo.com",
                "Prueba123!",
                true,
                false,
                true
        );
    }

    private Usuario usuarioEmpleado() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("persona@correo.com");
        usuario.setPassword("hash-actual");
        usuario.setEsEmpleado(true);
        usuario.setEsEmpleador(false);
        usuario.setEstado(estadoActivo);
        return usuario;
    }

    private PasswordResetCodigo codigoActivo(Usuario usuario) {
        PasswordResetCodigo codigo = new PasswordResetCodigo();
        codigo.setId(11L);
        codigo.setUsuario(usuario);
        codigo.setCodigoHash("hash-codigo");
        codigo.setFechaCreacion(LocalDateTime.now().minusSeconds(10));
        codigo.setFechaExpiracion(LocalDateTime.now().plusMinutes(2));
        codigo.setIntentosFallidos(0);
        codigo.setUsado(false);
        codigo.setVerificado(false);
        return codigo;
    }
}
