package com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.TrabajoPerfilDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;

@Component
public class UsuarioEmpleadoMapper {

    public UsuarioEmpleado toEntity(UsuarioEmpleadoRequestDTO dto) {

        UsuarioEmpleado empleado = new UsuarioEmpleado();

        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setTipoDoc(dto.getTipoDoc());
        empleado.setNumDoc(dto.getNumDoc());
        empleado.setFechaNacimiento(dto.getFechaNacimiento());
        empleado.setGenero(dto.getGenero());
        empleado.setTelefono(dto.getTelefono());
        empleado.setNacionalidad(dto.getNacionalidad());
        empleado.setDescripcion(dto.getDescripcion());
        empleado.setCurriculum(dto.getCurriculum());
        empleado.setFotoPerfil(dto.getFotoPerfil());
        empleado.setDisponibilidadEquipo(dto.getDisponibilidadEquipo());

        if (dto.getIdiomas() != null && !dto.getIdiomas().isEmpty()) {
            empleado.setIdiomas(String.join(",", dto.getIdiomas()));
        } else {
            empleado.setIdiomas("");
        }

        return empleado;
    }

    public void updateEntity(UsuarioEmpleado empleado, UsuarioEmpleadoRequestDTO dto) {

        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setTipoDoc(dto.getTipoDoc());
        empleado.setNumDoc(dto.getNumDoc());
        empleado.setFechaNacimiento(dto.getFechaNacimiento());
        empleado.setGenero(dto.getGenero());
        empleado.setTelefono(dto.getTelefono());
        empleado.setNacionalidad(dto.getNacionalidad());
        empleado.setDescripcion(dto.getDescripcion());
        empleado.setCurriculum(dto.getCurriculum());
        empleado.setFotoPerfil(dto.getFotoPerfil());
        empleado.setDisponibilidadEquipo(dto.getDisponibilidadEquipo());

        if (dto.getIdiomas() != null && !dto.getIdiomas().isEmpty()) {
            empleado.setIdiomas(String.join(",", dto.getIdiomas()));
        } else {
            empleado.setIdiomas("");
        }
    }

    public UsuarioEmpleadoResponseDTO toResponseDTO(UsuarioEmpleado empleado) {

        UsuarioEmpleadoResponseDTO dto = new UsuarioEmpleadoResponseDTO();

        dto.setIdEmpleado(empleado.getId());

        dto.setNombre(empleado.getNombre());
        dto.setApellido(empleado.getApellido());

        dto.setTipoDoc(empleado.getTipoDoc());
        dto.setNumDoc(empleado.getNumDoc());

        dto.setFechaNacimiento(empleado.getFechaNacimiento());

        dto.setGenero(empleado.getGenero());
        dto.setFotoPerfil(empleado.getFotoPerfil());

        if (empleado.getDistrito() != null) {
            dto.setIdDistrito(empleado.getDistrito().getId());
            dto.setDistrito(empleado.getDistrito().getNombreDistrito());

            if (empleado.getDistrito().getProvincia() != null) {
                dto.setIdProvincia(empleado.getDistrito().getProvincia().getId());

                if (empleado.getDistrito().getProvincia().getDepartamento() != null) {
                    dto.setIdDepartamento(
                            empleado.getDistrito()
                                    .getProvincia()
                                    .getDepartamento()
                                    .getId()
                    );
                }
            }
        }

        dto.setNacionalidad(empleado.getNacionalidad());
        dto.setTelefono(empleado.getTelefono());

        if (empleado.getUsuario() != null) {
            dto.setCorreo(empleado.getUsuario().getEmail());
        }

        dto.setDescripcion(empleado.getDescripcion());
        dto.setCurriculum(empleado.getCurriculum());
        dto.setDisponibilidadEquipo(empleado.getDisponibilidadEquipo());

        if (empleado.getIdiomas() != null && !empleado.getIdiomas().isBlank()) {
            dto.setIdiomas(
                    Arrays.stream(empleado.getIdiomas().split(","))
                            .map(String::trim)
                            .filter(idioma -> !idioma.isBlank())
                            .toList()
            );
        } else {
            dto.setIdiomas(List.of());
        }

        // Estas listas se llenan luego en el service desde tablas intermedias.
        dto.setHabilidades(List.of());
        dto.setCategorias(List.of());
        dto.setHerramientas(List.of());
        dto.setModalidades(List.of());

        dto.setTrabajosActivos(0);
        dto.setTrabajosFinalizados(0);

        dto.setTrabajosActivosDetalle(List.of());
        dto.setTrabajosFinalizadosDetalle(List.of());

        return dto;
    }

    public TrabajoPerfilDTO toTrabajoPerfilDTO(OfertaLaboral oferta) {

        TrabajoPerfilDTO dto = new TrabajoPerfilDTO();

        dto.setIdOferta(oferta.getId());
        dto.setTitulo(oferta.getTitulo());
        dto.setDescripcion(oferta.getDescripcion());
        dto.setEstadoOferta(oferta.getEstadoOferta());

        if (oferta.getIdCategoria() != null) {
            dto.setCategoria(oferta.getIdCategoria().getNombreCategoria());
        }

        if (oferta.getIdMod() != null) {
            dto.setModalidad(oferta.getIdMod().getNombreMod());
        }

        return dto;
    }
}