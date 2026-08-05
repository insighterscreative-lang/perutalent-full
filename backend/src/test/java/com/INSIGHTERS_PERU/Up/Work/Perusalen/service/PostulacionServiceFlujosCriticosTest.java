package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Postulacion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoHabilidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoHerramientaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.EmpleadoModalidadRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PostulacionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;

@ExtendWith(MockitoExtension.class)
class PostulacionServiceFlujosCriticosTest {

    @Mock
    private PostulacionRepository postulacionRepository;

    @Mock
    private OfertaLaboralRepository ofertaLaboralRepository;

    @Mock
    private UsuarioEmpleadoRepository usuarioEmpleadoRepository;

    @Mock
    private UsuarioEmpleadorRepository usuarioEmpleadorRepository;

    @Mock
    private EmpleadoModalidadRepository empleadoModalidadRepository;

    @Mock
    private EmpleadoHabilidadRepository empleadoHabilidadRepository;

    @Mock
    private EmpleadoHerramientaRepository empleadoHerramientaRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private EmailService emailService;

    @Mock
    private SuscripcionService suscripcionService;

    @InjectMocks
    private PostulacionService postulacionService;

    @Test
    void rechazaPostulacionCuandoLaFechaLimiteYaVencio() {
        UsuarioEmpleado empleado = crearEmpleado(7L, 70L);
        OfertaLaboral oferta = crearOferta(15L, FechaPeru.hoy().minusDays(1));

        when(usuarioEmpleadoRepository.findByUsuarioId(70L))
                .thenReturn(Optional.of(empleado));
        when(ofertaLaboralRepository.findById(15L))
                .thenReturn(Optional.of(oferta));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> postulacionService.postular(15L, 70L, true, null)
        );

        assertEquals(
                "El periodo de postulación de esta oferta ha finalizado",
                exception.getMessage()
        );

        verify(postulacionRepository, never())
                .existsByIdOfertaIdAndIdEmpleadoId(any(), any());
        verify(postulacionRepository, never()).save(any(Postulacion.class));
        verify(suscripcionService, never()).validarPuedePostular(any());
    }

    @Test
    void permitePostularDuranteTodoElDiaDeLaFechaLimite() {
        UsuarioEmpleado empleado = crearEmpleado(7L, 70L);
        empleado.setCurriculum("cvs/perfiles/70/cv.pdf");

        UsuarioEmpleador empleador = new UsuarioEmpleador();
        empleador.setId(9L);
        empleador.setNombreComercial("Empresa de prueba");

        OfertaLaboral oferta = crearOferta(15L, FechaPeru.hoy());
        oferta.setTitulo("Oferta vigente hoy");
        oferta.setIdEmpleador(empleador);

        when(usuarioEmpleadoRepository.findByUsuarioId(70L))
                .thenReturn(Optional.of(empleado));
        when(ofertaLaboralRepository.findById(15L))
                .thenReturn(Optional.of(oferta));
        when(postulacionRepository.existsByIdOfertaIdAndIdEmpleadoId(15L, 7L))
                .thenReturn(false);
        when(s3StorageService.copiarCvPerfilAPostulacion(
                "cvs/perfiles/70/cv.pdf",
                70L,
                15L
        )).thenReturn("cvs/postulaciones/70/15/cv.pdf");
        when(postulacionRepository.save(any(Postulacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        postulacionService.postular(15L, 70L, true, null);

        verify(suscripcionService).validarPuedePostular(70L);
        verify(postulacionRepository).save(any(Postulacion.class));
        verify(suscripcionService).registrarPostulacionUsada(70L);
        verify(emailService).enviarCorreoPostulacionEnviada(
                eq("empleado@correo.com"),
                eq("Empleado Prueba"),
                eq("Oferta vigente hoy"),
                eq("Empresa de prueba")
        );
    }

    @Test
    void empleadorNoPuedeConsultarPostulantesDeUnaOfertaAjena() {
        UsuarioEmpleador empleadorAutenticado = new UsuarioEmpleador();
        empleadorAutenticado.setId(10L);

        UsuarioEmpleador propietarioReal = new UsuarioEmpleador();
        propietarioReal.setId(11L);

        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setId(20L);
        oferta.setIdEmpleador(propietarioReal);

        when(usuarioEmpleadorRepository.findByUsuarioId(100L))
                .thenReturn(Optional.of(empleadorAutenticado));
        when(ofertaLaboralRepository.findById(20L))
                .thenReturn(Optional.of(oferta));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> postulacionService.listarPostulantesPorOfertaPaginados(
                        20L,
                        100L,
                        "TODOS",
                        "",
                        null,
                        null,
                        null,
                        null,
                        0,
                        8
                )
        );

        assertEquals(
                "No tienes permiso para ver las postulaciones de esta oferta",
                exception.getMessage()
        );

        verify(postulacionRepository, never()).findIdsPostulantesPaginados(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    private UsuarioEmpleado crearEmpleado(Long idEmpleado, Long idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setEmail("empleado@correo.com");

        UsuarioEmpleado empleado = new UsuarioEmpleado();
        empleado.setId(idEmpleado);
        empleado.setUsuario(usuario);
        empleado.setNombre("Empleado");
        empleado.setApellido("Prueba");
        return empleado;
    }

    private OfertaLaboral crearOferta(Long idOferta, java.time.LocalDate fechaLimite) {
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setId(idOferta);
        oferta.setEstadoOferta("ABIERTA");
        oferta.setFechaTerminoPostulacion(fechaLimite);
        return oferta;
    }
}
