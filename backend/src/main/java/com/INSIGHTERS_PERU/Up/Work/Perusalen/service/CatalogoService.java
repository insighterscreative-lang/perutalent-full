package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.CatalogoDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CategoriasTrabajosRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.DepartamentoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.DistritoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ExperienciaRequeridaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.HabilidadesRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.HerramientasRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ProvinciaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.TipoDuracionRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CatalogoService {

    private final DepartamentoRepository departamentoRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DistritoRepository distritoRepository;

    private final CategoriasTrabajosRepository categoriasTrabajosRepository;
    private final HabilidadesRepository habilidadesRepository;
    private final HerramientasRepository herramientasRepository;
    private final ModalidadRepository modalidadRepository;
    private final TipoDuracionRepository tipoDuracionRepository;
    private final ExperienciaRequeridaRepository experienciaRequeridaRepository;

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarDepartamentos() {
        return departamentoRepository.findAll()
                .stream()
                .map(departamento -> new CatalogoDTO(
                        departamento.getId(),
                        departamento.getNombreDepartamento()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarProvinciasPorDepartamento(Long departamentoId) {
        return provinciaRepository.findByDepartamentoId(departamentoId)
                .stream()
                .map(provincia -> new CatalogoDTO(
                        provincia.getId(),
                        provincia.getNombreProvincia()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarDistritosPorProvincia(Long provinciaId) {
        return distritoRepository.findByProvinciaId(provinciaId)
                .stream()
                .map(distrito -> new CatalogoDTO(
                        distrito.getId(),
                        distrito.getNombreDistrito()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarCategorias() {
        return categoriasTrabajosRepository.findAll()
                .stream()
                .map(categoria -> new CatalogoDTO(
                        categoria.getId(),
                        categoria.getNombreCategoria()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarHabilidades() {
        return habilidadesRepository.findAll()
                .stream()
                .map(habilidad -> new CatalogoDTO(
                        habilidad.getId(),
                        habilidad.getNombreHabilidad()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarHerramientas() {
        return herramientasRepository.findAll()
                .stream()
                .map(herramienta -> new CatalogoDTO(
                        herramienta.getId(),
                        herramienta.getNombreHerramienta()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarModalidades() {
        return modalidadRepository.findAll()
                .stream()
                .map(modalidad -> new CatalogoDTO(
                        modalidad.getId(),
                        modalidad.getNombreMod()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarTiposDuracion() {
        return tipoDuracionRepository.findAll()
                .stream()
                .map(tipo -> new CatalogoDTO(
                        tipo.getId(),
                        tipo.getNombreDuracion()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> listarExperiencias() {
        return experienciaRequeridaRepository.findAll()
                .stream()
                .map(experiencia -> new CatalogoDTO(
                        experiencia.getId(),
                        experiencia.getNombreExp()
                ))
                .toList();
    }
}