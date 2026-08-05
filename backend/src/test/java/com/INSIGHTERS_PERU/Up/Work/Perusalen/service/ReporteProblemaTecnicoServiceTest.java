package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteProblemaTecnicoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteProblemaTecnicoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteProblemaTecnico;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ReporteProblemaTecnicoRepository;

@ExtendWith(MockitoExtension.class)
class ReporteProblemaTecnicoServiceTest {

    @Mock
    private ReporteProblemaTecnicoRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReporteProblemaTecnicoService service;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(
                service,
                "correoSoporte",
                "insighters.creative@gmail.com"
        );
    }

    @Test
    void registraProblemaYEnviaCorreos() {
        when(repository.existsByCodigoReporte(any())).thenReturn(false);
        when(repository.save(any(ReporteProblemaTecnico.class))).thenAnswer(invocation -> {
            ReporteProblemaTecnico reporte = invocation.getArgument(0);
            reporte.setId(1L);
            return reporte;
        });

        ReporteProblemaTecnicoResponseDTO response = service.registrar(crearRequest());

        assertNotNull(response.getCodigoReporte());
        assertEquals("PENDIENTE", response.getEstado());
        assertNotNull(response.getFechaCreacion());

        verify(repository).save(any(ReporteProblemaTecnico.class));
        verify(emailService).enviarProblemaTecnicoAlEquipo(
                eq("insighters.creative@gmail.com"),
                any(ReporteProblemaTecnico.class)
        );
        verify(emailService).enviarConfirmacionProblemaTecnico(
                eq("persona@correo.com"),
                any(ReporteProblemaTecnico.class)
        );
    }

    private ReporteProblemaTecnicoRequestDTO crearRequest() {
        ReporteProblemaTecnicoRequestDTO request = new ReporteProblemaTecnicoRequestDTO();
        request.setNombreCompleto("Persona de Prueba");
        request.setEmail("persona@correo.com");
        request.setTipoProblema("POSTULACIONES");
        request.setPantalla("Mis postulaciones");
        request.setDescripcion("La pantalla no mostró la postulación que acababa de realizar.");
        request.setPasosReproducir("Inicié sesión, postulé y abrí Mis postulaciones.");
        request.setInformacionAdicional("La prueba se realizó desde Chrome.");
        request.setAceptaDeclaracion(true);
        return request;
    }
}
