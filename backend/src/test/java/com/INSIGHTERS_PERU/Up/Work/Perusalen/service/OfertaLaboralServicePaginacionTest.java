package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PaginaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper.OfertaLaboralMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CalificacionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CategoriasTrabajosRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.DistritoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoCategoriaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ExperienciaRequeridaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.HabilidadesRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaHabilidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PostulacionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.TipoDuracionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;

@ExtendWith(MockitoExtension.class)
class OfertaLaboralServicePaginacionTest {

    @Mock private OfertaLaboralRepository ofertaLaboralRepository;
    @Mock private OfertaLaboralMapper ofertaLaboralMapper;
    @Mock private UsuarioEmpleadorRepository usuarioEmpleadorRepository;
    @Mock private UsuarioEmpleadoRepository usuarioEmpleadoRepository;
    @Mock private CategoriasTrabajosRepository categoriasTrabajosRepository;
    @Mock private ModalidadRepository modalidadRepository;
    @Mock private DistritoRepository distritoRepository;
    @Mock private ExperienciaRequeridaRepository experienciaRequeridaRepository;
    @Mock private TipoDuracionRepository tipoDuracionRepository;
    @Mock private HabilidadesRepository habilidadesRepository;
    @Mock private OfertaHabilidadRepository ofertaHabilidadRepository;
    @Mock private PostulacionRepository postulacionRepository;
    @Mock private CalificacionRepository calificacionRepository;
    @Mock private S3StorageService s3StorageService;
    @Mock private EmpleadoCategoriaRepository empleadoCategoriaRepository;
    @Mock private SuscripcionService suscripcionService;

    @InjectMocks
    private OfertaLaboralService ofertaLaboralService;

    @Test
    @SuppressWarnings("unchecked")
    void devuelvePaginaConMetadatosYConservaElOrdenSolicitado() {
        OfertaLaboral ofertaReciente = crearOferta(3L);
        OfertaLaboral ofertaAnterior = crearOferta(2L);
        Pageable pageableEsperado = PageRequest.of(1, 2);

        when(ofertaLaboralRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(ofertaReciente, ofertaAnterior),
                pageableEsperado,
                5
        ));

        when(ofertaLaboralRepository.findAllByIdWithHabilidades(List.of(3L, 2L)))
                .thenReturn(List.of(ofertaAnterior, ofertaReciente));
        when(ofertaLaboralMapper.convertToDTO(ofertaReciente)).thenReturn(crearDto(3L));
        when(ofertaLaboralMapper.convertToDTO(ofertaAnterior)).thenReturn(crearDto(2L));

        PaginaResponseDTO<OfertaLaboralResponseDTO> resultado =
                ofertaLaboralService.getOfertasLaboralesActivasPaginadas(1, 2);

        assertEquals(List.of(3L, 2L), resultado.getContent().stream()
                .map(OfertaLaboralResponseDTO::getId)
                .toList());
        assertEquals(1, resultado.getPage());
        assertEquals(2, resultado.getSize());
        assertEquals(5, resultado.getTotalElements());
        assertEquals(3, resultado.getTotalPages());
        assertEquals(false, resultado.isFirst());
        assertEquals(false, resultado.isLast());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(ofertaLaboralRepository).findAll(any(Specification.class), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(2, captor.getValue().getPageSize());
    }

    private OfertaLaboral crearOferta(Long id) {
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setId(id);
        return oferta;
    }

    private OfertaLaboralResponseDTO crearDto(Long id) {
        OfertaLaboralResponseDTO dto = new OfertaLaboralResponseDTO();
        dto.setId(id);
        return dto;
    }
}
