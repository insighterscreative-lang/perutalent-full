package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.BadRequestException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ResourceNotFoundException;
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

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEstadoRepository usuarioEstadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final S3StorageService s3StorageService;
    private final EntityManager entityManager;
    private final PasswordResetCodigoRepository passwordResetCodigoRepository;
    private final PasswordResetCodeHasher passwordResetCodeHasher;

    private static final String ESTADO_ACTIVO = "Activo";
    private static final int MINUTOS_EXPIRACION_CODIGO = 2;
    private static final int MINUTOS_VALIDEZ_TRAS_VERIFICAR = 10;
    private static final int SEGUNDOS_ENTRE_SOLICITUDES = 60;
    private static final int MAX_SOLICITUDES_POR_HORA = 5;
    private static final int MAX_INTENTOS_POR_CODIGO = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private void validarRoles(UsuarioRegisterRequestDTO dto) {
        boolean esEmpleado = Boolean.TRUE.equals(dto.getEsEmpleado());
        boolean esEmpleador = Boolean.TRUE.equals(dto.getEsEmpleador());

        if (esEmpleado == esEmpleador) {
            throw new BadRequestException("Debe seleccionar un único tipo de usuario");
        }
    }

    private String obtenerRol(Usuario usuario) {
        if (usuario.isEsEmpleado() == usuario.isEsEmpleador()) {
            throw new IllegalStateException("La cuenta debe tener un único rol válido");
        }

        return usuario.isEsEmpleado() ? "EMPLEADO" : "EMPLEADOR";
    }

    @Transactional
    public Long register(UsuarioRegisterRequestDTO dto) {

        validarRoles(dto);

        if (!Boolean.TRUE.equals(dto.getAceptaTerminos())) {
            throw new BadRequestException("Debes aceptar los Términos y Condiciones y la Política de Privacidad");
        }

        String emailNormalizado = normalizarEmail(dto.getEmail());

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ConflictException("Este correo ya está registrado en la plataforma");
        }

        UsuarioEstado estadoActivo = usuarioEstadoRepository
                .findByNombreEstado(ESTADO_ACTIVO)
                .orElseThrow(() -> new RuntimeException("Estado 'Activo' no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailNormalizado);

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

        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(dto.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("El correo ingresado no existe"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (!ESTADO_ACTIVO.equals(usuario.getEstado().getNombreEstado())) {
            throw new UnauthorizedException("Cuenta no activa. Contacte al soporte.");
        }

        return construirAuthResponse(usuario);
    }

    @Transactional
    public void solicitarCodigoRecuperacionPassword(SolicitarRecuperacionPasswordRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("El correo ingresado no existe"));

        LocalDateTime ahora = LocalDateTime.now();
        validarLimitesSolicitudCodigo(usuario, ahora);

        passwordResetCodigoRepository.invalidarCodigosActivos(usuario.getId(), ahora);

        String codigo = generarCodigoRecuperacion();

        PasswordResetCodigo codigoReset = new PasswordResetCodigo();
        codigoReset.setUsuario(usuario);
        codigoReset.setCodigoHash(passwordResetCodeHasher.hash(email, codigo));
        codigoReset.setFechaCreacion(ahora);
        codigoReset.setFechaExpiracion(ahora.plusMinutes(MINUTOS_EXPIRACION_CODIGO));
        codigoReset.setUsado(false);
        codigoReset.setVerificado(false);
        codigoReset.setIntentosFallidos(0);

        passwordResetCodigoRepository.save(codigoReset);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.enviarCodigoRecuperacionPassword(usuario.getEmail(), codigo);
            } catch (Exception e) {
                System.out.println("No se pudo enviar el código de recuperación: " + e.getMessage());
            }
        });
    }

    @Transactional(noRollbackFor = {BadRequestException.class, TooManyRequestsException.class})
    public void verificarCodigoRecuperacionPassword(VerificarCodigoRecuperacionPasswordRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());

        PasswordResetCodigo codigoReset = obtenerCodigoActivo(email);
        LocalDateTime ahora = LocalDateTime.now();

        validarVigenciaParaVerificacion(codigoReset, ahora);
        validarCodigoIngresado(email, dto.getCodigo(), codigoReset, ahora);

        if (!codigoReset.isVerificado()) {
            codigoReset.setVerificado(true);
            codigoReset.setFechaVerificacion(ahora);
            passwordResetCodigoRepository.save(codigoReset);
        }
    }

    @Transactional(noRollbackFor = {BadRequestException.class, TooManyRequestsException.class})
    public void restablecerPassword(RestablecerPasswordRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());

        if (!dto.getNuevaPassword().equals(dto.getConfirmarPassword())) {
            throw new BadRequestException("La nueva contraseña y la confirmación no coinciden");
        }

        PasswordResetCodigo codigoReset = obtenerCodigoActivo(email);
        Usuario usuario = codigoReset.getUsuario();
        LocalDateTime ahora = LocalDateTime.now();

        validarVigenciaParaRestablecimiento(codigoReset, ahora);
        validarCodigoIngresado(email, dto.getCodigo(), codigoReset, ahora);

        if (!codigoReset.isVerificado()) {
            codigoReset.setVerificado(true);
            codigoReset.setFechaVerificacion(ahora);
            passwordResetCodigoRepository.save(codigoReset);
        }

        if (passwordEncoder.matches(dto.getNuevaPassword(), usuario.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la contraseña actual");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getNuevaPassword()));
        usuarioRepository.save(usuario);

        codigoReset.setUsado(true);
        codigoReset.setFechaUso(ahora);
        passwordResetCodigoRepository.save(codigoReset);
    }

    @Transactional(readOnly = true)
    public UsuarioCuentaResponseDTO obtenerCuenta(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return new UsuarioCuentaResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.isEsEmpleado(),
                usuario.isEsEmpleador(),
                usuario.getFechaRegistro()
        );
    }

    @Transactional
    public AuthResponseDTO actualizarEmail(Long idUsuario, ActualizarEmailRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new UnauthorizedException("La contraseña actual no es correcta");
        }

        String nuevoEmail = normalizarEmail(dto.getNuevoEmail());

        if (nuevoEmail.equalsIgnoreCase(usuario.getEmail())) {
            throw new BadRequestException("El nuevo correo debe ser diferente al correo actual");
        }

        if (usuarioRepository.existsByEmail(nuevoEmail)) {
            throw new ConflictException("Este correo ya está registrado en la plataforma");
        }

        usuario.setEmail(nuevoEmail);
        Usuario actualizado = usuarioRepository.save(usuario);

        return construirAuthResponse(actualizado);
    }

    @Transactional
    public void actualizarPassword(Long idUsuario, ActualizarPasswordRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new UnauthorizedException("La contraseña actual no es correcta");
        }

        if (!dto.getNuevaPassword().equals(dto.getConfirmarPassword())) {
            throw new BadRequestException("La nueva contraseña y la confirmación no coinciden");
        }

        if (passwordEncoder.matches(dto.getNuevaPassword(), usuario.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarCuenta(Long idUsuario, EliminarCuentaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new UnauthorizedException("La contraseña actual no es correcta");
        }

        if (!"ELIMINAR".equalsIgnoreCase(dto.getConfirmacion().trim())) {
            throw new BadRequestException("Para eliminar tu cuenta debes escribir ELIMINAR en el campo de confirmación");
        }

        Long idEmpleado = buscarLongUnico(
                "SELECT id_empleado FROM usuario_empleado WHERE id_usuario = :idUsuario",
                "idUsuario",
                idUsuario
        );

        Long idEmpleador = buscarLongUnico(
                "SELECT id_empleador FROM usuario_empleador WHERE id_usuario = :idUsuario",
                "idUsuario",
                idUsuario
        );

        eliminarArchivosS3DeLaCuenta(idEmpleado, idEmpleador);

        if (idEmpleado != null) {
            ejecutarActualizacion(
                    "UPDATE oferta_laboral SET id_empleado_seleccionado = NULL WHERE id_empleado_seleccionado = :idEmpleado",
                    "idEmpleado",
                    idEmpleado
            );
        }

        ejecutarActualizacion(
                "DELETE FROM calificacion WHERE id_calificador = :idUsuario OR id_calificado = :idUsuario",
                "idUsuario",
                idUsuario
        );

        if (idEmpleado != null) {
            eliminarDatosEmpleado(idEmpleado);
        }

        if (idEmpleador != null) {
            eliminarDatosEmpleador(idEmpleador);
        }

        ejecutarActualizacion("DELETE FROM pago_suscripcion WHERE id_usuario = :idUsuario", "idUsuario", idUsuario);
        ejecutarActualizacion("DELETE FROM suscripcion_usuario WHERE id_usuario = :idUsuario", "idUsuario", idUsuario);
        ejecutarActualizacion("DELETE FROM uso_plan_usuario WHERE id_usuario = :idUsuario", "idUsuario", idUsuario);
        ejecutarActualizacion("DELETE FROM usuario WHERE id_usuario = :idUsuario", "idUsuario", idUsuario);
    }

    private void eliminarDatosEmpleado(Long idEmpleado) {
        ejecutarActualizacion(
                "DELETE FROM calificacion WHERE id_postulacion IN (SELECT id_postulacion FROM postulacion WHERE id_empleado = :idEmpleado)",
                "idEmpleado",
                idEmpleado
        );
        ejecutarActualizacion("DELETE FROM postulacion WHERE id_empleado = :idEmpleado", "idEmpleado", idEmpleado);
        ejecutarActualizacion("DELETE FROM empleado_habilidad WHERE id_empleado = :idEmpleado", "idEmpleado", idEmpleado);
        ejecutarActualizacion("DELETE FROM empleado_herramienta WHERE id_empleado = :idEmpleado", "idEmpleado", idEmpleado);
        ejecutarActualizacion("DELETE FROM empleado_modalidad WHERE id_empleado = :idEmpleado", "idEmpleado", idEmpleado);
        ejecutarActualizacion("DELETE FROM empleado_categoria WHERE id_empleado = :idEmpleado", "idEmpleado", idEmpleado);
        ejecutarActualizacion("DELETE FROM usuario_empleado WHERE id_empleado = :idEmpleado", "idEmpleado", idEmpleado);
    }

    private void eliminarDatosEmpleador(Long idEmpleador) {
        List<Long> idsOfertas = consultarLongs(
                "SELECT id_oferta FROM oferta_laboral WHERE id_empleador = :idEmpleador",
                "idEmpleador",
                idEmpleador
        );

        for (Long idOferta : idsOfertas) {
            ejecutarActualizacion(
                    "DELETE FROM calificacion WHERE id_postulacion IN (SELECT id_postulacion FROM postulacion WHERE id_oferta = :idOferta)",
                    "idOferta",
                    idOferta
            );
            ejecutarActualizacion("DELETE FROM postulacion WHERE id_oferta = :idOferta", "idOferta", idOferta);
            ejecutarActualizacion("DELETE FROM oferta_habilidad WHERE id_oferta = :idOferta", "idOferta", idOferta);
            ejecutarActualizacion("DELETE FROM oferta_laboral WHERE id_oferta = :idOferta", "idOferta", idOferta);
        }

        ejecutarActualizacion("DELETE FROM empleador_categoria WHERE id_empleador = :idEmpleador", "idEmpleador", idEmpleador);
        ejecutarActualizacion("DELETE FROM usuario_empleador WHERE id_empleador = :idEmpleador", "idEmpleador", idEmpleador);
    }

    private void eliminarArchivosS3DeLaCuenta(Long idEmpleado, Long idEmpleador) {
        if (idEmpleado != null) {
            eliminarArchivosS3(consultarStrings(
                    "SELECT cv_url FROM postulacion WHERE id_empleado = :idEmpleado AND cv_url IS NOT NULL",
                    "idEmpleado",
                    idEmpleado
            ), "cvs/postulaciones/");

            eliminarArchivosS3(consultarStrings(
                    "SELECT curriculum FROM usuario_empleado WHERE id_empleado = :idEmpleado AND curriculum IS NOT NULL",
                    "idEmpleado",
                    idEmpleado
            ), "cvs/perfiles/");

            eliminarArchivosS3(consultarStrings(
                    "SELECT foto_perfil FROM usuario_empleado WHERE id_empleado = :idEmpleado AND foto_perfil IS NOT NULL",
                    "idEmpleado",
                    idEmpleado
            ), "imagenes/");
        }

        if (idEmpleador != null) {
            eliminarArchivosS3(consultarStrings(
                    "SELECT p.cv_url FROM postulacion p INNER JOIN oferta_laboral o ON o.id_oferta = p.id_oferta WHERE o.id_empleador = :idEmpleador AND p.cv_url IS NOT NULL",
                    "idEmpleador",
                    idEmpleador
            ), "cvs/postulaciones/");

            eliminarArchivosS3(consultarStrings(
                    "SELECT logo_empleador FROM usuario_empleador WHERE id_empleador = :idEmpleador AND logo_empleador IS NOT NULL",
                    "idEmpleador",
                    idEmpleador
            ), "imagenes/");
        }
    }

    private void eliminarArchivosS3(List<String> keys, String prefijoPermitido) {
        keys.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(key -> !key.isBlank())
                .filter(key -> key.startsWith(prefijoPermitido))
                .distinct()
                .forEach(this::eliminarArchivoS3SinRomperProceso);
    }

    private void eliminarArchivoS3SinRomperProceso(String key) {
        try {
            s3StorageService.eliminarArchivo(key);
        } catch (Exception e) {
            System.out.println("No se pudo eliminar archivo de S3 al eliminar cuenta: " + key + " - " + e.getMessage());
        }
    }

    private int ejecutarActualizacion(String sql, String paramName, Object value) {
        return entityManager.createNativeQuery(sql)
                .setParameter(paramName, value)
                .executeUpdate();
    }

    private Long buscarLongUnico(String sql, String paramName, Object value) {
        List<?> resultados = entityManager.createNativeQuery(sql)
                .setParameter(paramName, value)
                .getResultList();

        if (resultados.isEmpty() || resultados.get(0) == null) {
            return null;
        }

        return ((Number) resultados.get(0)).longValue();
    }

    private List<Long> consultarLongs(String sql, String paramName, Object value) {
        return entityManager.createNativeQuery(sql)
                .setParameter(paramName, value)
                .getResultList()
                .stream()
                .filter(Objects::nonNull)
                .map(resultado -> ((Number) resultado).longValue())
                .toList();
    }

    private List<String> consultarStrings(String sql, String paramName, Object value) {
        return entityManager.createNativeQuery(sql)
                .setParameter(paramName, value)
                .getResultList()
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
    }

    private void validarLimitesSolicitudCodigo(Usuario usuario, LocalDateTime ahora) {
        PasswordResetCodigo ultimaSolicitud = passwordResetCodigoRepository
                .findFirstByUsuarioEmailIgnoreCaseOrderByFechaCreacionDesc(usuario.getEmail())
                .orElse(null);

        if (ultimaSolicitud != null) {
            LocalDateTime siguienteSolicitudPermitida =
                    ultimaSolicitud.getFechaCreacion().plusSeconds(SEGUNDOS_ENTRE_SOLICITUDES);

            if (siguienteSolicitudPermitida.isAfter(ahora)) {
                long segundos = Math.max(1, Duration.between(ahora, siguienteSolicitudPermitida).getSeconds());
                throw new TooManyRequestsException(
                        "Espera " + segundos + " segundos antes de solicitar otro código."
                );
            }
        }

        long solicitudesUltimaHora = passwordResetCodigoRepository
                .contarSolicitudesDesde(usuario.getId(), ahora.minusHours(1));

        if (solicitudesUltimaHora >= MAX_SOLICITUDES_POR_HORA) {
            throw new TooManyRequestsException(
                    "Alcanzaste el límite de " + MAX_SOLICITUDES_POR_HORA
                            + " códigos de recuperación por hora. Inténtalo más tarde."
            );
        }
    }

    private PasswordResetCodigo obtenerCodigoActivo(String email) {
        return passwordResetCodigoRepository
                .findFirstByUsuarioEmailIgnoreCaseAndUsadoFalseOrderByFechaCreacionDesc(email)
                .orElseThrow(() -> new BadRequestException(
                        "No tienes un código activo. Solicita un nuevo código de recuperación."
                ));
    }

    private void validarVigenciaParaVerificacion(
            PasswordResetCodigo codigoReset,
            LocalDateTime ahora
    ) {
        if (codigoReset.getFechaExpiracion().isBefore(ahora)) {
            invalidarCodigo(codigoReset, ahora);
            throw new BadRequestException("El código venció. Solicita uno nuevo.");
        }
    }

    private void validarVigenciaParaRestablecimiento(
            PasswordResetCodigo codigoReset,
            LocalDateTime ahora
    ) {
        LocalDateTime limite;

        if (codigoReset.isVerificado() && codigoReset.getFechaVerificacion() != null) {
            limite = codigoReset.getFechaVerificacion().plusMinutes(MINUTOS_VALIDEZ_TRAS_VERIFICAR);
        } else {
            limite = codigoReset.getFechaExpiracion();
        }

        if (limite.isBefore(ahora)) {
            invalidarCodigo(codigoReset, ahora);
            throw new BadRequestException(
                    "La validación venció. Solicita un nuevo código de recuperación."
            );
        }
    }

    private void validarCodigoIngresado(
            String email,
            String codigo,
            PasswordResetCodigo codigoReset,
            LocalDateTime ahora
    ) {
        String codigoIngresado = codigo != null ? codigo.trim() : "";

        if (passwordResetCodeHasher.matches(email, codigoIngresado, codigoReset.getCodigoHash())) {
            return;
        }

        int intentosFallidos = codigoReset.getIntentosFallidos() + 1;
        codigoReset.setIntentosFallidos(intentosFallidos);

        if (intentosFallidos >= MAX_INTENTOS_POR_CODIGO) {
            invalidarCodigo(codigoReset, ahora);
            throw new TooManyRequestsException(
                    "Superaste el máximo de intentos. El código fue bloqueado; solicita uno nuevo."
            );
        }

        passwordResetCodigoRepository.save(codigoReset);

        int intentosRestantes = MAX_INTENTOS_POR_CODIGO - intentosFallidos;
        throw new BadRequestException(
                "Código inválido. Te quedan " + intentosRestantes + " intentos."
        );
    }

    private void invalidarCodigo(PasswordResetCodigo codigoReset, LocalDateTime ahora) {
        codigoReset.setUsado(true);
        codigoReset.setFechaUso(ahora);
        passwordResetCodigoRepository.save(codigoReset);
    }

    private String generarCodigoRecuperacion() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private AuthResponseDTO construirAuthResponse(Usuario usuario) {
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

    private String normalizarEmail(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}
