package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PostulacionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Postulacion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoHabilidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoHerramientaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PostulacionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostulacionService {

    private static final String ESTADO_OFERTA_ABIERTA = "ABIERTA";

    private static final String ESTADO_POSTULACION_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_POSTULACION_ACEPTADA = "ACEPTADA";
    private static final String ESTADO_POSTULACION_RECHAZADA = "RECHAZADA";

    private final PostulacionRepository postulacionRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final UsuarioEmpleadoRepository usuarioEmpleadoRepository;
    private final UsuarioEmpleadorRepository usuarioEmpleadorRepository;

    private final EmpleadoModalidadRepository empleadoModalidadRepository;
    private final EmpleadoHabilidadRepository empleadoHabilidadRepository;
    private final EmpleadoHerramientaRepository empleadoHerramientaRepository;

    private final S3StorageService s3StorageService;
    private final EmailService emailService;
    private final SuscripcionService suscripcionService;

    @Transactional
    public void postular(
            Long idOferta,
            Long idUsuario,
            Boolean usarCvPerfil,
            MultipartFile cv
    ) {
        UsuarioEmpleado empleado = usuarioEmpleadoRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de empleado no encontrado"));

        OfertaLaboral oferta = ofertaLaboralRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta laboral no encontrada"));

        if (!ESTADO_OFERTA_ABIERTA.equals(oferta.getEstadoOferta())) {
            throw new RuntimeException("Solo puedes postular a ofertas abiertas");
        }

        boolean yaPostulo = postulacionRepository
                .existsByIdOfertaIdAndIdEmpleadoId(idOferta, empleado.getId());

        if (yaPostulo) {
            throw new RuntimeException("Ya postulaste a esta oferta");
        }

        suscripcionService.validarPuedePostular(idUsuario);

        String cvUrl;

        if (Boolean.TRUE.equals(usarCvPerfil)) {
            if (empleado.getCurriculum() == null || empleado.getCurriculum().isBlank()) {
                throw new RuntimeException("No tienes un CV cargado en tu perfil");
            }

            cvUrl = empleado.getCurriculum();
        } else {
            if (cv == null || cv.isEmpty()) {
                throw new RuntimeException("Debes adjuntar un CV para postular");
            }

            cvUrl = s3StorageService.subirCvPostulacion(cv, idUsuario, idOferta);
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setIdOferta(oferta);
        postulacion.setIdEmpleado(empleado);
        postulacion.setFechaPostulacion(LocalDate.now());
        postulacion.setEstadoPostulacion(ESTADO_POSTULACION_PENDIENTE);
        postulacion.setCvUrl(cvUrl);

        postulacionRepository.save(postulacion);

        suscripcionService.registrarPostulacionUsada(idUsuario);
    }

    @Transactional
    public List<PostulacionResponseDTO> listarPostulantesPorOferta(
            Long idOferta,
            Long idUsuario
    ) {
        UsuarioEmpleador empleador = usuarioEmpleadorRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de empleador no encontrado"));

        OfertaLaboral oferta = ofertaLaboralRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta laboral no encontrada"));

        if (!oferta.getIdEmpleador().getId().equals(empleador.getId())) {
            throw new RuntimeException("No tienes permiso para ver las postulaciones de esta oferta");
        }

        List<Postulacion> postulaciones =
                postulacionRepository.findByIdOfertaIdOrderByFechaPostulacionDesc(idOferta);

        return postulaciones.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        (Postulacion postulacion) ->
                                                suscripcionService.usuarioTienePrioridadPostulante(
                                                        postulacion.getIdEmpleado().getUsuario().getId()
                                                )
                                )
                                .reversed()
                                .thenComparing(
                                        Postulacion::getFechaPostulacion,
                                        Comparator.reverseOrder()
                                )
                )
                .map(this::mapToPostulacionResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> listarIdsOfertasPostuladas(Long idUsuario) {
        UsuarioEmpleado empleado = usuarioEmpleadoRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de empleado no encontrado"));

        return postulacionRepository.findByIdEmpleadoId(empleado.getId())
                .stream()
                .map(postulacion -> postulacion.getIdOferta().getId())
                .toList();
    }

    @Transactional
    public PostulacionResponseDTO aceptarPostulacion(Long idPostulacion, Long idUsuario) {
        return cambiarEstadoPostulacion(
                idPostulacion,
                idUsuario,
                ESTADO_POSTULACION_ACEPTADA
        );
    }

    @Transactional
    public PostulacionResponseDTO rechazarPostulacion(Long idPostulacion, Long idUsuario) {
        return cambiarEstadoPostulacion(
                idPostulacion,
                idUsuario,
                ESTADO_POSTULACION_RECHAZADA
        );
    }

    private PostulacionResponseDTO cambiarEstadoPostulacion(
            Long idPostulacion,
            Long idUsuario,
            String nuevoEstado
    ) {
        UsuarioEmpleador empleador = usuarioEmpleadorRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de empleador no encontrado"));

        Postulacion postulacion = postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        OfertaLaboral oferta = postulacion.getIdOferta();

        if (!oferta.getIdEmpleador().getId().equals(empleador.getId())) {
            throw new RuntimeException("No tienes permiso para modificar esta postulación");
        }

        if (!ESTADO_POSTULACION_PENDIENTE.equals(postulacion.getEstadoPostulacion())) {
            throw new RuntimeException("Solo puedes aceptar o rechazar postulaciones pendientes");
        }

        postulacion.setEstadoPostulacion(nuevoEstado);

        Postulacion postulacionActualizada = postulacionRepository.save(postulacion);

        enviarCorreoCambioEstado(postulacionActualizada, nuevoEstado);

        return mapToPostulacionResponseDTO(postulacionActualizada);
    }

    private void enviarCorreoCambioEstado(Postulacion postulacion, String nuevoEstado) {
        try {
            UsuarioEmpleado empleado = postulacion.getIdEmpleado();
            OfertaLaboral oferta = postulacion.getIdOferta();

            String emailEmpleado = empleado.getUsuario().getEmail();
            String nombreEmpleado = construirNombreEmpleado(empleado);
            String tituloOferta = oferta.getTitulo();

            if (ESTADO_POSTULACION_ACEPTADA.equals(nuevoEstado)) {
                emailService.enviarCorreoPostulacionAceptada(
                        emailEmpleado,
                        nombreEmpleado,
                        tituloOferta
                );
            }

            if (ESTADO_POSTULACION_RECHAZADA.equals(nuevoEstado)) {
                emailService.enviarCorreoPostulacionRechazada(
                        emailEmpleado,
                        nombreEmpleado,
                        tituloOferta
                );
            }

        } catch (Exception e) {
            System.out.println("No se pudo enviar el correo de cambio de estado: " + e.getMessage());
        }
    }

    private String construirNombreEmpleado(UsuarioEmpleado empleado) {
        String nombre = empleado.getNombre() != null ? empleado.getNombre() : "";
        String apellido = empleado.getApellido() != null ? empleado.getApellido() : "";

        String nombreCompleto = (nombre + " " + apellido).trim();

        if (nombreCompleto.isBlank()) {
            return "postulante";
        }

        return nombreCompleto;
    }

    @Transactional(readOnly = true)
    public Postulacion obtenerPostulacionPorId(Long idPostulacion) {
        return postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
    }

    @Transactional(readOnly = true)
    public Postulacion obtenerPostulacionPorIdAutorizada(
            Long idPostulacion,
            Long idUsuario
    ) {
        Postulacion postulacion = postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        boolean esEmpleadoPostulante = postulacion.getIdEmpleado() != null
                && postulacion.getIdEmpleado().getUsuario() != null
                && postulacion.getIdEmpleado().getUsuario().getId().equals(idUsuario);

        boolean esEmpleadorDeLaOferta = postulacion.getIdOferta() != null
                && postulacion.getIdOferta().getIdEmpleador() != null
                && postulacion.getIdOferta().getIdEmpleador().getUsuario() != null
                && postulacion.getIdOferta().getIdEmpleador().getUsuario().getId().equals(idUsuario);

        if (!esEmpleadoPostulante && !esEmpleadorDeLaOferta) {
            throw new RuntimeException("No tienes permiso para ver el CV de esta postulación");
        }

        return postulacion;
    }

    private PostulacionResponseDTO mapToPostulacionResponseDTO(Postulacion postulacion) {
        UsuarioEmpleado empleado = postulacion.getIdEmpleado();

        Long idEmpleado = empleado.getId();

        List<Long> modalidadIds = empleadoModalidadRepository.findByIdEmpleadoId(idEmpleado)
                .stream()
                .map(empleadoModalidad -> empleadoModalidad.getIdMod().getId())
                .toList();

        List<String> modalidades = empleadoModalidadRepository.findByIdEmpleadoId(idEmpleado)
                .stream()
                .map(empleadoModalidad -> empleadoModalidad.getIdMod().getNombreMod())
                .toList();

        List<Long> habilidadIds = empleadoHabilidadRepository.findByIdEmpleadoId(idEmpleado)
                .stream()
                .map(empleadoHabilidad -> empleadoHabilidad.getIdHabilidad().getId())
                .toList();

        List<String> habilidades = empleadoHabilidadRepository.findByIdEmpleadoId(idEmpleado)
                .stream()
                .map(empleadoHabilidad -> empleadoHabilidad.getIdHabilidad().getNombreHabilidad())
                .toList();

        List<Long> herramientaIds = empleadoHerramientaRepository.findByIdEmpleadoId(idEmpleado)
                .stream()
                .map(empleadoHerramienta -> empleadoHerramienta.getIdHerramienta().getId())
                .toList();

        List<String> herramientas = empleadoHerramientaRepository.findByIdEmpleadoId(idEmpleado)
                .stream()
                .map(empleadoHerramienta -> empleadoHerramienta.getIdHerramienta().getNombreHerramienta())
                .toList();

        Long idDistrito = null;
        String distrito = null;

        if (empleado.getDistrito() != null) {
            idDistrito = empleado.getDistrito().getId();
            distrito = empleado.getDistrito().getNombreDistrito();
        }

        Long idUsuarioEmpleado = empleado.getUsuario().getId();
        Boolean empleadoPremium = suscripcionService.usuarioTienePrioridadPostulante(idUsuarioEmpleado);
        String planEmpleado = suscripcionService.obtenerNombrePlanUsuario(idUsuarioEmpleado);

        return new PostulacionResponseDTO(
                postulacion.getId(),
                empleado.getId(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getUsuario().getEmail(),
                empleado.getTelefono(),

                idDistrito,
                distrito,

                modalidadIds,
                modalidades,

                habilidadIds,
                habilidades,

                herramientaIds,
                herramientas,

                postulacion.getFechaPostulacion(),
                postulacion.getEstadoPostulacion(),

                postulacion.getCvUrl(),

                empleadoPremium,
                planEmpleado
        );
    }
}