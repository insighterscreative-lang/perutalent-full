package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.OfertaLaboralResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper.OfertaLaboralMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CategoriasTrabajos;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoCategoria;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;
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
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;

@ExtendWith(MockitoExtension.class)
class OfertaLaboralServiceRecomendacionesTest {

    @Mock
    private OfertaLaboralRepository ofertaLaboralRepository;

    @Mock
    private OfertaLaboralMapper ofertaLaboralMapper;

    @Mock
    private UsuarioEmpleadorRepository usuarioEmpleadorRepository;

    @Mock
    private UsuarioEmpleadoRepository usuarioEmpleadoRepository;

    @Mock
    private CategoriasTrabajosRepository categoriasTrabajosRepository;

    @Mock
    private ModalidadRepository modalidadRepository;

    @Mock
    private DistritoRepository distritoRepository;

    @Mock
    private ExperienciaRequeridaRepository experienciaRequeridaRepository;

    @Mock
    private TipoDuracionRepository tipoDuracionRepository;

    @Mock
    private HabilidadesRepository habilidadesRepository;

    @Mock
    private OfertaHabilidadRepository ofertaHabilidadRepository;

    @Mock
    private PostulacionRepository postulacionRepository;

    @Mock
    private CalificacionRepository calificacionRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private EmpleadoCategoriaRepository empleadoCategoriaRepository;

    @Mock
    private SuscripcionService suscripcionService;

    @InjectMocks
    private OfertaLaboralService ofertaLaboralService;

    @Test
    void devuelveSoloLasCompatiblesMasRecientesHastaElLimiteDelPlan() {
        UsuarioEmpleado empleado = new UsuarioEmpleado();
        empleado.setId(7L);

        CategoriasTrabajos categoriaCompatible = crearCategoria(10L);
        CategoriasTrabajos categoriaDistinta = crearCategoria(99L);

        EmpleadoCategoria relacion = new EmpleadoCategoria();
        relacion.setIdEmpleado(empleado);
        relacion.setIdCategoria(categoriaCompatible);

        OfertaLaboral ofertaAntigua = crearOferta(
                1L,
                categoriaCompatible,
                FechaPeru.hoy().minusDays(3)
        );
        OfertaLaboral ofertaIntermedia = crearOferta(
                2L,
                categoriaCompatible,
                FechaPeru.hoy().minusDays(1)
        );
        OfertaLaboral ofertaReciente = crearOferta(
                3L,
                categoriaCompatible,
                FechaPeru.hoy()
        );
        OfertaLaboral ofertaNoCompatible = crearOferta(
                4L,
                categoriaDistinta,
                FechaPeru.hoy().plusDays(1)
        );

        when(usuarioEmpleadoRepository.findByUsuarioId(70L))
                .thenReturn(Optional.of(empleado));
        when(empleadoCategoriaRepository.findByIdEmpleadoId(7L))
                .thenReturn(List.of(relacion));
        when(suscripcionService.obtenerLimiteRecomendaciones(70L))
                .thenReturn(2);
        when(ofertaLaboralRepository.findActivasWithHabilidades(
                "ABIERTA",
                FechaPeru.hoy()
        )).thenReturn(List.of(
                ofertaAntigua,
                ofertaNoCompatible,
                ofertaReciente,
                ofertaIntermedia
        ));

        when(ofertaLaboralMapper.convertToDTO(ofertaReciente))
                .thenReturn(crearDto(3L));
        when(ofertaLaboralMapper.convertToDTO(ofertaIntermedia))
                .thenReturn(crearDto(2L));

        List<OfertaLaboralResponseDTO> resultado =
                ofertaLaboralService.getOfertasParaTi(70L);

        assertEquals(List.of(3L, 2L), resultado.stream()
                .map(OfertaLaboralResponseDTO::getId)
                .toList());
    }

    private CategoriasTrabajos crearCategoria(Long id) {
        CategoriasTrabajos categoria = new CategoriasTrabajos();
        categoria.setId(id);
        return categoria;
    }

    private OfertaLaboral crearOferta(
            Long id,
            CategoriasTrabajos categoria,
            java.time.LocalDate fechaPublicacion
    ) {
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setId(id);
        oferta.setIdCategoria(categoria);
        oferta.setFechaPublicacion(fechaPublicacion);
        return oferta;
    }

    private OfertaLaboralResponseDTO crearDto(Long id) {
        OfertaLaboralResponseDTO dto = new OfertaLaboralResponseDTO();
        dto.setId(id);
        return dto;
    }
}
