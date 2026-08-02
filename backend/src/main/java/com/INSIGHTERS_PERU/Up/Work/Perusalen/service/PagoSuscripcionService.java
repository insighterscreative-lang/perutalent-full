package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoPremiumRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PagoSuscripcionResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CulqiEvento;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PagoSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PlanSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.SuscripcionUsuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.CulqiEventoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PagoSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PlanSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.SuscripcionUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PagoSuscripcionService {

    private static final String PLAN_PREMIUM = "PREMIUM";

    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_RECHAZADO = "RECHAZADO";
    private static final String ESTADO_PROCESADO = "PROCESADO";

    private static final String TIPO_PAGO_CULQI_CARGO_UNICO = "CULQI_CARGO_UNICO";
    private static final String TIPO_PAGO_CULQI_SUSCRIPCION_INICIAL = "CULQI_SUSCRIPCION_INICIAL";
    private static final String TIPO_PAGO_CULQI_RENOVACION = "CULQI_SUSCRIPCION_RENOVACION";

    @Value("${CULQI_PREMIUM_PLAN_ID:}")
    private String culqiPremiumPlanIdEnv;

    private final UsuarioRepository usuarioRepository;
    private final PlanSuscripcionRepository planSuscripcionRepository;
    private final PagoSuscripcionRepository pagoSuscripcionRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final CulqiEventoRepository culqiEventoRepository;
    private final SuscripcionService suscripcionService;
    private final CulqiService culqiService;

    public PagoSuscripcionService(
            UsuarioRepository usuarioRepository,
            PlanSuscripcionRepository planSuscripcionRepository,
            PagoSuscripcionRepository pagoSuscripcionRepository,
            SuscripcionUsuarioRepository suscripcionUsuarioRepository,
            CulqiEventoRepository culqiEventoRepository,
            SuscripcionService suscripcionService,
            CulqiService culqiService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.planSuscripcionRepository = planSuscripcionRepository;
        this.pagoSuscripcionRepository = pagoSuscripcionRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.culqiEventoRepository = culqiEventoRepository;
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
            throw new RuntimeException("Este endpoint solo procesa suscripciones del plan PREMIUM.");
        }

        if (plan.getPrecioCentimos() == null || plan.getPrecioCentimos() <= 0) {
            throw new RuntimeException("El plan PREMIUM no tiene un precio válido.");
        }

        SuscripcionUsuario actual = suscripcionService.obtenerSuscripcionActualEntidad(idUsuario);
        if (suscripcionService.esPremiumActivo(actual) && actual.getCulqiSubscriptionId() != null) {
            throw new RuntimeException("Ya tienes una suscripción Premium activa con renovación automática.");
        }

        String culqiPlanId = obtenerCulqiPlanId(plan);

        Map<String, Object> respuestaCliente = culqiService.crearCliente(usuario.getId(), usuario.getEmail());
        String culqiCustomerId = culqiService.obtenerCustomerId(respuestaCliente);

        Map<String, Object> respuestaTarjeta = culqiService.crearTarjeta(culqiCustomerId, request.getTokenId());
        String culqiCardId = culqiService.obtenerCardId(respuestaTarjeta);

        Map<String, Object> respuestaSuscripcion = culqiService.crearSuscripcion(
                culqiCardId,
                culqiPlanId,
                usuario.getId(),
                plan.getId()
        );

        String culqiSubscriptionId = culqiService.obtenerSubscriptionId(respuestaSuscripcion);
        if (culqiSubscriptionId == null || culqiSubscriptionId.isBlank()) {
            throw new RuntimeException("Culqi no devolvió el ID de la suscripción recurrente.");
        }

        LocalDate fechaProximoCobro = culqiService.obtenerFechaLocalDesdeTimestamp(
                respuestaSuscripcion,
                "next_billing_date"
        );

        MiSuscripcionDTO suscripcion = suscripcionService.activarPlanPorSuscripcionCulqi(
                idUsuario,
                plan.getId(),
                culqiCustomerId,
                culqiCardId,
                culqiSubscriptionId,
                fechaProximoCobro
        );

        SuscripcionUsuario entidadSuscripcion = suscripcionUsuarioRepository
                .findFirstByCulqiSubscriptionIdOrderByFechaCreacionDesc(culqiSubscriptionId)
                .orElse(null);

        String chargeId = culqiService.obtenerChargeId(respuestaSuscripcion);

        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setUsuario(usuario);
        pago.setPlan(plan);
        pago.setSuscripcion(entidadSuscripcion);
        pago.setMontoCentimos(plan.getPrecioCentimos());
        pago.setMoneda(plan.getMoneda());
        pago.setEstadoPago(ESTADO_APROBADO);
        pago.setTipoPago(TIPO_PAGO_CULQI_SUSCRIPCION_INICIAL);
        pago.setCulqiChargeId(chargeId);
        pago.setCulqiSubscriptionId(culqiSubscriptionId);
        pago.setFechaPago(LocalDateTime.now());
        pago.setRespuestaCulqi(culqiService.convertirRespuestaAJson(respuestaSuscripcion));

        PagoSuscripcion pagoGuardado = pagoSuscripcionRepository.save(pago);

        return new PagoSuscripcionResponseDTO(
                pagoGuardado.getId(),
                pagoGuardado.getEstadoPago(),
                "Suscripción Premium creada. La renovación mensual automática quedó activa.",
                plan.getId(),
                plan.getNombrePlan(),
                plan.getPrecioCentimos(),
                plan.getMoneda(),
                pagoGuardado.getCulqiChargeId(),
                culqiSubscriptionId,
                true,
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin(),
                suscripcion.getFechaProximoCobro()
        );
    }

    @Transactional
    public MiSuscripcionDTO cancelarSuscripcionPremium(Long idUsuario) {
        SuscripcionUsuario suscripcion = suscripcionService.obtenerSuscripcionActualEntidad(idUsuario);

        if (!suscripcionService.esPremiumActivo(suscripcion)) {
            throw new RuntimeException("No tienes una suscripción Premium activa para cancelar.");
        }

        if (suscripcion.getCulqiSubscriptionId() != null && !suscripcion.getCulqiSubscriptionId().isBlank()) {
            culqiService.cancelarSuscripcion(suscripcion.getCulqiSubscriptionId());
        }

        return suscripcionService.cancelarSuscripcionLocal(
                idUsuario,
                "Cancelada por el usuario desde la plataforma."
        );
    }

    @Transactional
    public Map<String, Object> procesarWebhookCulqi(String payload) {
        Map<String, Object> eventoMap = culqiService.convertirJsonAMap(payload);

        String eventId = obtenerEventId(eventoMap, payload);
        String tipoEvento = obtenerTipoEvento(eventoMap);

        if (culqiEventoRepository.existsByCulqiEventId(eventId)) {
            return Map.of(
                    "procesado", false,
                    "duplicado", true,
                    "mensaje", "Evento Culqi ya procesado."
            );
        }

        CulqiEvento evento = new CulqiEvento();
        evento.setCulqiEventId(eventId);
        evento.setTipoEvento(tipoEvento);
        evento.setProcesado(false);
        evento.setFechaRecepcion(LocalDateTime.now());
        evento.setPayload(payload == null ? "{}" : payload);

        CulqiEvento eventoGuardado = culqiEventoRepository.save(evento);

        String culqiSubscriptionId = culqiService.obtenerSubscriptionId(eventoMap);
        String chargeId = culqiService.obtenerChargeId(eventoMap);

        SuscripcionUsuario suscripcion = null;
        if (culqiSubscriptionId != null && !culqiSubscriptionId.isBlank()) {
            suscripcion = suscripcionUsuarioRepository
                    .findFirstByCulqiSubscriptionIdOrderByFechaCreacionDesc(culqiSubscriptionId)
                    .orElse(null);
        }

        if (suscripcion != null) {
            eventoGuardado.setSuscripcion(suscripcion);
        }

        boolean procesado = false;
        String mensaje = "Evento recibido.";

        if (esEventoCobroExitoso(tipoEvento)) {
            if (suscripcion != null) {
                LocalDate fechaProximoCobro = culqiService.obtenerFechaLocalDesdeTimestamp(
                        eventoMap,
                        "next_billing_date"
                );

                suscripcionService.renovarSuscripcionPorCobroExitoso(suscripcion, fechaProximoCobro);
                PagoSuscripcion pago = registrarPagoWebhookSiCorresponde(
                        suscripcion,
                        chargeId,
                        eventoMap,
                        TIPO_PAGO_CULQI_RENOVACION,
                        ESTADO_APROBADO
                );

                eventoGuardado.setPago(pago);
                procesado = true;
                mensaje = "Cobro recurrente exitoso procesado.";
            } else {
                mensaje = "Cobro exitoso recibido, pero no se encontró la suscripción local.";
            }
        } else if (esEventoCobroFallido(tipoEvento)) {
            if (suscripcion != null) {
                suscripcionService.marcarPagoFallido(suscripcion, "Culqi notificó un cobro fallido.");
                registrarPagoWebhookSiCorresponde(
                        suscripcion,
                        chargeId,
                        eventoMap,
                        TIPO_PAGO_CULQI_RENOVACION,
                        ESTADO_RECHAZADO
                );

                procesado = true;
                mensaje = "Cobro recurrente fallido procesado.";
            } else {
                mensaje = "Cobro fallido recibido, pero no se encontró la suscripción local.";
            }
        } else if (esEventoCancelacionOSuscripcionFinalizada(tipoEvento, eventoMap)) {
            if (culqiSubscriptionId != null) {
                suscripcionService.cancelarSuscripcionLocalPorCulqiId(
                        culqiSubscriptionId,
                        "Culqi notificó cancelación o finalización de la suscripción."
                );

                procesado = true;
                mensaje = "Cancelación o finalización de suscripción procesada.";
            } else {
                mensaje = "Evento de cancelación recibido sin ID de suscripción.";
            }
        }

        eventoGuardado.setProcesado(procesado);
        culqiEventoRepository.save(eventoGuardado);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("procesado", procesado);
        response.put("tipoEvento", tipoEvento);
        response.put("culqiSubscriptionId", culqiSubscriptionId);
        response.put("culqiChargeId", chargeId);
        response.put("mensaje", mensaje);
        return response;
    }

    private PagoSuscripcion registrarPagoWebhookSiCorresponde(
            SuscripcionUsuario suscripcion,
            String chargeId,
            Map<String, Object> respuesta,
            String tipoPago,
            String estadoPago
    ) {
        if (chargeId != null && !chargeId.isBlank()
                && pagoSuscripcionRepository.existsByCulqiChargeId(chargeId)) {
            return pagoSuscripcionRepository.findByCulqiChargeId(chargeId).orElse(null);
        }

        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setUsuario(suscripcion.getUsuario());
        pago.setPlan(suscripcion.getPlan());
        pago.setSuscripcion(suscripcion);
        pago.setMontoCentimos(suscripcion.getPlan().getPrecioCentimos());
        pago.setMoneda(suscripcion.getPlan().getMoneda());
        pago.setEstadoPago(estadoPago);
        pago.setTipoPago(tipoPago);
        pago.setCulqiChargeId(chargeId);
        pago.setCulqiSubscriptionId(suscripcion.getCulqiSubscriptionId());
        pago.setFechaPago(LocalDateTime.now());
        pago.setRespuestaCulqi(culqiService.convertirRespuestaAJson(respuesta));

        return pagoSuscripcionRepository.save(pago);
    }

    private String obtenerCulqiPlanId(PlanSuscripcion plan) {
        if (plan.getCulqiPlanId() != null && !plan.getCulqiPlanId().isBlank()) {
            return plan.getCulqiPlanId();
        }

        if (culqiPremiumPlanIdEnv != null && !culqiPremiumPlanIdEnv.isBlank()) {
            return culqiPremiumPlanIdEnv;
        }

        throw new RuntimeException(
                "No se configuró el ID del plan recurrente de Culqi. " +
                        "Completa plan_suscripcion.culqi_plan_id para PREMIUM o configura CULQI_PREMIUM_PLAN_ID."
        );
    }

    private String obtenerEventId(Map<String, Object> eventoMap, String payload) {
        String id = culqiService.obtenerId(eventoMap);
        if (id != null && !id.isBlank()) {
            return id;
        }

        String eventId = culqiService.buscarTextoPorClave(eventoMap, "event_id");
        if (eventId != null && !eventId.isBlank()) {
            return eventId;
        }

        return "generated-" + sha256(payload == null ? "" : payload);
    }

    private String obtenerTipoEvento(Map<String, Object> eventoMap) {
        String tipo = culqiService.buscarTextoPorClave(eventoMap, "type");
        if (tipo != null && !tipo.isBlank()) {
            return tipo;
        }

        String eventType = culqiService.buscarTextoPorClave(eventoMap, "event_type");
        if (eventType != null && !eventType.isBlank()) {
            return eventType;
        }

        return "CULQI_EVENTO_DESCONOCIDO";
    }

    private boolean esEventoCobroExitoso(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.contains("subscription.charge.succeeded")
                || tipo.contains("subscription_charge_succeeded")
                || tipo.contains("cargo_exitoso")
                || tipo.contains("charge.succeeded")
                || tipo.contains("charge.creation.succeeded");
    }

    private boolean esEventoCobroFallido(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.contains("subscription.charge.failed")
                || tipo.contains("subscription_charge_failed")
                || tipo.contains("cargo_fallido")
                || tipo.contains("charge.failed")
                || tipo.contains("charge.creation.failed");
    }

    private boolean esEventoCancelacionOSuscripcionFinalizada(
            String tipoEvento,
            Map<String, Object> eventoMap
    ) {
        String tipo = normalizar(tipoEvento);

        if (tipo.contains("subscription.deleted")
                || tipo.contains("subscription.cancelled")
                || tipo.contains("subscription.canceled")
                || tipo.contains("subscription.finished")
                || tipo.contains("subscription.finalized")
                || tipo.contains("subscription.updated")) {

            Integer status = culqiService.obtenerStatusSuscripcion(eventoMap);
            return status != null && (status == 4 || status == 6);
        }

        return tipo.contains("cancelacion")
                || tipo.contains("cancelada")
                || tipo.contains("cancelled")
                || tipo.contains("canceled")
                || tipo.contains("finalizada")
                || tipo.contains("finalized");
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.toLowerCase().trim();
    }

    private String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(valor.hashCode());
        }
    }
}
