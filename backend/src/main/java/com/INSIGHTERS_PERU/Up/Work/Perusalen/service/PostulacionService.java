package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.FiltrosPostulantesResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.HabilidadesResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiPostulacionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PaginaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PostulacionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.BadRequestException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHabilidad;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHerramienta;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoModalidad;
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
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;

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
            throw new ConflictException("Esta oferta ya no se encuentra abierta para postulaciones");
        }

        if (FechaPeru.estaVencida(oferta.getFechaTerminoPostulacion())) {
            throw new ConflictException("El periodo de postulación de esta oferta ha finalizado");
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

            cvUrl = s3StorageService.copiarCvPerfilAPostulacion(
                    empleado.getCurriculum(),
                    idUsuario,
                    idOferta
            );
        } else {
            if (cv == null || cv.isEmpty()) {
                throw new RuntimeException("Debes adjuntar un CV para postular");
            }

            cvUrl = s3StorageService.subirCvPostulacion(cv, idUsuario, idOferta);
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setIdOferta(oferta);
        postulacion.setIdEmpleado(empleado);
        postulacion.setFechaPostulacion(FechaPeru.hoy());
        postulacion.setEstadoPostulacion(ESTADO_POSTULACION_PENDIENTE);
        postulacion.setCvUrl(cvUrl);

        Postulacion postulacionGuardada = postulacionRepository.save(postulacion);

        suscripcionService.registrarPostulacionUsada(idUsuario);

        enviarCorreoPostulacionEnviada(postulacionGuardada);
    }


    private void enviarCorreoPostulacionEnviada(Postulacion postulacion) {
        try {
            UsuarioEmpleado empleado = postulacion.getIdEmpleado();
            OfertaLaboral oferta = postulacion.getIdOferta();

            String emailEmpleado = empleado.getUsuario().getEmail();
            String nombreEmpleado = construirNombreEmpleado(empleado);
            String tituloOferta = oferta.getTitulo();
            String nombreEmpresa = "la empresa";

            if (oferta.getIdEmpleador() != null
                    && oferta.getIdEmpleador().getNombreComercial() != null
                    && !oferta.getIdEmpleador().getNombreComercial().isBlank()) {
                nombreEmpresa = oferta.getIdEmpleador().getNombreComercial();
            }

            emailService.enviarCorreoPostulacionEnviada(
                    emailEmpleado,
                    nombreEmpleado,
                    tituloOferta,
                    nombreEmpresa
            );

        } catch (Exception e) {
            System.out.println("No se pudo enviar el correo de confirmación de postulación: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<PostulacionResponseDTO> listarPostulantesPorOfertaPaginados(
            Long idOferta,
            Long idUsuario,
            String estado,
            String texto,
            Long distritoId,
            Long modalidadId,
            Long habilidadId,
            Long herramientaId,
            int page,
            int size
    ) {
        validarPaginacion(page, size);
        validarAccesoOferta(idOferta, idUsuario);

        String estadoNormalizado = normalizarEstadoFiltro(estado);
        String textoNormalizado = texto == null ? "" : texto.trim();

        Pageable pageable = PageRequest.of(page, size);

        Page<Long> paginaIds = postulacionRepository.findIdsPostulantesPaginados(
                idOferta,
                estadoNormalizado,
                textoNormalizado,
                normalizarIdFiltro(distritoId),
                normalizarIdFiltro(modalidadId),
                normalizarIdFiltro(habilidadId),
                normalizarIdFiltro(herramientaId),
                pageable
        );

        List<PostulacionResponseDTO> contenido = mapearPostulacionesEnLote(
                paginaIds.getContent()
        );

        return new PaginaResponseDTO<>(
                contenido,
                paginaIds.getNumber(),
                paginaIds.getSize(),
                paginaIds.getTotalElements(),
                paginaIds.getTotalPages(),
                paginaIds.isFirst(),
                paginaIds.isLast()
        );
    }

    @Transactional(readOnly = true)
    public FiltrosPostulantesResponseDTO listarFiltrosPostulantes(
            Long idOferta,
            Long idUsuario
    ) {
        validarAccesoOferta(idOferta, idUsuario);

        return new FiltrosPostulantesResponseDTO(
                postulacionRepository.findDistritosDisponiblesByOfertaId(idOferta),
                empleadoModalidadRepository.findOpcionesByOfertaId(idOferta),
                empleadoHabilidadRepository.findOpcionesByOfertaId(idOferta),
                empleadoHerramientaRepository.findOpcionesByOfertaId(idOferta)
        );
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

    private void validarAccesoOferta(Long idOferta, Long idUsuario) {
        UsuarioEmpleador empleador = usuarioEmpleadorRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de empleador no encontrado"));

        OfertaLaboral oferta = ofertaLaboralRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta laboral no encontrada"));

        if (!oferta.getIdEmpleador().getId().equals(empleador.getId())) {
            throw new RuntimeException("No tienes permiso para ver las postulaciones de esta oferta");
        }
    }

    private String normalizarEstadoFiltro(String estado) {
        if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)) {
            return "";
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        if (!List.of(
                ESTADO_POSTULACION_PENDIENTE,
                ESTADO_POSTULACION_ACEPTADA,
                ESTADO_POSTULACION_RECHAZADA
        ).contains(estadoNormalizado)) {
            throw new BadRequestException("Estado de postulación inválido");
        }

        return estadoNormalizado;
    }

    private Long normalizarIdFiltro(Long id) {
        return id == null || id < 1 ? 0L : id;
    }

    private void validarPaginacion(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("El número de página no puede ser negativo");
        }

        if (size < 1 || size > 50) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y 50");
        }
    }

    private List<PostulacionResponseDTO> mapearPostulacionesEnLote(List<Long> idsPostulaciones) {
        if (idsPostulaciones == null || idsPostulaciones.isEmpty()) {
            return List.of();
        }

        Map<Long, Postulacion> postulacionesPorId = postulacionRepository
                .findAllByIdWithDetalle(idsPostulaciones)
                .stream()
                .collect(Collectors.toMap(
                        Postulacion::getId,
                        postulacion -> postulacion,
                        (primera, segunda) -> primera,
                        LinkedHashMap::new
                ));

        List<Postulacion> postulacionesOrdenadas = idsPostulaciones.stream()
                .map(postulacionesPorId::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        List<Long> idsEmpleados = postulacionesOrdenadas.stream()
                .map(postulacion -> postulacion.getIdEmpleado().getId())
                .distinct()
                .toList();

        List<Long> idsUsuarios = postulacionesOrdenadas.stream()
                .map(postulacion -> postulacion.getIdEmpleado().getUsuario().getId())
                .distinct()
                .toList();

        Map<Long, CatalogosEmpleado> catalogos = cargarCatalogosEmpleados(idsEmpleados);
        Map<Long, SuscripcionService.PlanUsuarioResumen> planes =
                suscripcionService.obtenerResumenPlanesUsuarios(idsUsuarios);

        return postulacionesOrdenadas.stream()
                .map(postulacion -> mapToPostulacionResponseDTO(
                        postulacion,
                        catalogos.getOrDefault(
                                postulacion.getIdEmpleado().getId(),
                                CatalogosEmpleado.vacio()
                        ),
                        planes.getOrDefault(
                                postulacion.getIdEmpleado().getUsuario().getId(),
                                new SuscripcionService.PlanUsuarioResumen(false, "GRATUITO")
                        )
                ))
                .toList();
    }

    private Map<Long, CatalogosEmpleado> cargarCatalogosEmpleados(List<Long> idsEmpleados) {
        Map<Long, CatalogosEmpleadoMutable> temporal = new HashMap<>();
        idsEmpleados.forEach(id -> temporal.put(id, new CatalogosEmpleadoMutable()));

        empleadoModalidadRepository.findByIdEmpleadoIdIn(idsEmpleados)
                .forEach(relacion -> temporal
                        .computeIfAbsent(relacion.getIdEmpleado().getId(), id -> new CatalogosEmpleadoMutable())
                        .agregarModalidad(relacion));

        empleadoHabilidadRepository.findByIdEmpleadoIdIn(idsEmpleados)
                .forEach(relacion -> temporal
                        .computeIfAbsent(relacion.getIdEmpleado().getId(), id -> new CatalogosEmpleadoMutable())
                        .agregarHabilidad(relacion));

        empleadoHerramientaRepository.findByIdEmpleadoIdIn(idsEmpleados)
                .forEach(relacion -> temporal
                        .computeIfAbsent(relacion.getIdEmpleado().getId(), id -> new CatalogosEmpleadoMutable())
                        .agregarHerramienta(relacion));

        Map<Long, CatalogosEmpleado> resultado = new HashMap<>();
        temporal.forEach((id, valor) -> resultado.put(id, valor.inmutable()));
        return resultado;
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

    @Transactional(readOnly = true)
    public PaginaResponseDTO<MiPostulacionResponseDTO> listarMisPostulaciones(
            Long idUsuario,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BadRequestException("El número de página no puede ser negativo");
        }

        if (size < 1 || size > 20) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y 20");
        }

        UsuarioEmpleado empleado = usuarioEmpleadoRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de empleado no encontrado"));

        Sort orden = Sort.by(
                Sort.Order.desc("fechaPostulacion"),
                Sort.Order.desc("id")
        );

        Pageable pageable = PageRequest.of(page, size, orden);

        Page<Postulacion> pagina = postulacionRepository.findByIdEmpleadoId(
                empleado.getId(),
                pageable
        );

        List<MiPostulacionResponseDTO> contenido = pagina.getContent()
                .stream()
                .map(this::mapToMiPostulacionResponseDTO)
                .toList();

        return new PaginaResponseDTO<>(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
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
            throw new RuntimeException("Solo puedes preseleccionar o rechazar postulaciones pendientes");
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

    private MiPostulacionResponseDTO mapToMiPostulacionResponseDTO(Postulacion postulacion) {
        OfertaLaboral oferta = postulacion.getIdOferta();

        boolean ofertaVencida = FechaPeru.estaVencida(oferta.getFechaTerminoPostulacion());
        boolean ofertaFinalizada = ofertaVencida
                || !ESTADO_OFERTA_ABIERTA.equals(oferta.getEstadoOferta());

        String estadoOfertaVisible = ofertaFinalizada
                ? "FINALIZADA"
                : ESTADO_OFERTA_ABIERTA;

        String nombreEmpleador = null;
        Long idEmpleador = null;

        if (oferta.getIdEmpleador() != null) {
            idEmpleador = oferta.getIdEmpleador().getId();
            nombreEmpleador = oferta.getIdEmpleador().getNombreComercial();
        }

        List<HabilidadesResponseDTO> habilidades = oferta.getHabilidades() == null
                ? List.of()
                : oferta.getHabilidades()
                        .stream()
                        .map(habilidad -> new HabilidadesResponseDTO(
                                habilidad.getIdHabilidad().getId(),
                                habilidad.getIdHabilidad().getNombreHabilidad()
                        ))
                        .toList();

        return new MiPostulacionResponseDTO(
                postulacion.getId(),
                oferta.getId(),
                oferta.getTitulo(),
                idEmpleador,
                nombreEmpleador,
                oferta.getIdDistrito() != null
                        ? oferta.getIdDistrito().getNombreDistrito()
                        : null,
                oferta.getIdCategoria() != null
                        ? oferta.getIdCategoria().getNombreCategoria()
                        : null,
                oferta.getIdMod() != null
                        ? oferta.getIdMod().getNombreMod()
                        : null,
                oferta.getIdExperienciaRequerida() != null
                        ? oferta.getIdExperienciaRequerida().getNombreExp()
                        : null,
                oferta.getIdDuracion() != null
                        ? oferta.getIdDuracion().getNombreDuracion()
                        : null,
                oferta.getCantidadDuracion(),
                oferta.getMontoTotal(),
                oferta.getDescripcion(),
                oferta.getTareasEspecificas(),
                habilidades,
                oferta.getFechaPublicacion(),
                oferta.getFechaTerminoPostulacion(),
                estadoOfertaVisible,
                ofertaVencida,
                ofertaFinalizada,
                postulacion.getFechaPostulacion(),
                postulacion.getEstadoPostulacion(),
                postulacion.getCvUrl() != null && !postulacion.getCvUrl().isBlank()
        );
    }

    private PostulacionResponseDTO mapToPostulacionResponseDTO(
            Postulacion postulacion,
            CatalogosEmpleado catalogos,
            SuscripcionService.PlanUsuarioResumen plan
    ) {
        UsuarioEmpleado empleado = postulacion.getIdEmpleado();

        Long idDistrito = empleado.getDistrito() != null
                ? empleado.getDistrito().getId()
                : null;
        String distrito = empleado.getDistrito() != null
                ? empleado.getDistrito().getNombreDistrito()
                : null;

        return new PostulacionResponseDTO(
                postulacion.getId(),
                empleado.getId(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getUsuario().getEmail(),
                empleado.getTelefono(),
                idDistrito,
                distrito,
                catalogos.modalidadIds(),
                catalogos.modalidades(),
                catalogos.habilidadIds(),
                catalogos.habilidades(),
                catalogos.herramientaIds(),
                catalogos.herramientas(),
                postulacion.getFechaPostulacion(),
                postulacion.getEstadoPostulacion(),
                postulacion.getCvUrl(),
                plan.prioridadPostulante(),
                plan.nombrePlan()
        );
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
    private record CatalogosEmpleado(
            List<Long> modalidadIds,
            List<String> modalidades,
            List<Long> habilidadIds,
            List<String> habilidades,
            List<Long> herramientaIds,
            List<String> herramientas
    ) {
        private static CatalogosEmpleado vacio() {
            return new CatalogosEmpleado(
                    List.of(), List.of(),
                    List.of(), List.of(),
                    List.of(), List.of()
            );
        }
    }

    private static class CatalogosEmpleadoMutable {
        private final List<Long> modalidadIds = new java.util.ArrayList<>();
        private final List<String> modalidades = new java.util.ArrayList<>();
        private final List<Long> habilidadIds = new java.util.ArrayList<>();
        private final List<String> habilidades = new java.util.ArrayList<>();
        private final List<Long> herramientaIds = new java.util.ArrayList<>();
        private final List<String> herramientas = new java.util.ArrayList<>();

        private void agregarModalidad(EmpleadoModalidad relacion) {
            modalidadIds.add(relacion.getIdMod().getId());
            modalidades.add(relacion.getIdMod().getNombreMod());
        }

        private void agregarHabilidad(EmpleadoHabilidad relacion) {
            habilidadIds.add(relacion.getIdHabilidad().getId());
            habilidades.add(relacion.getIdHabilidad().getNombreHabilidad());
        }

        private void agregarHerramienta(EmpleadoHerramienta relacion) {
            herramientaIds.add(relacion.getIdHerramienta().getId());
            herramientas.add(relacion.getIdHerramienta().getNombreHerramienta());
        }

        private CatalogosEmpleado inmutable() {
            return new CatalogosEmpleado(
                    List.copyOf(modalidadIds),
                    List.copyOf(modalidades),
                    List.copyOf(habilidadIds),
                    List.copyOf(habilidades),
                    List.copyOf(herramientaIds),
                    List.copyOf(herramientas)
            );
        }
    }

}