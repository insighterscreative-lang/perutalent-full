package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ResourceNotFoundException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.UnauthorizedException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper.OfertaLaboralMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CategoriasTrabajos;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Distrito;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ExperienciaRequerida;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Habilidades;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Modalidad;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaHabilidad;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.TipoDuracion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CategoriasTrabajosRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.DistritoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoCategoriaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ExperienciaRequeridaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.HabilidadesRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaHabilidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.TipoDuracionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.specification.OfertaSpecification;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OfertaLaboralService {

    private static final String ESTADO_ABIERTA = "ABIERTA";
    private static final String ESTADO_FINALIZADA = "FINALIZADA";

    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final OfertaLaboralMapper ofertaLaboralMapper;

    private final UsuarioEmpleadorRepository usuarioEmpleadorRepository;
    private final UsuarioEmpleadoRepository usuarioEmpleadoRepository;

    private final CategoriasTrabajosRepository categoriasTrabajosRepository;
    private final ModalidadRepository modalidadRepository;
    private final DistritoRepository distritoRepository;
    private final ExperienciaRequeridaRepository experienciaRequeridaRepository;
    private final TipoDuracionRepository tipoDuracionRepository;
    private final HabilidadesRepository habilidadesRepository;

    private final OfertaHabilidadRepository ofertaHabilidadRepository;
    private final EmpleadoCategoriaRepository empleadoCategoriaRepository;
    private final SuscripcionService suscripcionService;

    @Transactional
    public OfertaLaboralResponseDTO crearOferta(OfertaLaboralRequestDTO dto, Long idUsuario) {

        if (ofertaLaboralRepository.existsByCodigoInterno(dto.getCodigoInterno())) {
            throw new ConflictException("Ya existe una oferta laboral con ese código interno");
        }

        UsuarioEmpleador empleador = obtenerEmpleadorPorUsuario(idUsuario);

        Integer ofertasActivasActuales = ofertaLaboralRepository
                .countByIdEmpleadorIdAndEstadoOferta(empleador.getId(), ESTADO_ABIERTA);

        suscripcionService.validarPuedeCrearOferta(idUsuario, ofertasActivasActuales);

        CategoriasTrabajos categoria = obtenerCategoria(dto.getIdCategoria());
        Modalidad modalidad = obtenerModalidad(dto.getIdMod());
        Distrito distrito = obtenerDistrito(dto.getIdDistrito());
        ExperienciaRequerida experiencia = obtenerExperiencia(dto.getIdExperienciaRequerida());
        TipoDuracion duracion = obtenerDuracion(dto.getIdDuracion());

        OfertaLaboral oferta = new OfertaLaboral();

        oferta.setIdEmpleador(empleador);
        oferta.setIdCategoria(categoria);
        oferta.setIdMod(modalidad);
        oferta.setIdDistrito(distrito);
        oferta.setIdExperienciaRequerida(experiencia);
        oferta.setIdDuracion(duracion);

        oferta.setTitulo(dto.getTitulo());
        oferta.setCodigoInterno(dto.getCodigoInterno());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setTareasEspecificas(dto.getTareasEspecificas());
        oferta.setCantidadDuracion(dto.getCantidadDuracion());
        oferta.setMontoTotal(dto.getMontoTotal());
        oferta.setFechaTerminoPostulacion(dto.getFechaTerminoPostulacion());

        oferta.setFechaPublicacion(LocalDate.now());
        oferta.setEstadoOferta(ESTADO_ABIERTA);
        oferta.setEmpleadoSeleccionado(null);

        OfertaLaboral ofertaGuardada = ofertaLaboralRepository.save(oferta);

        guardarHabilidadesOferta(ofertaGuardada, dto.getHabilidadesId());

        return obtenerOfertaConHabilidadesDTO(ofertaGuardada.getId());
    }

    @Transactional
    public OfertaLaboralResponseDTO editarOferta(
            Long idOferta,
            OfertaLaboralRequestDTO dto,
            Long idUsuario
    ) {

        UsuarioEmpleador empleador = obtenerEmpleadorPorUsuario(idUsuario);

        OfertaLaboral oferta = ofertaLaboralRepository.findById(idOferta)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta laboral no encontrada"));

        validarPropietarioOferta(oferta, empleador);

        if (!ESTADO_ABIERTA.equals(oferta.getEstadoOferta())) {
            throw new ConflictException("Solo se pueden editar ofertas en estado ABIERTA");
        }

        if (!oferta.getCodigoInterno().equals(dto.getCodigoInterno())
                && ofertaLaboralRepository.existsByCodigoInterno(dto.getCodigoInterno())) {
            throw new ConflictException("Ya existe una oferta laboral con ese código interno");
        }

        CategoriasTrabajos categoria = obtenerCategoria(dto.getIdCategoria());
        Modalidad modalidad = obtenerModalidad(dto.getIdMod());
        Distrito distrito = obtenerDistrito(dto.getIdDistrito());
        ExperienciaRequerida experiencia = obtenerExperiencia(dto.getIdExperienciaRequerida());
        TipoDuracion duracion = obtenerDuracion(dto.getIdDuracion());

        oferta.setIdCategoria(categoria);
        oferta.setIdMod(modalidad);
        oferta.setIdDistrito(distrito);
        oferta.setIdExperienciaRequerida(experiencia);
        oferta.setIdDuracion(duracion);

        oferta.setTitulo(dto.getTitulo());
        oferta.setCodigoInterno(dto.getCodigoInterno());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setTareasEspecificas(dto.getTareasEspecificas());
        oferta.setCantidadDuracion(dto.getCantidadDuracion());
        oferta.setMontoTotal(dto.getMontoTotal());
        oferta.setFechaTerminoPostulacion(dto.getFechaTerminoPostulacion());

        OfertaLaboral ofertaGuardada = ofertaLaboralRepository.save(oferta);

        ofertaHabilidadRepository.deleteByOfertaId(ofertaGuardada.getId());
        guardarHabilidadesOferta(ofertaGuardada, dto.getHabilidadesId());

        return obtenerOfertaConHabilidadesDTO(ofertaGuardada.getId());
    }

    @Transactional
    public OfertaLaboralResponseDTO finalizarOferta(Long idOferta, Long idUsuario) {

        UsuarioEmpleador empleador = obtenerEmpleadorPorUsuario(idUsuario);

        OfertaLaboral oferta = ofertaLaboralRepository.findById(idOferta)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta laboral no encontrada"));

        validarPropietarioOferta(oferta, empleador);

        if (!ESTADO_ABIERTA.equals(oferta.getEstadoOferta())) {
            throw new ConflictException("Solo se pueden finalizar ofertas en estado ABIERTA");
        }

        oferta.setEstadoOferta(ESTADO_FINALIZADA);

        OfertaLaboral ofertaGuardada = ofertaLaboralRepository.save(oferta);

        return obtenerOfertaConHabilidadesDTO(ofertaGuardada.getId());
    }

    @Transactional(readOnly = true)
    public List<OfertaLaboralResponseDTO> getOfertasLaboralesActivas() {

        return ofertaLaboralRepository.findActivasWithHabilidades(ESTADO_ABIERTA)
                .stream()
                .map(ofertaLaboralMapper::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OfertaLaboralResponseDTO getOfertaById(Long id) {

        OfertaLaboral oferta = ofertaLaboralRepository.findByIdWithHabilidades(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la oferta laboral"));

        return ofertaLaboralMapper.convertToDTO(oferta);
    }

    @Transactional(readOnly = true)
    public OfertaLaboralResponseDTO getOfertaActivaById(Long id) {

        OfertaLaboral oferta = ofertaLaboralRepository.findByIdWithHabilidades(id)
                .filter(o -> ESTADO_ABIERTA.equals(o.getEstadoOferta()))
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una oferta laboral abierta"));

        return ofertaLaboralMapper.convertToDTO(oferta);
    }

    @Transactional
    public List<OfertaLaboralResponseDTO> getOfertasParaTi(Long idUsuario) {

        UsuarioEmpleado empleado = usuarioEmpleadoRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new UnauthorizedException("El usuario no tiene perfil de empleado"));

        Set<Long> categoriasEmpleado = empleadoCategoriaRepository.findByIdEmpleadoId(empleado.getId())
                .stream()
                .map(relacion -> relacion.getIdCategoria().getId())
                .collect(Collectors.toSet());

        if (categoriasEmpleado.isEmpty()) {
            return List.of();
        }

        Integer limiteRecomendaciones = suscripcionService.obtenerLimiteRecomendaciones(idUsuario);

        Stream<OfertaLaboral> streamRecomendaciones = ofertaLaboralRepository.findActivasWithHabilidades(ESTADO_ABIERTA)
                .stream()
                .filter(oferta -> oferta.getIdCategoria() != null)
                .filter(oferta -> categoriasEmpleado.contains(oferta.getIdCategoria().getId()))
                .sorted(Comparator.comparing(OfertaLaboral::getFechaPublicacion).reversed());

        if (limiteRecomendaciones != null) {
            streamRecomendaciones = streamRecomendaciones.limit(limiteRecomendaciones);
        }

        return streamRecomendaciones
                .map(ofertaLaboralMapper::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaLaboralResponseDTO> filtrarOfertas(
            Long categoria,
            Long modalidad,
            Long experiencia,
            BigDecimal montoMin,
            BigDecimal montoMax,
            String palabraClave,
            String ubicacion,
            String sortBy,
            String order
    ) {

        Specification<OfertaLaboral> spec = Specification.allOf();

        if (categoria != null) {
            spec = spec.and(OfertaSpecification.conCategorias(List.of(categoria)));
        }

        if (modalidad != null) {
            spec = spec.and(OfertaSpecification.conModalidades(List.of(modalidad)));
        }

        if (experiencia != null) {
            spec = spec.and(OfertaSpecification.conExperiencia(List.of(experiencia)));
        }

        if (montoMin != null || montoMax != null) {
            spec = spec.and(OfertaSpecification.conSalario(montoMin, montoMax));
        }

        if (palabraClave != null && !palabraClave.isBlank()) {
            spec = spec.and(OfertaSpecification.conKeyword(palabraClave));
        }

        if (ubicacion != null && !ubicacion.isBlank()) {
            spec = spec.and(OfertaSpecification.porUbicacion(ubicacion));
        }

        spec = spec.and(OfertaSpecification.estadoActiva());

        Sort sort = Sort.unsorted();

        if (sortBy != null && order != null) {
            Sort.Direction direction = order.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            if (sortBy.equals("monto")) {
                sort = Sort.by(direction, "montoTotal");
            } else if (sortBy.equals("fecha")) {
                sort = Sort.by(direction, "fechaPublicacion");
            }
        }

        return ofertaLaboralRepository.findAll(spec, sort)
                .stream()
                .map(ofertaLaboralMapper::convertToDTO)
                .toList();
    }

    private UsuarioEmpleador obtenerEmpleadorPorUsuario(Long idUsuario) {
        return usuarioEmpleadorRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new UnauthorizedException("El usuario no tiene perfil de empleador"));
    }

    private void validarPropietarioOferta(OfertaLaboral oferta, UsuarioEmpleador empleador) {
        if (!oferta.getIdEmpleador().getId().equals(empleador.getId())) {
            throw new UnauthorizedException("No tienes permisos para modificar esta oferta");
        }
    }

    private CategoriasTrabajos obtenerCategoria(Long idCategoria) {
        return categoriasTrabajosRepository.findById(idCategoria)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría no existe"));
    }

    private Modalidad obtenerModalidad(Long idModalidad) {
        return modalidadRepository.findById(idModalidad)
                .orElseThrow(() -> new ResourceNotFoundException("La modalidad no existe"));
    }

    private Distrito obtenerDistrito(Long idDistrito) {
        return distritoRepository.findById(idDistrito)
                .orElseThrow(() -> new ResourceNotFoundException("El distrito no existe"));
    }

    private ExperienciaRequerida obtenerExperiencia(Long idExperiencia) {
        return experienciaRequeridaRepository.findById(idExperiencia)
                .orElseThrow(() -> new ResourceNotFoundException("La experiencia requerida no existe"));
    }

    private TipoDuracion obtenerDuracion(Long idDuracion) {
        return tipoDuracionRepository.findById(idDuracion)
                .orElseThrow(() -> new ResourceNotFoundException("El tipo de duración no existe"));
    }

    private void guardarHabilidadesOferta(
            OfertaLaboral oferta,
            List<Long> habilidadesId
    ) {

        if (habilidadesId == null || habilidadesId.isEmpty()) {
            return;
        }

        List<OfertaHabilidad> ofertaHabilidades = habilidadesId
                .stream()
                .distinct()
                .map(idHabilidad -> {
                    Habilidades habilidad = habilidadesRepository.findById(idHabilidad)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "La habilidad con ID " + idHabilidad + " no existe"
                            ));

                    OfertaHabilidad ofertaHabilidad = new OfertaHabilidad();
                    ofertaHabilidad.setIdOferta(oferta);
                    ofertaHabilidad.setIdHabilidad(habilidad);

                    return ofertaHabilidad;
                })
                .toList();

        ofertaHabilidadRepository.saveAll(ofertaHabilidades);
    }

    private OfertaLaboralResponseDTO obtenerOfertaConHabilidadesDTO(Long idOferta) {
        OfertaLaboral ofertaConHabilidades = ofertaLaboralRepository.findByIdWithHabilidades(idOferta)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la oferta laboral"));

        return ofertaLaboralMapper.convertToDTO(ofertaConHabilidades);
    }
}