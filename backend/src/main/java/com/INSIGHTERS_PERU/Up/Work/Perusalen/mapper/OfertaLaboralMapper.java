package com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.HabilidadesResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class OfertaLaboralMapper {

    public OfertaLaboralResponseDTO convertToDTO(OfertaLaboral oferta) {

        List<HabilidadesResponseDTO> habilidades = oferta.getHabilidades() == null
                ? List.of()
                : oferta.getHabilidades()
                        .stream()
                        .map(h -> new HabilidadesResponseDTO(
                                h.getIdHabilidad().getId(),
                                h.getIdHabilidad().getNombreHabilidad()
                        ))
                        .toList();

        String estadoVisible = oferta.getEstadoOferta();

        if ("ABIERTA".equals(estadoVisible)
                && (FechaPeru.estaVencida(oferta.getFechaTerminoPostulacion()))) {
            estadoVisible = "FINALIZADA";
        }

        return new OfertaLaboralResponseDTO(
                oferta.getId(),
                oferta.getTitulo(),
                oferta.getCodigoInterno(),
                oferta.getIdEmpleador().getNombreComercial(),
                oferta.getIdEmpleador().getId(),
                oferta.getIdDistrito().getNombreDistrito(),
                oferta.getIdDistrito().getId(),
                oferta.getMontoTotal(),
                oferta.getIdDuracion().getNombreDuracion(),
                oferta.getIdDuracion().getId(),
                oferta.getIdCategoria().getNombreCategoria(),
                oferta.getIdCategoria().getId(),
                oferta.getIdMod().getNombreMod(),
                oferta.getIdMod().getId(),
                oferta.getIdExperienciaRequerida().getNombreExp(),
                oferta.getIdExperienciaRequerida().getId(),
                oferta.getDescripcion(),
                oferta.getTareasEspecificas(),
                oferta.getCantidadDuracion(),
                oferta.getFechaTerminoPostulacion(),
                estadoVisible,
                habilidades,
                oferta.getFechaPublicacion()
        );
    }
}