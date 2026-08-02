package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.BadRequestException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ResourceNotFoundException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.UnauthorizedException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper.UsuarioEmpleadorMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CategoriasTrabajos;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadorCategoria;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CategoriasTrabajosRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadorCategoriaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioEmpleadorService {

    private static final String TIPO_DOC_RUC = "RUC";
    private static final String TIPO_DOC_DNI = "DNI";
    private static final String TIPO_DOC_CARNET_EXTRANJERIA = "Carnet de extranjería";
    private static final String TIPO_DOC_PASAPORTE = "Pasaporte";

    private static final String ESTADO_OFERTA_ABIERTA = "ABIERTA";
    private static final String ESTADO_OFERTA_FINALIZADA = "FINALIZADA";

    private final UsuarioEmpleadorRepository usuarioEmpleadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEmpleadorMapper usuarioEmpleadorMapper;

    private final CategoriasTrabajosRepository categoriasTrabajosRepository;
    private final EmpleadorCategoriaRepository empleadorCategoriaRepository;

    private final OfertaLaboralRepository ofertaLaboralRepository;

    private final S3StorageService s3StorageService;

    @Transactional
    public void crearPerfil(UsuarioEmpleadorRequestDTO dto, Long idUsuario, MultipartFile logo) {

        normalizarDto(dto);
        validarDatosPerfil(dto);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.isEsEmpleador()) {
            throw new UnauthorizedException("El usuario no tiene permisos de empleador");
        }

        if (usuarioEmpleadorRepository.findByUsuarioId(idUsuario).isPresent()) {
            throw new ConflictException("El usuario ya tiene un perfil de empleador");
        }

        if (usuarioEmpleadorRepository.existsByNumDoc(dto.getNumDoc())) {
            throw new ConflictException("El número de documento ya está registrado");
        }

        UsuarioEmpleador empleador = usuarioEmpleadorMapper.toEntity(dto);

        empleador.setUsuario(usuario);

        if (logo != null && !logo.isEmpty()) {
            String logoKey = s3StorageService.subirLogoEmpleador(logo, idUsuario);
            empleador.setLogoEmpleador(logoKey);
        }

        UsuarioEmpleador empleadorGuardado = usuarioEmpleadorRepository.save(empleador);

        guardarCategorias(empleadorGuardado, dto.getCategoriasId());
    }

    @Transactional(readOnly = true)
    public UsuarioEmpleadorResponseDTO obtenerPerfil(Long idUsuario) {

        UsuarioEmpleador empleador = usuarioEmpleadorRepository
                .findByUsuarioId(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleador no encontrado"));

        UsuarioEmpleadorResponseDTO response =
                usuarioEmpleadorMapper.toResponseDTO(empleador);

        llenarCategorias(response, empleador);
        llenarModalidadesContratacion(response, empleador);
        llenarTrabajos(response, empleador);

        return response;
    }

    @Transactional
    public void editarPerfil(UsuarioEmpleadorRequestDTO dto, Long idUsuario, MultipartFile logo) {

        normalizarDto(dto);
        validarDatosPerfil(dto);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.isEsEmpleador()) {
            throw new UnauthorizedException("El usuario no tiene permisos de empleador");
        }

        UsuarioEmpleador empleador = usuarioEmpleadorRepository
                .findByUsuarioId(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleador no encontrado"));

        if (!empleador.getNumDoc().equals(dto.getNumDoc())
                && usuarioEmpleadorRepository.existsByNumDoc(dto.getNumDoc())) {
            throw new ConflictException("El número de documento ya está registrado");
        }

        String logoAnterior = empleador.getLogoEmpleador();
        String nuevoLogo = null;

        if (logo != null && !logo.isEmpty()) {
            nuevoLogo = s3StorageService.subirLogoEmpleador(logo, idUsuario);
        }

        usuarioEmpleadorMapper.updateEntity(empleador, dto);

        if (nuevoLogo != null) {
            empleador.setLogoEmpleador(nuevoLogo);
        } else {
            empleador.setLogoEmpleador(logoAnterior);
        }

        usuarioEmpleadorRepository.save(empleador);

        if (nuevoLogo != null) {
            eliminarArchivoAnteriorSiCorresponde(logoAnterior, "logo anterior del empleador");
        }

        empleadorCategoriaRepository.deleteByEmpleadorId(empleador.getId());

        guardarCategorias(empleador, dto.getCategoriasId());
    }


    @Transactional(readOnly = true)
    public UsuarioEmpleador obtenerPerfilEntidadPorId(Long idEmpleador) {

        return usuarioEmpleadorRepository
                .findById(idEmpleador)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleador no encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioEmpleadorResponseDTO obtenerPerfilPublico(Long idEmpleador) {

        UsuarioEmpleador empleador = usuarioEmpleadorRepository
                .findById(idEmpleador)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Perfil de empleador no encontrado"));

        UsuarioEmpleadorResponseDTO response =
                usuarioEmpleadorMapper.toResponseDTO(empleador);

        llenarCategorias(response, empleador);
        llenarModalidadesContratacion(response, empleador);
        llenarTrabajos(response, empleador);

        return response;
    }

    private void eliminarArchivoAnteriorSiCorresponde(String rutaAnterior, String descripcion) {
        if (rutaAnterior == null || rutaAnterior.isBlank()) {
            return;
        }

        try {
            s3StorageService.eliminarArchivo(rutaAnterior);
        } catch (Exception e) {
            System.out.println("No se pudo eliminar " + descripcion + ": " + e.getMessage());
        }
    }

    private void validarDatosPerfil(UsuarioEmpleadorRequestDTO dto) {
        validarCamposObligatorios(dto);
        validarAniosOperacion(dto.getAniosOperacion());
        validarDocumento(dto.getTipoDoc(), dto.getNumDoc());
        validarContactoOpcional(dto);
    }

    private void validarCamposObligatorios(UsuarioEmpleadorRequestDTO dto) {

        if (dto.getTipoEmpleador() == null || dto.getTipoEmpleador().isBlank()) {
            throw new BadRequestException("El tipo de empleador es obligatorio");
        }

        if (dto.getNombreComercial() == null || dto.getNombreComercial().isBlank()) {
            throw new BadRequestException("El nombre comercial es obligatorio");
        }

        if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
            throw new BadRequestException("La razón social o nombre legal es obligatorio");
        }

        if (dto.getTipoDoc() == null || dto.getTipoDoc().isBlank()) {
            throw new BadRequestException("El tipo de documento es obligatorio");
        }

        if (dto.getNumDoc() == null || dto.getNumDoc().isBlank()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }

        if (dto.getAniosOperacion() == null) {
            throw new BadRequestException("Los años de operación son obligatorios");
        }
    }

    private void validarAniosOperacion(Integer aniosOperacion) {

        if (aniosOperacion == null) {
            throw new BadRequestException("Los años de operación son obligatorios");
        }

        if (aniosOperacion < 0) {
            throw new BadRequestException("Los años de operación no pueden ser negativos");
        }

        if (aniosOperacion > 200) {
            throw new BadRequestException("Los años de operación no tienen un valor válido");
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
            case TIPO_DOC_RUC -> {
                if (!documento.matches("\\d{11}")) {
                    throw new BadRequestException("El RUC debe tener exactamente 11 dígitos numéricos");
                }
            }

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

    private void validarContactoOpcional(UsuarioEmpleadorRequestDTO dto) {

        if (dto.getCorreoContacto() != null && !dto.getCorreoContacto().isBlank()) {
            if (!dto.getCorreoContacto().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")) {
                throw new BadRequestException("El correo de contacto no tiene un formato válido");
            }
        }

        if (dto.getTelefonoContacto() != null && !dto.getTelefonoContacto().isBlank()) {
            if (!dto.getTelefonoContacto().matches("^[0-9+\\s]{6,20}$")) {
                throw new BadRequestException("El teléfono de contacto no tiene un formato válido");
            }
        }

        if (dto.getSitioWeb() != null && !dto.getSitioWeb().isBlank()) {
            if (!dto.getSitioWeb().matches("^(https?://)?([\\w-]+\\.)+[\\w-]{2,}(/.*)?$")) {
                throw new BadRequestException("El sitio web no tiene un formato válido");
            }
        }
    }

    private void normalizarDto(UsuarioEmpleadorRequestDTO dto) {

        if (dto.getTipoEmpleador() != null) {
            dto.setTipoEmpleador(dto.getTipoEmpleador().trim());
        }

        if (dto.getNombreComercial() != null) {
            dto.setNombreComercial(dto.getNombreComercial().trim());
        }

        if (dto.getRazonSocial() != null) {
            dto.setRazonSocial(dto.getRazonSocial().trim());
        }

        if (dto.getTipoDoc() != null) {
            dto.setTipoDoc(dto.getTipoDoc().trim());
        }

        if (dto.getNumDoc() != null) {
            dto.setNumDoc(dto.getNumDoc().trim());
        }

        if (dto.getLogoEmpleador() != null) {
            dto.setLogoEmpleador(dto.getLogoEmpleador().trim());
        }

        if (dto.getDescripcionNegocio() != null) {
            dto.setDescripcionNegocio(dto.getDescripcionNegocio().trim());
        }

        if (dto.getSitioWeb() != null) {
            dto.setSitioWeb(dto.getSitioWeb().trim());
        }

        if (dto.getCorreoContacto() != null) {
            dto.setCorreoContacto(dto.getCorreoContacto().trim());
        }

        if (dto.getTelefonoContacto() != null) {
            dto.setTelefonoContacto(dto.getTelefonoContacto().trim());
        }
    }

    private void guardarCategorias(UsuarioEmpleador empleador, List<Long> categoriasId) {

        if (categoriasId == null || categoriasId.isEmpty()) {
            return;
        }

        List<Long> idsUnicos = categoriasId.stream()
                .distinct()
                .toList();

        List<CategoriasTrabajos> categorias =
                categoriasTrabajosRepository.findAllById(idsUnicos);

        if (categorias.size() != idsUnicos.size()) {
            throw new ResourceNotFoundException("Una o más categorías no existen");
        }

        List<EmpleadorCategoria> relaciones = categorias.stream()
                .map(categoria -> {
                    EmpleadorCategoria relacion = new EmpleadorCategoria();
                    relacion.setEmpleador(empleador);
                    relacion.setCategoria(categoria);
                    return relacion;
                })
                .toList();

        empleadorCategoriaRepository.saveAll(relaciones);
    }

    private void llenarCategorias(
            UsuarioEmpleadorResponseDTO response,
            UsuarioEmpleador empleador
    ) {

        List<String> categorias = empleadorCategoriaRepository
                .findByEmpleadorId(empleador.getId())
                .stream()
                .map(relacion -> relacion.getCategoria().getNombreCategoria())
                .toList();

        response.setCategorias(categorias);
    }

    private void llenarModalidadesContratacion(
            UsuarioEmpleadorResponseDTO response,
            UsuarioEmpleador empleador
    ) {

        List<String> modalidades = ofertaLaboralRepository
                .findModalidadesContratacionByEmpleadorId(empleador.getId());

        response.setModalidadesContratacion(modalidades);
    }

    private void llenarTrabajos(
            UsuarioEmpleadorResponseDTO response,
            UsuarioEmpleador empleador
    ) {

        List<String> estadosActivos = List.of(ESTADO_OFERTA_ABIERTA);

        List<OfertaLaboral> trabajosActivos =
                ofertaLaboralRepository.findByIdEmpleadorIdAndEstadoOfertaIn(
                        empleador.getId(),
                        estadosActivos
                );

        List<OfertaLaboral> trabajosFinalizados =
                ofertaLaboralRepository.findByIdEmpleadorIdAndEstadoOferta(
                        empleador.getId(),
                        ESTADO_OFERTA_FINALIZADA
                );

        response.setTrabajosActivos(trabajosActivos.size());
        response.setTrabajosFinalizados(trabajosFinalizados.size());

        response.setTrabajosActivosDetalle(
                trabajosActivos.stream()
                        .map(usuarioEmpleadorMapper::toTrabajoPerfilDTO)
                        .toList()
        );

        response.setTrabajosFinalizadosDetalle(
                trabajosFinalizados.stream()
                        .map(usuarioEmpleadorMapper::toTrabajoPerfilDTO)
                        .toList()
        );
    }
}