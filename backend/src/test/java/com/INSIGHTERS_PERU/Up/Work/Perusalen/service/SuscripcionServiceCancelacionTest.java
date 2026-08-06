package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PlanSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.SuscripcionUsuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PlanSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.SuscripcionUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsoPlanUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceCancelacionTest {

    @Mock private PlanSuscripcionRepository planSuscripcionRepository;
    @Mock private SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    @Mock private UsoPlanUsuarioRepository usoPlanUsuarioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioEmpleadorRepository usuarioEmpleadorRepository;
    @Mock private OfertaLaboralRepository ofertaLaboralRepository;

    private SuscripcionService suscripcionService;

    @BeforeEach
    void preparar() {
        suscripcionService = new SuscripcionService(
                planSuscripcionRepository,
                suscripcionUsuarioRepository,
                usoPlanUsuarioRepository,
                usuarioRepository,
                usuarioEmpleadorRepository,
                ofertaLaboralRepository
        );

        when(suscripcionUsuarioRepository.save(any(SuscripcionUsuario.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void cancelarRenovacionMantienePremiumHastaFinDelPeriodoPagado() {
        Usuario usuario = crearUsuario(10L);
        LocalDate fechaFinPagada = FechaPeru.hoy().plusDays(20);
        SuscripcionUsuario premium = crearPremiumActivo(usuario, fechaFinPagada);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(suscripcionUsuarioRepository
                .findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
                        any(Long.class),
                        anyList()
                ))
                .thenReturn(Optional.of(premium));

        MiSuscripcionDTO resultado = suscripcionService.cancelarSuscripcionLocal(
                usuario.getId(),
                "Cancelada por el usuario desde la plataforma."
        );

        assertEquals("PREMIUM", resultado.getNombrePlan());
        assertEquals("ACTIVA", resultado.getEstadoSuscripcion());
        assertFalse(resultado.getRenovacionAutomatica());
        assertEquals(fechaFinPagada, resultado.getFechaFin());
        assertNotNull(resultado.getFechaCancelacion());

        assertEquals("ACTIVA", premium.getEstadoSuscripcion());
        assertFalse(premium.getRenovacionAutomatica());
        assertEquals(fechaFinPagada, premium.getFechaFin());
        assertNull(premium.getFechaProximoCobro());

        verify(planSuscripcionRepository, never()).findByNombrePlan("GRATUITO");
    }

    @Test
    void webhookDeCancelacionNoQuitaPremiumAntesDeLaFechaFin() {
        Usuario usuario = crearUsuario(20L);
        LocalDate fechaFinPagada = FechaPeru.hoy().plusDays(5);
        SuscripcionUsuario premium = crearPremiumActivo(usuario, fechaFinPagada);

        when(suscripcionUsuarioRepository
                .findFirstByCulqiSubscriptionIdOrderByFechaCreacionDesc(
                        premium.getCulqiSubscriptionId()
                ))
                .thenReturn(Optional.of(premium));

        MiSuscripcionDTO resultado = suscripcionService.cancelarSuscripcionLocalPorCulqiId(
                premium.getCulqiSubscriptionId(),
                "Culqi notificó la cancelación."
        );

        assertEquals("PREMIUM", resultado.getNombrePlan());
        assertEquals("ACTIVA", resultado.getEstadoSuscripcion());
        assertFalse(resultado.getRenovacionAutomatica());
        assertEquals(fechaFinPagada, resultado.getFechaFin());
        verify(planSuscripcionRepository, never()).findByNombrePlan("GRATUITO");
    }

    private Usuario crearUsuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail("usuario" + id + "@example.com");
        return usuario;
    }

    private SuscripcionUsuario crearPremiumActivo(Usuario usuario, LocalDate fechaFin) {
        PlanSuscripcion premium = new PlanSuscripcion();
        premium.setId(2L);
        premium.setNombrePlan("PREMIUM");
        premium.setDescripcion("Plan Premium");
        premium.setPrecioCentimos(1990);
        premium.setMoneda("PEN");
        premium.setDuracionDias(30);
        premium.setPrioridadPostulante(true);
        premium.setOfertasDestacadas(true);
        premium.setActivo(true);

        SuscripcionUsuario suscripcion = new SuscripcionUsuario();
        suscripcion.setId(100L + usuario.getId());
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(premium);
        suscripcion.setEstadoSuscripcion("ACTIVA");
        suscripcion.setFechaInicio(FechaPeru.hoy().minusDays(10));
        suscripcion.setFechaFin(fechaFin);
        suscripcion.setFechaProximoCobro(fechaFin);
        suscripcion.setRenovacionAutomatica(true);
        suscripcion.setCulqiSubscriptionId("sxn_live_ficticia_" + usuario.getId());
        suscripcion.setFechaCreacion(LocalDateTime.now().minusDays(10));
        suscripcion.setFechaActualizacion(LocalDateTime.now());
        return suscripcion;
    }
}
