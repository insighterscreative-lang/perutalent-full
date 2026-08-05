package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteOfertaRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteOfertaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteOferta;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ReporteOfertaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ReporteOfertaServiceTest {

    @Mock
    private ReporteOfertaRepository reporteOfertaRepository;

    @Mock
    private OfertaLaboralRepository ofertaLaboralRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReporteOfertaService reporteOfertaService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(
                reporteOfertaService,
                "correoSoporte",
                "insighters.creative@gmail.com"
        );
    }

    @Test
    void registraReporteValido() {
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setId(10L);
        oferta.setTitulo("Oferta de prueba");

        Usuario usuario = new Usuario();
        usuario.setId(20L);
        usuario.setEmail("empleado@correo.com");

        when(ofertaLaboralRepository.findById(10L)).thenReturn(Optional.of(oferta));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuario));
        when(reporteOfertaRepository.existsByOfertaIdAndUsuarioReportanteId(10L, 20L))
                .thenReturn(false);
        when(reporteOfertaRepository.saveAndFlush(any(ReporteOferta.class))).thenAnswer(invocation -> {
            ReporteOferta reporte = invocation.getArgument(0);
            reporte.setId(99L);
            return reporte;
        });

        ReporteOfertaResponseDTO response = reporteOfertaService.reportar(
                10L,
                20L,
                crearRequest()
        );

        assertEquals(99L, response.getIdReporte());
        assertEquals("PENDIENTE", response.getEstado());

        verify(emailService).enviarReporteOfertaAlEquipo(
                eq("insighters.creative@gmail.com"),
                any(ReporteOferta.class)
        );
        verify(emailService).enviarConfirmacionReporteOferta(
                eq("empleado@correo.com"),
                any(ReporteOferta.class)
        );
    }

    @Test
    void rechazaReporteDuplicado() {
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setId(10L);

        Usuario usuario = new Usuario();
        usuario.setId(20L);

        when(ofertaLaboralRepository.findById(10L)).thenReturn(Optional.of(oferta));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuario));
        when(reporteOfertaRepository.existsByOfertaIdAndUsuarioReportanteId(10L, 20L))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> reporteOfertaService.reportar(10L, 20L, crearRequest())
        );

        verify(reporteOfertaRepository, never()).saveAndFlush(any());
        verify(emailService, never()).enviarReporteOfertaAlEquipo(any(), any());
    }

    private ReporteOfertaRequestDTO crearRequest() {
        ReporteOfertaRequestDTO request = new ReporteOfertaRequestDTO();
        request.setMotivo("POSIBLE_ESTAFA");
        request.setDescripcion("La oferta solicita un pago antes de postular.");
        return request;
    }
}
