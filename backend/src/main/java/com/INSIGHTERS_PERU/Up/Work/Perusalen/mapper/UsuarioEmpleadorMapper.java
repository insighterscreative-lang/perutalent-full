package com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.TrabajoPerfilDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;

@Component
public class UsuarioEmpleadorMapper {

    public UsuarioEmpleador toEntity(UsuarioEmpleadorRequestDTO dto) {

        UsuarioEmpleador empleador = new UsuarioEmpleador();

        empleador.setTipoEmpleador(dto.getTipoEmpleador());
        empleador.setNombreComercial(dto.getNombreComercial());
        empleador.setRazonSocial(dto.getRazonSocial());
        empleador.setTipoDoc(dto.getTipoDoc());
        empleador.setNumDoc(dto.getNumDoc());
        empleador.setLogoEmpleador(dto.getLogoEmpleador());
        empleador.setDescripcionNegocio(dto.getDescripcionNegocio());
        empleador.setAniosOperacion(dto.getAniosOperacion());

        empleador.setSitioWeb(dto.getSitioWeb());
        empleador.setCorreoContacto(dto.getCorreoContacto());
        empleador.setTelefonoContacto(dto.getTelefonoContacto());

        return empleador;
    }

    public void updateEntity(UsuarioEmpleador empleador, UsuarioEmpleadorRequestDTO dto) {

        empleador.setTipoEmpleador(dto.getTipoEmpleador());
        empleador.setNombreComercial(dto.getNombreComercial());
        empleador.setRazonSocial(dto.getRazonSocial());
        empleador.setTipoDoc(dto.getTipoDoc());
        empleador.setNumDoc(dto.getNumDoc());
        empleador.setLogoEmpleador(dto.getLogoEmpleador());
        empleador.setDescripcionNegocio(dto.getDescripcionNegocio());
        empleador.setAniosOperacion(dto.getAniosOperacion());

        empleador.setSitioWeb(dto.getSitioWeb());
        empleador.setCorreoContacto(dto.getCorreoContacto());
        empleador.setTelefonoContacto(dto.getTelefonoContacto());
    }

    public UsuarioEmpleadorResponseDTO toResponseDTO(UsuarioEmpleador empleador) {

        UsuarioEmpleadorResponseDTO dto = new UsuarioEmpleadorResponseDTO();

        dto.setIdEmpleador(empleador.getId());

        dto.setTipoEmpleador(empleador.getTipoEmpleador());
        dto.setNombreComercial(empleador.getNombreComercial());
        dto.setRazonSocial(empleador.getRazonSocial());
        dto.setTipoDoc(empleador.getTipoDoc());
        dto.setNumDoc(empleador.getNumDoc());
        dto.setLogoEmpleador(empleador.getLogoEmpleador());
        dto.setDescripcionNegocio(empleador.getDescripcionNegocio());
        dto.setAniosOperacion(empleador.getAniosOperacion());

        dto.setSitioWeb(empleador.getSitioWeb());
        dto.setCorreoContacto(empleador.getCorreoContacto());
        dto.setTelefonoContacto(empleador.getTelefonoContacto());

        if (empleador.getUsuario() != null) {
            dto.setCorreo(empleador.getUsuario().getEmail());
        }

        dto.setCategorias(List.of());
        dto.setModalidadesContratacion(List.of());

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
        dto.setMontoTotal(oferta.getMontoTotal());

        if (oferta.getFechaPublicacion() != null) {
            dto.setFechaPublicacion(oferta.getFechaPublicacion().toString());
        }

        if (oferta.getIdEmpleador() != null) {
            dto.setEmpleador(oferta.getIdEmpleador().getNombreComercial());
        }

        if (oferta.getIdDistrito() != null) {
            dto.setDistrito(oferta.getIdDistrito().getNombreDistrito());
        }

        if (oferta.getIdCategoria() != null) {
            dto.setCategoria(oferta.getIdCategoria().getNombreCategoria());
        }

        if (oferta.getIdMod() != null) {
            dto.setModalidad(oferta.getIdMod().getNombreMod());
        }

        if (oferta.getIdExperienciaRequerida() != null) {
            dto.setExperiencia(oferta.getIdExperienciaRequerida().getNombreExp());
        }

        if (oferta.getIdDuracion().getNombreDuracion() != null) {
            dto.setTipoDuracion(oferta.getIdDuracion().getNombreDuracion());
        }

        if (oferta.getHabilidades() != null && !oferta.getHabilidades().isEmpty()) {
            dto.setHabilidades(
                    oferta.getHabilidades()
                            .stream()
                            .filter(ofertaHabilidad -> ofertaHabilidad.getIdHabilidad() != null)
                            .map(ofertaHabilidad -> new TrabajoPerfilDTO.HabilidadTrabajoDTO(
                                    ofertaHabilidad.getIdHabilidad().getId(),
                                    ofertaHabilidad.getIdHabilidad().getNombreHabilidad()
                            ))
                            .toList()
            );
        } else {
            dto.setHabilidades(List.of());
        }

        return dto;
    }
}