package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.BadRequestException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ResourceNotFoundException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.UnauthorizedException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper.UsuarioEmpleadoMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CategoriasTrabajos;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Distrito;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoCategoria;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHabilidad;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHerramienta;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoModalidad;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Habilidades;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Herramientas;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Modalidad;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CategoriasTrabajosRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.DistritoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoCategoriaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoHabilidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoHerramientaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.HabilidadesRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.HerramientasRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioEmpleadoService {

    private static final int EDAD_MINIMA = 18;

    private static final String TIPO_DOC_DNI = "DNI";
    private static final String TIPO_DOC_CARNET_EXTRANJERIA = "Carnet de extranjería";
    private static final String TIPO_DOC_PASAPORTE = "Pasaporte";

    private final UsuarioEmpleadoRepository usuarioEmpleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DistritoRepository distritoRepository;
    private final UsuarioEmpleadoMapper usuarioEmpleadoMapper;

    private final HabilidadesRepository habilidadesRepository;
    private final CategoriasTrabajosRepository categoriasTrabajosRepository;
    private final HerramientasRepository herramientasRepository;
    private final ModalidadRepository modalidadRepository;

    private final EmpleadoHabilidadRepository empleadoHabilidadRepository;
    private final EmpleadoCategoriaRepository empleadoCategoriaRepository;
    private final EmpleadoHerramientaRepository empleadoHerramientaRepository;
    private final EmpleadoModalidadRepository empleadoModalidadRepository;

    private final OfertaLaboralRepository ofertaLaboralRepository;

    private final S3StorageService s3StorageService;

    @Transactional
    public void crearPerfil(UsuarioEmpleadoRequestDTO dto, Long idUsuario, MultipartFile cv) {

        normalizarDto(dto);
        validarDatosPerfil(dto);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.isEsEmpleado()) {
            throw new UnauthorizedException("El usuario no tiene permisos de empleado");
        }

        if (usuarioEmpleadoRepository.findByUsuarioId(idUsuario).isPresent()) {
            throw new ConflictException("El usuario ya tiene un perfil de empleado");
        }

        if (usuarioEmpleadoRepository.existsByNumDoc(dto.getNumDoc())) {
            throw new ConflictException("El número de documento ya está registrado");
        }

        Distrito distrito = distritoRepository.findById(dto.getIdDistrito())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Distrito no encontrado"));

        UsuarioEmpleado empleado = usuarioEmpleadoMapper.toEntity(dto);

        empleado.setUsuario(usuario);
        empleado.setDistrito(distrito);

        if (cv != null && !cv.isEmpty()) {
            String cvKey = s3StorageService.subirCvPerfil(cv, idUsuario);
            empleado.setCurriculum(cvKey);
        }

        UsuarioEmpleado empleadoGuardado = usuarioEmpleadoRepository.save(empleado);

        guardarHabilidades(empleadoGuardado, dto.getHabilidadesId());
        guardarCategorias(empleadoGuardado, dto.getCategoriasId());
        guardarHerramientas(empleadoGuardado, dto.getHerramientasId());
        guardarModalidades(empleadoGuardado, dto.getModalidadesId());
    }

    @Transactional(readOnly = true)
    public UsuarioEmpleadoResponseDTO obtenerPerfil(Long idUsuario) {

        UsuarioEmpleado empleado = usuarioEmpleadoRepository
                .findByUsuarioId(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleado no encontrado"));

        UsuarioEmpleadoResponseDTO response =
                usuarioEmpleadoMapper.toResponseDTO(empleado);

        llenarRelaciones(response, empleado);
        llenarTrabajos(response, empleado);

        return response;
    }

    @Transactional(readOnly = true)
    public UsuarioEmpleado obtenerPerfilEntidad(Long idUsuario) {

        return usuarioEmpleadoRepository
                .findByUsuarioId(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleado no encontrado"));
    }

    @Transactional
    public void editarPerfil(UsuarioEmpleadoRequestDTO dto, Long idUsuario, MultipartFile cv) {

        normalizarDto(dto);
        validarDatosPerfil(dto);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.isEsEmpleado()) {
            throw new UnauthorizedException("El usuario no tiene permisos de empleado");
        }

        UsuarioEmpleado empleado = usuarioEmpleadoRepository
                .findByUsuarioId(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleado no encontrado"));

        if (!empleado.getNumDoc().equals(dto.getNumDoc())
                && usuarioEmpleadoRepository.existsByNumDoc(dto.getNumDoc())) {
            throw new ConflictException("El número de documento ya está registrado");
        }

        Distrito distrito = distritoRepository.findById(dto.getIdDistrito())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Distrito no encontrado"));

        String curriculumAnterior = empleado.getCurriculum();
        String nuevoCurriculum = null;

        if (cv != null && !cv.isEmpty()) {
            nuevoCurriculum = s3StorageService.subirCvPerfil(cv, idUsuario);
        }

        usuarioEmpleadoMapper.updateEntity(empleado, dto);
        empleado.setDistrito(distrito);

        if (nuevoCurriculum != null) {
            empleado.setCurriculum(nuevoCurriculum);
        } else {
            empleado.setCurriculum(curriculumAnterior);
        }

        usuarioEmpleadoRepository.save(empleado);

        if (nuevoCurriculum != null) {
            eliminarCvAnteriorSiCorresponde(curriculumAnterior);
        }

        empleadoHabilidadRepository.deleteByEmpleadoId(empleado.getId());
        empleadoCategoriaRepository.deleteByEmpleadoId(empleado.getId());
        empleadoHerramientaRepository.deleteByEmpleadoId(empleado.getId());
        empleadoModalidadRepository.deleteByEmpleadoId(empleado.getId());

        guardarHabilidades(empleado, dto.getHabilidadesId());
        guardarCategorias(empleado, dto.getCategoriasId());
        guardarHerramientas(empleado, dto.getHerramientasId());
        guardarModalidades(empleado, dto.getModalidadesId());
    }

    private void eliminarCvAnteriorSiCorresponde(String curriculumAnterior) {
        if (curriculumAnterior == null || curriculumAnterior.isBlank()) {
            return;
        }

        try {
            s3StorageService.eliminarArchivo(curriculumAnterior);
        } catch (Exception e) {
            System.out.println("No se pudo eliminar el CV anterior del perfil: " + e.getMessage());
        }
    }

    private void validarDatosPerfil(UsuarioEmpleadoRequestDTO dto) {
        validarMayorEdad(dto.getFechaNacimiento());
        validarDocumento(dto.getTipoDoc(), dto.getNumDoc());
    }

    private void validarMayorEdad(LocalDate fechaNacimiento) {

        if (fechaNacimiento == null) {
            throw new BadRequestException("La fecha de nacimiento es obligatoria");
        }

        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento no puede ser futura");
        }

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();

        if (edad < EDAD_MINIMA) {
            throw new BadRequestException("Debes ser mayor de edad para tener un perfil de empleado");
        }
    }

    private void validarDocumento(String tipoDoc, String numDoc) {

        if (tipoDoc == null || tipoDoc.isBlank()) {
            throw new BadRequestException("El tipo de documento es obligatorio");
        }

        if (numDoc == null || numDoc.isBlank()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }

        String documento = numDoc.trim();

        switch (tipoDoc) {
            case TIPO_DOC_DNI -> {
                if (!documento.matches("\\d{8}")) {
                    throw new BadRequestException("El DNI debe tener exactamente 8 dígitos numéricos");
                }
            }

            case TIPO_DOC_CARNET_EXTRANJERIA -> {
                if (!documento.matches("[A-Za-z0-9]{9,12}")) {
                    throw new BadRequestException("El carnet de extranjería debe tener entre 9 y 12 caracteres alfanuméricos");
                }
            }

            case TIPO_DOC_PASAPORTE -> {
                if (!documento.matches("[A-Za-z0-9]{6,12}")) {
                    throw new BadRequestException("El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos");
                }
            }

            default -> throw new BadRequestException("Tipo de documento no válido");
        }
    }

    private void normalizarDto(UsuarioEmpleadoRequestDTO dto) {

        if (dto.getNombre() != null) {
            dto.setNombre(dto.getNombre().trim());
        }

        if (dto.getApellido() != null) {
            dto.setApellido(dto.getApellido().trim());
        }

        if (dto.getTipoDoc() != null) {
            dto.setTipoDoc(dto.getTipoDoc().trim());
        }

        if (dto.getNumDoc() != null) {
            dto.setNumDoc(dto.getNumDoc().trim());
        }

        if (dto.getGenero() != null) {
            dto.setGenero(dto.getGenero().trim());
        }

        if (dto.getTelefono() != null) {
            dto.setTelefono(dto.getTelefono().trim());
        }

        if (dto.getNacionalidad() != null) {
            dto.setNacionalidad(dto.getNacionalidad().trim());
        }

        if (dto.getDescripcion() != null) {
            dto.setDescripcion(dto.getDescripcion().trim());
        }

        if (dto.getCurriculum() != null) {
            dto.setCurriculum(dto.getCurriculum().trim());
        }

        if (dto.getFotoPerfil() != null) {
            dto.setFotoPerfil(dto.getFotoPerfil().trim());
        }

        if (dto.getDisponibilidadEquipo() != null) {
            dto.setDisponibilidadEquipo(dto.getDisponibilidadEquipo().trim());
        }
    }

    private void llenarRelaciones(UsuarioEmpleadoResponseDTO response, UsuarioEmpleado empleado) {

        List<String> habilidades = empleadoHabilidadRepository
                .findByIdEmpleadoId(empleado.getId())
                .stream()
                .map(relacion -> relacion.getIdHabilidad().getNombreHabilidad())
                .toList();

        List<String> categorias = empleadoCategoriaRepository
                .findByIdEmpleadoId(empleado.getId())
                .stream()
                .map(relacion -> relacion.getIdCategoria().getNombreCategoria())
                .toList();

        List<String> herramientas = empleadoHerramientaRepository
                .findByIdEmpleadoId(empleado.getId())
                .stream()
                .map(relacion -> relacion.getIdHerramienta().getNombreHerramienta())
                .toList();

        List<String> modalidades = empleadoModalidadRepository
                .findByIdEmpleadoId(empleado.getId())
                .stream()
                .map(relacion -> relacion.getIdMod().getNombreMod())
                .toList();

        response.setHabilidades(habilidades);
        response.setCategorias(categorias);
        response.setHerramientas(herramientas);
        response.setModalidades(modalidades);
    }

    private void llenarTrabajos(UsuarioEmpleadoResponseDTO response, UsuarioEmpleado empleado) {

        List<OfertaLaboral> trabajosActivos =
                ofertaLaboralRepository.findByEmpleadoSeleccionadoIdAndEstadoOferta(
                        empleado.getId(),
                        "ABIERTA"
                );

        List<OfertaLaboral> trabajosFinalizados =
                ofertaLaboralRepository.findByEmpleadoSeleccionadoIdAndEstadoOferta(
                        empleado.getId(),
                        "FINALIZADA"
                );

        response.setTrabajosActivos(trabajosActivos.size());
        response.setTrabajosFinalizados(trabajosFinalizados.size());

        response.setTrabajosActivosDetalle(
                trabajosActivos.stream()
                        .map(usuarioEmpleadoMapper::toTrabajoPerfilDTO)
                        .toList()
        );

        response.setTrabajosFinalizadosDetalle(
                trabajosFinalizados.stream()
                        .map(usuarioEmpleadoMapper::toTrabajoPerfilDTO)
                        .toList()
        );
    }

    private void guardarHabilidades(UsuarioEmpleado empleado, List<Long> habilidadesId) {

        if (habilidadesId == null || habilidadesId.isEmpty()) {
            return;
        }

        List<Long> idsUnicos = habilidadesId.stream()
                .distinct()
                .toList();

        List<Habilidades> habilidades = habilidadesRepository.findAllById(idsUnicos);

        if (habilidades.size() != idsUnicos.size()) {
            throw new ResourceNotFoundException("Una o más habilidades no existen");
        }

        List<EmpleadoHabilidad> relaciones = habilidades.stream()
                .map(habilidad -> {
                    EmpleadoHabilidad relacion = new EmpleadoHabilidad();
                    relacion.setIdEmpleado(empleado);
                    relacion.setIdHabilidad(habilidad);
                    return relacion;
                })
                .toList();

        empleadoHabilidadRepository.saveAll(relaciones);
    }

    private void guardarCategorias(UsuarioEmpleado empleado, List<Long> categoriasId) {

        if (categoriasId == null || categoriasId.isEmpty()) {
            return;
        }

        List<Long> idsUnicos = categoriasId.stream()
                .distinct()
                .toList();

        List<CategoriasTrabajos> categorias = categoriasTrabajosRepository.findAllById(idsUnicos);

        if (categorias.size() != idsUnicos.size()) {
            throw new ResourceNotFoundException("Una o más categorías no existen");
        }

        List<EmpleadoCategoria> relaciones = categorias.stream()
                .map(categoria -> {
                    EmpleadoCategoria relacion = new EmpleadoCategoria();
                    relacion.setIdEmpleado(empleado);
                    relacion.setIdCategoria(categoria);
                    return relacion;
                })
                .toList();

        empleadoCategoriaRepository.saveAll(relaciones);
    }

    private void guardarHerramientas(UsuarioEmpleado empleado, List<Long> herramientasId) {

        if (herramientasId == null || herramientasId.isEmpty()) {
            return;
        }

        List<Long> idsUnicos = herramientasId.stream()
                .distinct()
                .toList();

        List<Herramientas> herramientas = herramientasRepository.findAllById(idsUnicos);

        if (herramientas.size() != idsUnicos.size()) {
            throw new ResourceNotFoundException("Una o más herramientas no existen");
        }

        List<EmpleadoHerramienta> relaciones = herramientas.stream()
                .map(herramienta -> {
                    EmpleadoHerramienta relacion = new EmpleadoHerramienta();
                    relacion.setIdEmpleado(empleado);
                    relacion.setIdHerramienta(herramienta);
                    return relacion;
                })
                .toList();

        empleadoHerramientaRepository.saveAll(relaciones);
    }

    private void guardarModalidades(UsuarioEmpleado empleado, List<Long> modalidadesId) {

        if (modalidadesId == null || modalidadesId.isEmpty()) {
            return;
        }

        List<Long> idsUnicos = modalidadesId.stream()
                .distinct()
                .toList();

        List<Modalidad> modalidades = modalidadRepository.findAllById(idsUnicos);

        if (modalidades.size() != idsUnicos.size()) {
            throw new ResourceNotFoundException("Una o más modalidades no existen");
        }

        List<EmpleadoModalidad> relaciones = modalidades.stream()
                .map(modalidad -> {
                    EmpleadoModalidad relacion = new EmpleadoModalidad();
                    relacion.setIdEmpleado(empleado);
                    relacion.setIdMod(modalidad);
                    return relacion;
                })
                .toList();

        empleadoModalidadRepository.saveAll(relaciones);
    }

    @Transactional(readOnly = true)
    public UsuarioEmpleadoResponseDTO obtenerPerfilPublico(Long idEmpleado) {

        UsuarioEmpleado empleado = usuarioEmpleadoRepository
                .findById(idEmpleado)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleado no encontrado"));

        UsuarioEmpleadoResponseDTO response =
                usuarioEmpleadoMapper.toResponseDTO(empleado);

        llenarRelaciones(response, empleado);
        llenarTrabajos(response, empleado);

        return response;
    }
}