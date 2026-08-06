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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PagoSuscripcionService {

    private static final Logger log = LoggerFactory.getLogger(PagoSuscripcionService.class);

    private static final String PLAN_PREMIUM = "PREMIUM";

    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_RECHAZADO = "RECHAZADO";
    private static final String ESTADO_PROCESADO = "PROCESADO";

    private static final String ESTADO_SUSCRIPCION_CANCELADA = "CANCELADA";
    private static final String ESTADO_SUSCRIPCION_VENCIDA = "VENCIDA";

    private static final String TIPO_PAGO_CULQI_SUSCRIPCION_INICIAL = "CULQI_SUSCRIPCION_INICIAL";
    private static final String TIPO_PAGO_CULQI_RENOVACION = "CULQI_SUSCRIPCION_RENOVACION";

    @Value("${culqi.premium-plan-id:}")
    private String culqiPremiumPlanIdEnv;

    @Value("${culqi.enabled:false}")
    private boolean culqiEnabled;

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
        validarCulqiHabilitado();

        if (request == null || request.getIdPlan() == null) {
            throw new RuntimeException("Debes seleccionar un plan.");
        }

        if (request.getTokenId() == null || request.getTokenId().isBlank()) {
            throw new RuntimeException("No se recibió el token de pago de Culqi.");
        }

        Usuario usuario = usuarioRepository.findByIdForUpdate(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        PlanSuscripcion plan = planSuscripcionRepository.findByIdAndActivoTrue(request.getIdPlan())
                .orElseThrow(() -> new RuntimeException("El plan seleccionado no existe o no está activo."));

        validarPlanPremium(plan);
        validarNoExisteSuscripcionCulqiVigente(usuario.getId());

        String culqiPlanId = obtenerCulqiPlanId(plan);
        culqiService.validarAmbientePlanYToken(culqiPlanId, request.getTokenId());

        String culqiSubscriptionId = null;

        try {
            Map<String, Object> respuestaCliente = culqiService.crearCliente(usuario.getId(), usuario.getEmail());
            String culqiCustomerId = culqiService.obtenerCustomerId(respuestaCliente);

            Map<String, Object> respuestaTarjeta = culqiService.crearTarjeta(
                    culqiCustomerId,
                    request.getTokenId()
            );
            String culqiCardId = culqiService.obtenerCardId(respuestaTarjeta);

            Map<String, Object> respuestaSuscripcion = culqiService.crearSuscripcion(
                    culqiCardId,
                    culqiPlanId,
                    usuario.getId(),
                    plan.getId()
            );

            culqiSubscriptionId = culqiService.obtenerSubscriptionId(respuestaSuscripcion);

            if (!culqiService.suscripcionActivaYCorrespondeAlPlan(
                    respuestaSuscripcion,
                    culqiPlanId
            )) {
                throw new RuntimeException(
                        "Culqi no confirmó una suscripción activa asociada al plan Premium configurado."
                );
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
                    .orElseThrow(() -> new RuntimeException(
                            "La suscripción fue creada, pero no pudo registrarse localmente."
                    ));

            String chargeId = culqiService.obtenerChargeId(respuestaSuscripcion);
            String estadoPago = chargeId == null || chargeId.isBlank()
                    ? ESTADO_PROCESADO
                    : ESTADO_APROBADO;

            PagoSuscripcion pago = crearPago(
                    usuario,
                    plan,
                    entidadSuscripcion,
                    chargeId,
                    culqiSubscriptionId,
                    estadoPago,
                    TIPO_PAGO_CULQI_SUSCRIPCION_INICIAL,
                    respuestaSuscripcion
            );

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
        } catch (RuntimeException ex) {
            compensarSuscripcionExternaSiFueCreada(culqiSubscriptionId);
            throw ex;
        }
    }

    @Transactional
    public MiSuscripcionDTO cancelarSuscripcionPremium(Long idUsuario) {
        validarCulqiHabilitado();

        Usuario usuario = usuarioRepository.findByIdForUpdate(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        SuscripcionUsuario suscripcion = suscripcionService.obtenerSuscripcionActualEntidad(usuario.getId());

        if (!suscripcionService.esPremiumActivo(suscripcion)) {
            throw new RuntimeException("No tienes una suscripción Premium activa para cancelar.");
        }

        if (suscripcion.getCulqiSubscriptionId() == null
                || suscripcion.getCulqiSubscriptionId().isBlank()) {
            throw new RuntimeException("La suscripción Premium no tiene un identificador válido de Culqi.");
        }

        culqiService.cancelarSuscripcion(suscripcion.getCulqiSubscriptionId());

        return suscripcionService.cancelarSuscripcionLocal(
                idUsuario,
                "Cancelada por el usuario desde la plataforma."
        );
    }

    @Transactional
    public Map<String, Object> procesarWebhookCulqi(String payload) {
        validarCulqiHabilitado();

        Map<String, Object> eventoMap = culqiService.convertirJsonAMapEstricto(payload);
        String eventId = obtenerEventId(eventoMap, payload);
        String tipoEvento = obtenerTipoEvento(eventoMap);

        if (tipoEvento == null || tipoEvento.isBlank()) {
            throw new IllegalArgumentException("El webhook de Culqi no contiene un tipo de evento.");
        }

        if (culqiEventoRepository.existsByCulqiEventId(eventId)) {
            return Map.of(
                    "procesado", false,
                    "duplicado", true,
                    "mensaje", "Evento Culqi ya procesado."
            );
        }

        CulqiEvento eventoGuardado = guardarEventoInicial(eventId, tipoEvento, eventoMap);

        String culqiSubscriptionId = culqiService.obtenerSubscriptionId(eventoMap);
        String chargeId = culqiService.obtenerChargeId(eventoMap);
        String planIdEvento = culqiService.obtenerPlanId(eventoMap);

        PlanSuscripcion planPremium = planSuscripcionRepository.findByNombrePlan(PLAN_PREMIUM)
                .orElseThrow(() -> new RuntimeException("No existe el plan PREMIUM en la base de datos."));
        String planIdEsperado = obtenerCulqiPlanId(planPremium);

        SuscripcionUsuario suscripcion = buscarSuscripcionLocal(culqiSubscriptionId);

        boolean procesado = false;
        String mensaje = "Evento recibido y registrado.";

        if (esEventoCreacionExitosa(tipoEvento)) {
            if (planIdEvento != null && !planIdEsperado.equals(planIdEvento)) {
                mensaje = "La suscripción notificada pertenece a un plan distinto del Premium configurado.";
            } else {
                suscripcion = recuperarSuscripcionDesdeEventoSiCorresponde(
                        suscripcion,
                        eventoMap,
                        planPremium,
                        planIdEsperado,
                        culqiSubscriptionId
                );

                if (suscripcion != null) {
                    eventoGuardado.setSuscripcion(suscripcion);
                    procesado = true;
                    mensaje = "Creación de suscripción confirmada por Culqi.";
                } else {
                    mensaje = "Culqi confirmó la suscripción, pero no se pudo vincular con un usuario local.";
                }
            }
        } else if (esEventoCreacionFallida(tipoEvento)) {
            procesado = true;
            mensaje = "Culqi notificó que la creación de la suscripción falló.";
        } else if (esEventoCobroExitoso(tipoEvento)) {
            if (culqiSubscriptionId == null || culqiSubscriptionId.isBlank()) {
                mensaje = "El cobro exitoso no está vinculado a una suscripción de Culqi.";
            } else if (planIdEvento != null && !planIdEsperado.equals(planIdEvento)) {
                mensaje = "El cobro pertenece a un plan distinto del Premium configurado.";
            } else if (suscripcion != null) {
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

                eventoGuardado.setSuscripcion(suscripcion);
                eventoGuardado.setPago(pago);
                procesado = true;
                mensaje = "Cobro recurrente exitoso procesado.";
            } else {
                mensaje = "Cobro exitoso recibido, pero no se encontró la suscripción local.";
            }
        } else if (esEventoCobroFallido(tipoEvento)) {
            if (suscripcion != null) {
                suscripcionService.marcarPagoFallido(
                        suscripcion,
                        "Culqi notificó un intento de cobro fallido."
                );

                PagoSuscripcion pago = registrarPagoWebhookSiCorresponde(
                        suscripcion,
                        chargeId,
                        eventoMap,
                        TIPO_PAGO_CULQI_RENOVACION,
                        ESTADO_RECHAZADO
                );

                eventoGuardado.setSuscripcion(suscripcion);
                eventoGuardado.setPago(pago);
                procesado = true;
                mensaje = "Intento de cobro fallido registrado.";
            } else {
                mensaje = "Cobro fallido recibido, pero no se encontró la suscripción local.";
            }
        } else if (esEventoCancelacionExitosaOFinalizada(tipoEvento, eventoMap)) {
            if (culqiSubscriptionId != null && !culqiSubscriptionId.isBlank()) {
                suscripcionService.cancelarSuscripcionLocalPorCulqiId(
                        culqiSubscriptionId,
                        "Culqi notificó la cancelación o finalización de la suscripción."
                );

                eventoGuardado.setSuscripcion(suscripcion);
                procesado = true;
                mensaje = "Cancelación o finalización de suscripción procesada.";
            } else {
                mensaje = "Evento de cancelación recibido sin ID de suscripción.";
            }
        } else if (esEventoCancelacionFallida(tipoEvento)) {
            procesado = true;
            mensaje = "Culqi notificó que el intento de cancelación falló.";
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

    private CulqiEvento guardarEventoInicial(
            String eventId,
            String tipoEvento,
            Map<String, Object> eventoMap
    ) {
        CulqiEvento evento = new CulqiEvento();
        evento.setCulqiEventId(eventId);
        evento.setTipoEvento(tipoEvento);
        evento.setProcesado(false);
        evento.setFechaRecepcion(LocalDateTime.now());
        evento.setPayload(culqiService.convertirRespuestaAJson(eventoMap));
        return culqiEventoRepository.save(evento);
    }

    private SuscripcionUsuario recuperarSuscripcionDesdeEventoSiCorresponde(
            SuscripcionUsuario suscripcion,
            Map<String, Object> eventoMap,
            PlanSuscripcion planPremium,
            String planIdEsperado,
            String culqiSubscriptionId
    ) {
        if (suscripcion != null) {
            return suscripcion;
        }

        if (culqiSubscriptionId == null || culqiSubscriptionId.isBlank()) {
            return null;
        }

        if (!culqiService.suscripcionActivaYCorrespondeAlPlan(eventoMap, planIdEsperado)) {
            return null;
        }

        Long idUsuario = culqiService.obtenerMetadataLong(eventoMap, "id_usuario");
        Long idPlanLocal = culqiService.obtenerMetadataLong(eventoMap, "id_plan_local");

        if (idUsuario == null
                || idPlanLocal == null
                || !planPremium.getId().equals(idPlanLocal)
                || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }

        String customerId = culqiService.obtenerCustomerId(eventoMap);
        String cardId = culqiService.obtenerCardId(eventoMap);
        LocalDate fechaProximoCobro = culqiService.obtenerFechaLocalDesdeTimestamp(
                eventoMap,
                "next_billing_date"
        );

        suscripcionService.activarPlanPorSuscripcionCulqi(
                idUsuario,
                planPremium.getId(),
                customerId,
                cardId,
                culqiSubscriptionId,
                fechaProximoCobro
        );

        return buscarSuscripcionLocal(culqiSubscriptionId);
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

        PagoSuscripcion pago = crearPago(
                suscripcion.getUsuario(),
                suscripcion.getPlan(),
                suscripcion,
                chargeId,
                suscripcion.getCulqiSubscriptionId(),
                estadoPago,
                tipoPago,
                respuesta
        );

        return pagoSuscripcionRepository.save(pago);
    }

    private PagoSuscripcion crearPago(
            Usuario usuario,
            PlanSuscripcion plan,
            SuscripcionUsuario suscripcion,
            String chargeId,
            String subscriptionId,
            String estado,
            String tipo,
            Map<String, Object> respuesta
    ) {
        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setUsuario(usuario);
        pago.setPlan(plan);
        pago.setSuscripcion(suscripcion);
        pago.setMontoCentimos(plan.getPrecioCentimos());
        pago.setMoneda(plan.getMoneda());
        pago.setEstadoPago(estado);
        pago.setTipoPago(tipo);
        pago.setCulqiChargeId(chargeId);
        pago.setCulqiSubscriptionId(subscriptionId);
        pago.setFechaPago(LocalDateTime.now());
        pago.setRespuestaCulqi(culqiService.convertirRespuestaAJson(respuesta));
        return pago;
    }

    private void validarCulqiHabilitado() {
        if (!culqiEnabled) {
            throw new RuntimeException("Los pagos con Culqi todavía no están habilitados.");
        }
    }

    private void validarPlanPremium(PlanSuscripcion plan) {
        if (!PLAN_PREMIUM.equalsIgnoreCase(plan.getNombrePlan())) {
            throw new RuntimeException("Este endpoint solo procesa suscripciones del plan PREMIUM.");
        }

        if (plan.getPrecioCentimos() == null || plan.getPrecioCentimos() <= 0) {
            throw new RuntimeException("El plan PREMIUM no tiene un precio válido.");
        }
    }

    private void validarNoExisteSuscripcionCulqiVigente(Long idUsuario) {
        boolean existe = suscripcionUsuarioRepository.findByUsuario_IdOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .anyMatch(suscripcion -> suscripcion.getCulqiSubscriptionId() != null
                        && !suscripcion.getCulqiSubscriptionId().isBlank()
                        && !ESTADO_SUSCRIPCION_CANCELADA.equalsIgnoreCase(suscripcion.getEstadoSuscripcion())
                        && !ESTADO_SUSCRIPCION_VENCIDA.equalsIgnoreCase(suscripcion.getEstadoSuscripcion()));

        if (existe) {
            throw new RuntimeException(
                    "Ya existe una suscripción de Culqi pendiente, activa o con cobro en revisión para este usuario."
            );
        }
    }

    private void compensarSuscripcionExternaSiFueCreada(String culqiSubscriptionId) {
        if (culqiSubscriptionId == null || culqiSubscriptionId.isBlank()) {
            return;
        }

        try {
            culqiService.cancelarSuscripcion(culqiSubscriptionId);
            log.warn(
                    "Se canceló en Culqi la suscripción {} porque falló el registro local.",
                    enmascararId(culqiSubscriptionId)
            );
        } catch (RuntimeException cancelError) {
            log.error(
                    "ATENCIÓN: no se pudo compensar la suscripción externa {}. Revisar CulqiPanel.",
                    enmascararId(culqiSubscriptionId)
            );
        }
    }

    private SuscripcionUsuario buscarSuscripcionLocal(String culqiSubscriptionId) {
        if (culqiSubscriptionId == null || culqiSubscriptionId.isBlank()) {
            return null;
        }

        return suscripcionUsuarioRepository
                .findFirstByCulqiSubscriptionIdOrderByFechaCreacionDesc(culqiSubscriptionId)
                .orElse(null);
    }

    private String obtenerCulqiPlanId(PlanSuscripcion plan) {
        String planId = null;

        if (plan.getCulqiPlanId() != null && !plan.getCulqiPlanId().isBlank()) {
            planId = plan.getCulqiPlanId().trim();
        } else if (culqiPremiumPlanIdEnv != null && !culqiPremiumPlanIdEnv.isBlank()) {
            planId = culqiPremiumPlanIdEnv.trim();
        }

        if (planId == null) {
            throw new RuntimeException(
                    "No se configuró el ID del plan recurrente de Culqi. "
                            + "Completa plan_suscripcion.culqi_plan_id para PREMIUM o configura CULQI_PREMIUM_PLAN_ID."
            );
        }

        return planId;
    }

    private String obtenerEventId(Map<String, Object> eventoMap, String payload) {
        String id = culqiService.obtenerTextoRaiz(eventoMap, "id");
        if (id != null && !id.isBlank()) {
            return id;
        }

        String eventId = culqiService.obtenerTextoRaiz(eventoMap, "event_id");
        if (eventId != null && !eventId.isBlank()) {
            return eventId;
        }

        return "generated-" + sha256(payload == null ? "" : payload);
    }

    private String obtenerTipoEvento(Map<String, Object> eventoMap) {
        String tipo = culqiService.obtenerTextoRaiz(eventoMap, "type");
        if (tipo != null && !tipo.isBlank()) {
            return tipo;
        }

        return culqiService.obtenerTextoRaiz(eventoMap, "event_type");
    }

    private boolean esEventoCreacionExitosa(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.equals("subscription.creation.succeeded")
                || tipo.equals("subscription.created")
                || tipo.equals("subscription.create.succeeded")
                || tipo.equals("subscription_creation_succeeded");
    }

    private boolean esEventoCreacionFallida(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.equals("subscription.creation.failed")
                || tipo.equals("subscription.create.failed")
                || tipo.equals("subscription_creation_failed");
    }

    private boolean esEventoCobroExitoso(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.equals("subscription.charge.succeeded")
                || tipo.equals("subscription_charge_succeeded")
                || tipo.equals("charge.succeeded")
                || tipo.equals("charge.creation.succeeded");
    }

    private boolean esEventoCobroFallido(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.equals("subscription.charge.failed")
                || tipo.equals("subscription_charge_failed")
                || tipo.equals("charge.failed")
                || tipo.equals("charge.creation.failed");
    }

    private boolean esEventoCancelacionExitosaOFinalizada(
            String tipoEvento,
            Map<String, Object> eventoMap
    ) {
        String tipo = normalizar(tipoEvento);

        if (tipo.equals("subscription.cancel.succeeded")
                || tipo.equals("subscription.cancelled")
                || tipo.equals("subscription.canceled")
                || tipo.equals("subscription.deleted")
                || tipo.equals("subscription.finished")
                || tipo.equals("subscription.finalized")) {
            return true;
        }

        if (tipo.equals("subscription.updated")) {
            Integer status = culqiService.obtenerStatusSuscripcion(eventoMap);
            return status != null && (status == 4 || status == 6);
        }

        return false;
    }

    private boolean esEventoCancelacionFallida(String tipoEvento) {
        String tipo = normalizar(tipoEvento);
        return tipo.equals("subscription.cancel.failed")
                || tipo.equals("subscription_cancel_failed");
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.toLowerCase(Locale.ROOT).trim();
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

    private String enmascararId(String id) {
        if (id == null || id.length() < 10) {
            return "***";
        }
        return id.substring(0, 8) + "***" + id.substring(id.length() - 4);
    }
}
