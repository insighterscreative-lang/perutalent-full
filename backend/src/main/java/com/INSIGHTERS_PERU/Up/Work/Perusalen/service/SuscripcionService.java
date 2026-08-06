package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PlanSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsoPlanUsuarioDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PlanSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.SuscripcionUsuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsoPlanUsuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PlanSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.SuscripcionUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsoPlanUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioEmpleadorRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SuscripcionService {

    private static final String PLAN_GRATUITO = "GRATUITO";
    private static final String PLAN_PREMIUM = "PREMIUM";

    private static final String ESTADO_ACTIVA = "ACTIVA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final String ESTADO_VENCIDA = "VENCIDA";
    private static final String ESTADO_PAGO_FALLIDO = "PAGO_FALLIDO";

    private final PlanSuscripcionRepository planSuscripcionRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsoPlanUsuarioRepository usoPlanUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEmpleadorRepository usuarioEmpleadorRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;

    public SuscripcionService(
            PlanSuscripcionRepository planSuscripcionRepository,
            SuscripcionUsuarioRepository suscripcionUsuarioRepository,
            UsoPlanUsuarioRepository usoPlanUsuarioRepository,
            UsuarioRepository usuarioRepository,
            UsuarioEmpleadorRepository usuarioEmpleadorRepository,
            OfertaLaboralRepository ofertaLaboralRepository
    ) {
        this.planSuscripcionRepository = planSuscripcionRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usoPlanUsuarioRepository = usoPlanUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioEmpleadorRepository = usuarioEmpleadorRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanSuscripcionDTO> listarPlanesActivos() {
        return planSuscripcionRepository.findByActivoTrueOrderByPrecioCentimosAsc()
                .stream()
                .map(this::convertirPlanDTO)
                .toList();
    }

    @Transactional
    public MiSuscripcionDTO obtenerMiSuscripcion(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);

        return convertirMiSuscripcionDTO(suscripcion);
    }

    @Transactional
    public MiSuscripcionDTO cambiarPlan(Long idUsuario, Long idPlan) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        PlanSuscripcion planNuevo = planSuscripcionRepository.findByIdAndActivoTrue(idPlan)
                .orElseThrow(() -> new RuntimeException("El plan seleccionado no existe o no está activo."));

        if (PLAN_PREMIUM.equalsIgnoreCase(planNuevo.getNombrePlan())) {
            throw new RuntimeException("Para activar Premium debes completar la suscripción con Culqi.");
        }

        SuscripcionUsuario suscripcionActual = obtenerSuscripcionActualEntidad(idUsuario);
        if (esPremium(suscripcionActual.getPlan()) && suscripcionActual.getCulqiSubscriptionId() != null) {
            throw new RuntimeException("Para volver al plan gratuito primero debes cancelar la suscripción Premium.");
        }

        return actualizarSuscripcionUsuario(usuario, planNuevo);
    }

    @Transactional
    public MiSuscripcionDTO activarPlanPorPago(Long idUsuario, Long idPlan) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        PlanSuscripcion planNuevo = planSuscripcionRepository.findByIdAndActivoTrue(idPlan)
                .orElseThrow(() -> new RuntimeException("El plan pagado no existe o no está activo."));

        if (!PLAN_PREMIUM.equalsIgnoreCase(planNuevo.getNombrePlan())) {
            throw new RuntimeException("Solo se puede activar por pago el plan PREMIUM.");
        }

        return actualizarSuscripcionUsuario(usuario, planNuevo);
    }

    @Transactional
    public MiSuscripcionDTO activarPlanPorSuscripcionCulqi(
            Long idUsuario,
            Long idPlan,
            String culqiCustomerId,
            String culqiCardId,
            String culqiSubscriptionId,
            LocalDate fechaProximoCobro
    ) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        PlanSuscripcion planNuevo = planSuscripcionRepository.findByIdAndActivoTrue(idPlan)
                .orElseThrow(() -> new RuntimeException("El plan pagado no existe o no está activo."));

        if (!PLAN_PREMIUM.equalsIgnoreCase(planNuevo.getNombrePlan())) {
            throw new RuntimeException("Solo se puede activar por suscripción el plan PREMIUM.");
        }

        SuscripcionUsuario suscripcion = suscripcionUsuarioRepository
                .findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
                        usuario.getId(),
                        estadosVigentesParaBeneficios()
                )
                .orElseGet(() -> crearNuevaSuscripcion(usuario));

        LocalDate hoy = LocalDate.now();
        LocalDate siguienteCobro = fechaProximoCobro != null
                ? fechaProximoCobro
                : hoy.plusDays(obtenerDuracionPlan(planNuevo));

        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(planNuevo);
        suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        suscripcion.setFechaInicio(hoy);
        suscripcion.setFechaFin(siguienteCobro);
        suscripcion.setFechaProximoCobro(siguienteCobro);
        suscripcion.setFechaUltimoCobro(LocalDateTime.now());
        suscripcion.setFechaCancelacion(null);
        suscripcion.setMotivoCancelacion(null);
        suscripcion.setCulqiCustomerId(culqiCustomerId);
        suscripcion.setCulqiCardId(culqiCardId);
        suscripcion.setCulqiSubscriptionId(culqiSubscriptionId);
        suscripcion.setRenovacionAutomatica(true);
        suscripcion.setFechaActualizacion(LocalDateTime.now());

        if (suscripcion.getFechaCreacion() == null) {
            suscripcion.setFechaCreacion(LocalDateTime.now());
        }

        SuscripcionUsuario suscripcionGuardada = suscripcionUsuarioRepository.save(suscripcion);

        obtenerOCrearUsoMensual(usuario);

        return convertirMiSuscripcionDTO(suscripcionGuardada);
    }

    @Transactional
    public SuscripcionUsuario obtenerSuscripcionActualEntidad(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        return obtenerOCrearSuscripcionGratuita(usuario);
    }

    @Transactional
    public MiSuscripcionDTO renovarSuscripcionPorCobroExitoso(
            SuscripcionUsuario suscripcion,
            LocalDate fechaProximoCobro
    ) {
        if (suscripcion == null) {
            throw new RuntimeException("No se encontró la suscripción local a renovar.");
        }

        PlanSuscripcion plan = suscripcion.getPlan();
        LocalDate hoy = LocalDate.now();
        LocalDate siguienteCobro = fechaProximoCobro != null
                ? fechaProximoCobro
                : hoy.plusDays(obtenerDuracionPlan(plan));

        suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        suscripcion.setFechaFin(siguienteCobro);
        suscripcion.setFechaProximoCobro(siguienteCobro);
        suscripcion.setFechaUltimoCobro(LocalDateTime.now());
        suscripcion.setRenovacionAutomatica(true);
        suscripcion.setFechaActualizacion(LocalDateTime.now());
        suscripcion.setFechaCancelacion(null);
        suscripcion.setMotivoCancelacion(null);

        SuscripcionUsuario guardada = suscripcionUsuarioRepository.save(suscripcion);
        return convertirMiSuscripcionDTO(guardada);
    }

    @Transactional
    public MiSuscripcionDTO marcarPagoFallido(SuscripcionUsuario suscripcion, String motivo) {
        if (suscripcion == null) {
            throw new RuntimeException("No se encontró la suscripción local con pago fallido.");
        }

        // Culqi puede volver a intentar un cobro fallido. Mientras el periodo ya pagado
        // siga vigente, el usuario conserva Premium; al vencer, la normalización habitual
        // cambiará la suscripción a VENCIDA y dejará de otorgar beneficios.
        LocalDate hoy = FechaPeru.hoy();
        boolean periodoVencido = suscripcion.getFechaFin() != null
                && suscripcion.getFechaFin().isBefore(hoy);

        if (periodoVencido) {
            suscripcion.setEstadoSuscripcion(ESTADO_PAGO_FALLIDO);
        } else {
            suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        }

        suscripcion.setFechaActualizacion(LocalDateTime.now());
        suscripcion.setMotivoCancelacion(motivo);

        SuscripcionUsuario guardada = suscripcionUsuarioRepository.save(suscripcion);
        return convertirMiSuscripcionDTO(guardada);
    }

    @Transactional
    public MiSuscripcionDTO cancelarSuscripcionLocal(Long idUsuario, String motivo) {
        SuscripcionUsuario suscripcionActual = obtenerSuscripcionActualEntidad(idUsuario);
        return cancelarRenovacionConAccesoHastaFinDelPeriodo(suscripcionActual, motivo);
    }

    @Transactional
    public MiSuscripcionDTO cancelarSuscripcionLocalPorCulqiId(String culqiSubscriptionId, String motivo) {
        SuscripcionUsuario suscripcion = suscripcionUsuarioRepository
                .findFirstByCulqiSubscriptionIdOrderByFechaCreacionDesc(culqiSubscriptionId)
                .orElseThrow(() -> new RuntimeException("No se encontró la suscripción local de Culqi."));

        return cancelarRenovacionConAccesoHastaFinDelPeriodo(suscripcion, motivo);
    }

    /**
     * Culqi cancela la recurrencia de forma inmediata, pero el usuario ya pagó
     * el periodo vigente. Por eso se desactiva la renovación automática y se
     * conserva el plan Premium como ACTIVO hasta la fecha_fin registrada.
     *
     * Si el periodo ya venció (o no tiene una fecha fin válida), se cierra la
     * suscripción y se devuelve/crea el plan gratuito inmediatamente.
     */
    private MiSuscripcionDTO cancelarRenovacionConAccesoHastaFinDelPeriodo(
            SuscripcionUsuario suscripcion,
            String motivo
    ) {
        LocalDate hoy = FechaPeru.hoy();
        LocalDateTime ahora = FechaPeru.ahora();
        LocalDate fechaFin = suscripcion.getFechaFin();

        suscripcion.setRenovacionAutomatica(false);
        suscripcion.setFechaProximoCobro(null);

        if (suscripcion.getFechaCancelacion() == null) {
            suscripcion.setFechaCancelacion(ahora);
        }

        suscripcion.setMotivoCancelacion(motivo);
        suscripcion.setFechaActualizacion(ahora);

        boolean periodoPagadoVigente = esPremium(suscripcion.getPlan())
                && fechaFin != null
                && !fechaFin.isBefore(hoy);

        if (periodoPagadoVigente) {
            suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
            SuscripcionUsuario guardada = suscripcionUsuarioRepository.save(suscripcion);
            return convertirMiSuscripcionDTO(guardada);
        }

        suscripcion.setEstadoSuscripcion(ESTADO_CANCELADA);

        // Primero se cierra y se sincroniza la suscripción anterior; solo después se
        // crea el plan gratuito para no violar uq_suscripcion_usuario_activa.
        suscripcionUsuarioRepository.saveAndFlush(suscripcion);

        SuscripcionUsuario gratuita = obtenerOCrearSuscripcionGratuita(suscripcion.getUsuario());
        return convertirMiSuscripcionDTO(gratuita);
    }

    @Transactional
    public UsoPlanUsuarioDTO obtenerMiUso(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);
        UsoPlanUsuario uso = obtenerOCrearUsoMensual(usuario);
        Integer ofertasActivasActuales = obtenerOfertasActivasActuales(usuario);

        return convertirUsoPlanUsuarioDTO(uso, suscripcion.getPlan(), ofertasActivasActuales);
    }

    @Transactional
    public void validarPuedePostular(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);
        UsoPlanUsuario uso = obtenerOCrearUsoMensual(usuario);

        Integer limitePostulaciones = suscripcion.getPlan().getMaxPostulacionesMes();

        if (limitePostulaciones == null) {
            return;
        }

        Integer postulacionesUsadas = uso.getPostulacionesUsadas();

        if (postulacionesUsadas == null) {
            postulacionesUsadas = 0;
        }

        if (postulacionesUsadas >= limitePostulaciones) {
            throw new RuntimeException(
                    "Has alcanzado el límite mensual de postulaciones de tu plan "
                            + suscripcion.getPlan().getNombrePlan()
                            + "."
            );
        }
    }

    @Transactional
    public void registrarPostulacionUsada(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        UsoPlanUsuario uso = obtenerOCrearUsoMensual(usuario);

        Integer postulacionesUsadas = uso.getPostulacionesUsadas();

        if (postulacionesUsadas == null) {
            postulacionesUsadas = 0;
        }

        uso.setPostulacionesUsadas(postulacionesUsadas + 1);
        uso.setFechaActualizacion(LocalDateTime.now());

        usoPlanUsuarioRepository.save(uso);
    }

    @Transactional
    public void validarPuedeCrearOferta(Long idUsuario, Integer ofertasActivasActuales) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);

        Integer limiteOfertasActivas = suscripcion.getPlan().getMaxOfertasActivas();

        if (limiteOfertasActivas == null) {
            return;
        }

        if (ofertasActivasActuales == null) {
            ofertasActivasActuales = 0;
        }

        if (ofertasActivasActuales >= limiteOfertasActivas) {
            throw new RuntimeException(
                    "Has alcanzado el límite de ofertas activas de tu plan "
                            + suscripcion.getPlan().getNombrePlan()
                            + ". Finaliza una oferta o mejora tu plan."
            );
        }
    }

    @Transactional
    public Integer obtenerLimiteRecomendaciones(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);

        return suscripcion.getPlan().getMaxRecomendaciones();
    }

    @Transactional(readOnly = true)
    public Map<Long, PlanUsuarioResumen> obtenerResumenPlanesUsuarios(List<Long> idsUsuarios) {
        Map<Long, PlanUsuarioResumen> resultado = new HashMap<>();

        if (idsUsuarios == null || idsUsuarios.isEmpty()) {
            return resultado;
        }

        LocalDate hoy = FechaPeru.hoy();

        suscripcionUsuarioRepository.findActivasByUsuarioIds(idsUsuarios)
                .stream()
                .filter(suscripcion -> suscripcion.getFechaInicio() == null
                        || !suscripcion.getFechaInicio().isAfter(hoy))
                .filter(suscripcion -> suscripcion.getFechaFin() == null
                        || !suscripcion.getFechaFin().isBefore(hoy))
                .forEach(suscripcion -> resultado.putIfAbsent(
                        suscripcion.getUsuario().getId(),
                        new PlanUsuarioResumen(
                                Boolean.TRUE.equals(suscripcion.getPlan().getPrioridadPostulante()),
                                suscripcion.getPlan().getNombrePlan()
                        )
                ));

        idsUsuarios.forEach(idUsuario -> resultado.putIfAbsent(
                idUsuario,
                new PlanUsuarioResumen(false, PLAN_GRATUITO)
        ));

        return resultado;
    }

    @Transactional
    public boolean usuarioTienePrioridadPostulante(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);

        return Boolean.TRUE.equals(suscripcion.getPlan().getPrioridadPostulante());
    }

    @Transactional
    public String obtenerNombrePlanUsuario(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);

        return suscripcion.getPlan().getNombrePlan();
    }

    @Transactional
    public SuscripcionUsuario obtenerOCrearSuscripcionGratuita(Usuario usuario) {
        // Ruta rápida: si existe una suscripción activa que todavía otorga beneficios,
        // no es necesario bloquear al usuario ni escribir en la base de datos.
        SuscripcionUsuario suscripcionActual = buscarSuscripcionActiva(usuario.getId()).orElse(null);

        if (suscripcionActual != null && !requiereTransicionAutomatica(suscripcionActual)) {
            return suscripcionActual;
        }

        /*
         * La transición de PREMIUM vencida a GRATUITO debe ser atómica. El bloqueo
         * pesimista del usuario evita que dos peticiones simultáneas intenten crear
         * dos planes gratuitos y choquen con uq_suscripcion_usuario_activa.
         */
        Usuario usuarioBloqueado = usuarioRepository.findByIdForUpdate(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Volvemos a consultar después de adquirir el bloqueo, porque otra petición
        // pudo haber completado la transición mientras esperábamos.
        SuscripcionUsuario suscripcionTrasBloqueo = buscarSuscripcionActiva(usuario.getId()).orElse(null);

        if (suscripcionTrasBloqueo != null) {
            SuscripcionUsuario normalizada = normalizarSuscripcionVigente(suscripcionTrasBloqueo);

            if (ESTADO_ACTIVA.equalsIgnoreCase(normalizada.getEstadoSuscripcion())) {
                return normalizada;
            }
        }

        return crearSuscripcionGratuita(usuarioBloqueado);
    }

    private Optional<SuscripcionUsuario> buscarSuscripcionActiva(Long idUsuario) {
        return suscripcionUsuarioRepository
                .findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
                        idUsuario,
                        estadosVigentesParaBeneficios()
                );
    }

    private boolean requiereTransicionAutomatica(SuscripcionUsuario suscripcion) {
        if (suscripcion == null
                || !ESTADO_ACTIVA.equalsIgnoreCase(suscripcion.getEstadoSuscripcion())
                || !esPremium(suscripcion.getPlan())) {
            return false;
        }

        LocalDate fechaFin = suscripcion.getFechaFin();

        // Una suscripción Premium sin fecha fin no debe otorgar beneficios de forma
        // indefinida por un dato incompleto.
        return fechaFin == null || fechaFin.isBefore(FechaPeru.hoy());
    }

    @Transactional
    public UsoPlanUsuario obtenerOCrearUsoMensual(Usuario usuario) {
        String periodoActual = obtenerPeriodoActual();

        return usoPlanUsuarioRepository.findByUsuario_IdAndPeriodo(usuario.getId(), periodoActual)
                .orElseGet(() -> {
                    UsoPlanUsuario nuevoUso = new UsoPlanUsuario();
                    nuevoUso.setUsuario(usuario);
                    nuevoUso.setPeriodo(periodoActual);
                    nuevoUso.setPostulacionesUsadas(0);
                    nuevoUso.setOfertasPublicadas(0);
                    nuevoUso.setRecomendacionesVistas(0);
                    nuevoUso.setFechaActualizacion(LocalDateTime.now());

                    return usoPlanUsuarioRepository.save(nuevoUso);
                });
    }

    public boolean esPremiumActivo(SuscripcionUsuario suscripcion) {
        return suscripcion != null
                && suscripcion.getPlan() != null
                && esPremium(suscripcion.getPlan())
                && ESTADO_ACTIVA.equalsIgnoreCase(suscripcion.getEstadoSuscripcion());
    }

    private SuscripcionUsuario normalizarSuscripcionVigente(SuscripcionUsuario suscripcion) {
        if (suscripcion == null || !requiereTransicionAutomatica(suscripcion)) {
            return suscripcion;
        }

        suscripcion.setEstadoSuscripcion(ESTADO_VENCIDA);
        suscripcion.setRenovacionAutomatica(false);
        suscripcion.setFechaProximoCobro(null);
        suscripcion.setFechaActualizacion(FechaPeru.ahora());

        /*
         * Es indispensable hacer flush antes de insertar el plan GRATUITO. Así
         * PostgreSQL libera primero el índice único parcial de la suscripción ACTIVA
         * y luego acepta la nueva fila activa.
         */
        return suscripcionUsuarioRepository.saveAndFlush(suscripcion);
    }

    private Integer obtenerOfertasActivasActuales(Usuario usuario) {
        return usuarioEmpleadorRepository.findByUsuarioId(usuario.getId())
                .map(empleador -> ofertaLaboralRepository
                        .countByIdEmpleadorIdAndEstadoOfertaAndFechaTerminoPostulacionGreaterThanEqual(
                                empleador.getId(),
                                "ABIERTA",
                                FechaPeru.hoy()
                        ))
                .orElse(0);
    }

    private MiSuscripcionDTO actualizarSuscripcionUsuario(
            Usuario usuario,
            PlanSuscripcion planNuevo
    ) {
        SuscripcionUsuario suscripcion = suscripcionUsuarioRepository
                .findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
                        usuario.getId(),
                        estadosVigentesParaBeneficios()
                )
                .orElseGet(() -> crearNuevaSuscripcion(usuario));

        suscripcion.setPlan(planNuevo);
        suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setFechaFin(calcularFechaFin(planNuevo));
        suscripcion.setFechaProximoCobro(null);
        suscripcion.setFechaUltimoCobro(null);
        suscripcion.setFechaCancelacion(null);
        suscripcion.setMotivoCancelacion(null);
        suscripcion.setRenovacionAutomatica(false);
        suscripcion.setFechaActualizacion(LocalDateTime.now());

        suscripcion.setCulqiCustomerId(null);
        suscripcion.setCulqiCardId(null);
        suscripcion.setCulqiSubscriptionId(null);

        SuscripcionUsuario suscripcionGuardada = suscripcionUsuarioRepository.save(suscripcion);

        obtenerOCrearUsoMensual(usuario);

        return convertirMiSuscripcionDTO(suscripcionGuardada);
    }

    private Usuario buscarUsuarioPorId(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    private SuscripcionUsuario crearNuevaSuscripcion(Usuario usuario) {
        SuscripcionUsuario suscripcion = new SuscripcionUsuario();
        suscripcion.setUsuario(usuario);
        suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setFechaCreacion(LocalDateTime.now());
        suscripcion.setFechaActualizacion(LocalDateTime.now());
        suscripcion.setRenovacionAutomatica(false);

        return suscripcion;
    }

    private SuscripcionUsuario crearSuscripcionGratuita(Usuario usuario) {
        PlanSuscripcion planGratuito = planSuscripcionRepository.findByNombrePlan(PLAN_GRATUITO)
                .orElseThrow(() -> new RuntimeException("No existe el plan GRATUITO en la base de datos."));

        SuscripcionUsuario nuevaSuscripcion = new SuscripcionUsuario();
        nuevaSuscripcion.setUsuario(usuario);
        nuevaSuscripcion.setPlan(planGratuito);
        nuevaSuscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        nuevaSuscripcion.setFechaInicio(LocalDate.now());
        nuevaSuscripcion.setFechaFin(null);
        nuevaSuscripcion.setFechaProximoCobro(null);
        nuevaSuscripcion.setFechaUltimoCobro(null);
        nuevaSuscripcion.setFechaCancelacion(null);
        nuevaSuscripcion.setMotivoCancelacion(null);
        nuevaSuscripcion.setCulqiCustomerId(null);
        nuevaSuscripcion.setCulqiCardId(null);
        nuevaSuscripcion.setCulqiSubscriptionId(null);
        nuevaSuscripcion.setRenovacionAutomatica(false);
        nuevaSuscripcion.setFechaCreacion(LocalDateTime.now());
        nuevaSuscripcion.setFechaActualizacion(LocalDateTime.now());

        return suscripcionUsuarioRepository.save(nuevaSuscripcion);
    }

    private LocalDate calcularFechaFin(PlanSuscripcion plan) {
        if (plan.getNombrePlan().equalsIgnoreCase(PLAN_PREMIUM)) {
            return LocalDate.now().plusDays(obtenerDuracionPlan(plan));
        }

        return null;
    }

    private int obtenerDuracionPlan(PlanSuscripcion plan) {
        if (plan.getDuracionDias() == null || plan.getDuracionDias() <= 0) {
            return 30;
        }

        return plan.getDuracionDias();
    }

    private boolean esPremium(PlanSuscripcion plan) {
        return plan != null && plan.getNombrePlan().equalsIgnoreCase(PLAN_PREMIUM);
    }

    private List<String> estadosVigentesParaBeneficios() {
        return List.of(ESTADO_ACTIVA);
    }

    private String obtenerPeriodoActual() {
        return YearMonth.now().toString();
    }

    private PlanSuscripcionDTO convertirPlanDTO(PlanSuscripcion plan) {
        return new PlanSuscripcionDTO(
                plan.getId(),
                plan.getNombrePlan(),
                plan.getDescripcion(),
                plan.getPrecioCentimos(),
                plan.getMoneda(),
                plan.getDuracionDias(),
                plan.getMaxPostulacionesMes(),
                plan.getMaxRecomendaciones(),
                plan.getMaxOfertasActivas(),
                plan.getPrioridadPostulante(),
                plan.getOfertasDestacadas(),
                plan.getActivo()
        );
    }

    private MiSuscripcionDTO convertirMiSuscripcionDTO(SuscripcionUsuario suscripcion) {
        PlanSuscripcion plan = suscripcion.getPlan();

        return new MiSuscripcionDTO(
                suscripcion.getId(),
                plan.getId(),
                plan.getNombrePlan(),
                suscripcion.getEstadoSuscripcion(),
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin(),
                esPremium(plan),
                plan.getMaxPostulacionesMes(),
                plan.getMaxRecomendaciones(),
                plan.getMaxOfertasActivas(),
                plan.getPrioridadPostulante(),
                plan.getOfertasDestacadas(),
                Boolean.TRUE.equals(suscripcion.getRenovacionAutomatica()),
                suscripcion.getFechaProximoCobro(),
                suscripcion.getFechaUltimoCobro(),
                suscripcion.getFechaCancelacion()
        );
    }

    private UsoPlanUsuarioDTO convertirUsoPlanUsuarioDTO(
            UsoPlanUsuario uso,
            PlanSuscripcion plan,
            Integer ofertasActivasActuales
    ) {
        Integer postulacionesRestantes = calcularRestantes(
                plan.getMaxPostulacionesMes(),
                uso.getPostulacionesUsadas()
        );

        Integer ofertasActivas = ofertasActivasActuales != null ? ofertasActivasActuales : 0;

        Integer ofertasRestantes = calcularRestantes(
                plan.getMaxOfertasActivas(),
                ofertasActivas
        );

        // Las recomendaciones no se consumen. Este valor representa cuántas
        // ofertas como máximo se muestran en cada consulta de “Para ti”.
        Integer recomendacionesRestantes = plan.getMaxRecomendaciones();

        return new UsoPlanUsuarioDTO(
                uso.getId(),
                uso.getPeriodo(),
                uso.getPostulacionesUsadas(),
                ofertasActivas,
                uso.getRecomendacionesVistas(),
                plan.getMaxPostulacionesMes(),
                plan.getMaxOfertasActivas(),
                plan.getMaxRecomendaciones(),
                postulacionesRestantes,
                ofertasRestantes,
                recomendacionesRestantes
        );
    }

    private Integer calcularRestantes(Integer limite, Integer usado) {
        if (limite == null) {
            return null;
        }

        if (usado == null) {
            usado = 0;
        }

        return Math.max(limite - usado, 0);
    }
    public record PlanUsuarioResumen(boolean prioridadPostulante, String nombrePlan) {
    }

}
