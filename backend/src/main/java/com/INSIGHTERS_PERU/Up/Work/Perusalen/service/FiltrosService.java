package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.FiltrosResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.RangoSalarioDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper.FiltrosMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CategoriasTrabajosRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ExperienciaRequeridaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ModalidadRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FiltrosService {

    private final CategoriasTrabajosRepository categoriaRepository;
    private final ModalidadRepository modalidadRepository;
    private final ExperienciaRequeridaRepository experienciaRepository;
    private final FiltrosMapper mapper;

    public FiltrosResponseDTO getFiltros() {

        List<SimpleDTO> categorias = categoriaRepository.findAll()
                .stream()
                .map(mapper::toDTOCategorias)
                .toList();

        List<SimpleDTO> modalidades = modalidadRepository.findAll()
                .stream()
                .map(mapper::toDTOModalidad)
                .toList();

        List<SimpleDTO> experiencias = experienciaRepository.findAll()
                .stream()
                .map(mapper::toDTOExperiencia)
                .toList();

        List<RangoSalarioDTO> rangos = List.of(
                new RangoSalarioDTO(new BigDecimal("0"), new BigDecimal("2000"), "0 - 2000"),
                new RangoSalarioDTO(new BigDecimal("2000"), new BigDecimal("4000"), "2000 - 4000"),
                new RangoSalarioDTO(new BigDecimal("4000"), new BigDecimal("6000"), "4000 - 6000")
        );

        return new FiltrosResponseDTO(categorias, modalidades, experiencias, rangos);
    }
}