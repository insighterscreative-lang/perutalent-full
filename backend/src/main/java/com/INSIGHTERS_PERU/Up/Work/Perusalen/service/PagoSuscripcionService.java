package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoPremiumRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoSuscripcionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PagoSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PlanSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PagoSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PlanSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PagoSuscripcionService {

    private static final String PLAN_PREMIUM = "PREMIUM";
    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_RECHAZADO = "RECHAZADO";
    private static final String TIPO_PAGO_CULQI_CARGO_UNICO = "CULQI_CARGO_UNICO";

    private final UsuarioRepository usuarioRepository;
    private final PlanSuscripcionRepository planSuscripcionRepository;
    private final PagoSuscripcionRepository pagoSuscripcionRepository;
    private final SuscripcionService suscripcionService;
    private final CulqiService culqiService;

    public PagoSuscripcionService(
            UsuarioRepository usuarioRepository,
            PlanSuscripcionRepository planSuscripcionRepository,
            PagoSuscripcionRepository pagoSuscripcionRepository,
            SuscripcionService suscripcionService,
            CulqiService culqiService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.planSuscripcionRepository = planSuscripcionRepository;
        this.pagoSuscripcionRepository = pagoSuscripcionRepository;
        this.suscripcionService = suscripcionService;
        this.culqiService = culqiService;
    }

    @Transactional
    public PagoSuscripcionResponseDTO pagarPremium(Long idUsuario, PagoPremiumRequestDTO request) {
        if (request == null || request.getIdPlan() == null) {
            throw new RuntimeException("Debes seleccionar un plan.");
        }

        if (request.getTokenId() == null || request.getTokenId().isBlank()) {
            throw new RuntimeException("No se recibió el token de pago de Culqi.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        PlanSuscripcion plan = planSuscripcionRepository.findByIdAndActivoTrue(request.getIdPlan())
                .orElseThrow(() -> new RuntimeException("El plan seleccionado no existe o no está activo."));

        if (!PLAN_PREMIUM.equalsIgnoreCase(plan.getNombrePlan())) {
            throw new RuntimeException("Este endpoint solo procesa pagos del plan PREMIUM.");
        }

        if (plan.getPrecioCentimos() == null || plan.getPrecioCentimos() <= 0) {
            throw new RuntimeException("El plan PREMIUM no tiene un precio válido.");
        }

        Map<String, Object> respuestaCulqi = culqiService.crearCargo(
                plan.getPrecioCentimos(),
                plan.getMoneda(),
                usuario.getEmail(),
                request.getTokenId(),
                "Suscripción Premium PeruTalent"
        );

        boolean aprobado = culqiService.cargoAprobado(respuestaCulqi);

        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setUsuario(usuario);
        pago.setPlan(plan);
        pago.setSuscripcion(null);
        pago.setMontoCentimos(plan.getPrecioCentimos());
        pago.setMoneda(plan.getMoneda());
        pago.setEstadoPago(aprobado ? ESTADO_APROBADO : ESTADO_RECHAZADO);
        pago.setTipoPago(TIPO_PAGO_CULQI_CARGO_UNICO);
        pago.setCulqiChargeId(culqiService.obtenerChargeId(respuestaCulqi));
        pago.setCulqiSubscriptionId(null);
        pago.setFechaPago(LocalDateTime.now());
        pago.setRespuestaCulqi(culqiService.convertirRespuestaAJson(respuestaCulqi));

        PagoSuscripcion pagoGuardado = pagoSuscripcionRepository.save(pago);

        if (!aprobado) {
            throw new RuntimeException("El pago no fue aprobado por Culqi.");
        }

        MiSuscripcionDTO suscripcion = suscripcionService.activarPlanPorPago(
                idUsuario,
                plan.getId()
        );

        return new PagoSuscripcionResponseDTO(
                pagoGuardado.getId(),
                pagoGuardado.getEstadoPago(),
                "Pago aprobado. Tu plan Premium fue activado correctamente.",
                plan.getId(),
                plan.getNombrePlan(),
                plan.getPrecioCentimos(),
                plan.getMoneda(),
                pagoGuardado.getCulqiChargeId(),
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin()
        );
    }
}