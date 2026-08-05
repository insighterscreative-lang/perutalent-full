package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReclamoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReclamoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Reclamo;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ReclamoRepository;

@ExtendWith(MockitoExtension.class)
class ReclamoServiceTest {

    @Mock
    private ReclamoRepository reclamoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReclamoService reclamoService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(
                reclamoService,
                "correoSoporte",
                "insighters.creative@gmail.com"
        );
    }

    @Test
    void registraReclamoYEnviaCorreos() {
        when(reclamoRepository.existsByCodigoReclamo(any())).thenReturn(false);
        when(reclamoRepository.save(any(Reclamo.class))).thenAnswer(invocation -> {
            Reclamo reclamo = invocation.getArgument(0);
            reclamo.setId(1L);
            return reclamo;
        });

        ReclamoResponseDTO response = reclamoService.registrar(crearRequest());

        assertNotNull(response.getCodigoReclamo());
        assertEquals("PENDIENTE", response.getEstado());
        assertNotNull(response.getFechaCreacion());

        verify(reclamoRepository).save(any(Reclamo.class));
        verify(emailService).enviarReclamoAlEquipo(
                eq("insighters.creative@gmail.com"),
                any(Reclamo.class)
        );
        verify(emailService).enviarConfirmacionReclamo(
                eq("persona@correo.com"),
                any(Reclamo.class)
        );
    }

    private ReclamoRequestDTO crearRequest() {
        ReclamoRequestDTO request = new ReclamoRequestDTO();
        request.setNombreCompleto("Persona de Prueba");
        request.setEmail("persona@correo.com");
        request.setTelefono("999999999");
        request.setTipoDocumento("DNI");
        request.setNumeroDocumento("12345678");
        request.setServicioRelacionado("CUENTA");
        request.setMontoReclamado(new BigDecimal("10.00"));
        request.setTipoSolicitud("RECLAMO");
        request.setAsunto("Problema de prueba");
        request.setDetalle("Detalle suficientemente largo para la prueba.");
        request.setPedido("Solicito revisar el caso.");
        request.setAceptaDeclaracion(true);
        return request;
    }
}
