package com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper;

import org.springframework.stereotype.Component;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CategoriasTrabajos;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ExperienciaRequerida;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Modalidad;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class FiltrosMapper {

    public SimpleDTO toDTOCategorias(CategoriasTrabajos cat) {
        return new SimpleDTO(cat.getId(), cat.getNombreCategoria());
    }

    public SimpleDTO toDTOModalidad(Modalidad mod) {
        return new SimpleDTO(mod.getId(), mod.getNombreMod());
    }

    public SimpleDTO toDTOExperiencia(ExperienciaRequerida exp) {
        return new SimpleDTO(exp.getId(), exp.getNombreExp());
    }
}
