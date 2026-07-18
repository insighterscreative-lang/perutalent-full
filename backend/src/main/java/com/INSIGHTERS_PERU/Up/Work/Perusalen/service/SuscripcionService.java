package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.MiSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.PlanSuscripcionDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsoPlanUsuarioDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PlanSuscripcion;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.SuscripcionUsuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsoPlanUsuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.PlanSuscripcionRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.SuscripcionUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsoPlanUsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@Service
public class SuscripcionService {

    private final PlanSuscripcionRepository planSuscripcionRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsoPlanUsuarioRepository usoPlanUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public SuscripcionService(
            PlanSuscripcionRepository planSuscripcionRepository,
            SuscripcionUsuarioRepository suscripcionUsuarioRepository,
            UsoPlanUsuarioRepository usoPlanUsuarioRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.planSuscripcionRepository = planSuscripcionRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usoPlanUsuarioRepository = usoPlanUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private static final String PLAN_GRATUITO = "GRATUITO";
    private static final String PLAN_PREMIUM = "PREMIUM";

    private static final String ESTADO_ACTIVA = "ACTIVA";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";

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

        /*
         Descomentas esto si quieres usar el culqui real y lo comentas si es que lo quieres probar sin el culqui
         if (PLAN_PREMIUM.equalsIgnoreCase(planNuevo.getNombrePlan())) {
         throw new RuntimeException("Para activar Premium debes completar el pago con Culqi.");
         }
         */

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
    public UsoPlanUsuarioDTO obtenerMiUso(Long idUsuario) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        SuscripcionUsuario suscripcion = obtenerOCrearSuscripcionGratuita(usuario);
        UsoPlanUsuario uso = obtenerOCrearUsoMensual(usuario);

        return convertirUsoPlanUsuarioDTO(uso, suscripcion.getPlan());
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
        return suscripcionUsuarioRepository
                .findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
                        usuario.getId(),
                        estadosActivos()
                )
                .orElseGet(() -> {
                    PlanSuscripcion planGratuito = planSuscripcionRepository.findByNombrePlan(PLAN_GRATUITO)
                            .orElseThrow(() -> new RuntimeException("No existe el plan GRATUITO en la base de datos."));

                    SuscripcionUsuario nuevaSuscripcion = new SuscripcionUsuario();
                    nuevaSuscripcion.setUsuario(usuario);
                    nuevaSuscripcion.setPlan(planGratuito);
                    nuevaSuscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
                    nuevaSuscripcion.setFechaInicio(LocalDate.now());
                    nuevaSuscripcion.setFechaFin(null);
                    nuevaSuscripcion.setCulqiCustomerId(null);
                    nuevaSuscripcion.setCulqiCardId(null);
                    nuevaSuscripcion.setCulqiSubscriptionId(null);
                    nuevaSuscripcion.setFechaCreacion(LocalDateTime.now());
                    nuevaSuscripcion.setFechaActualizacion(LocalDateTime.now());

                    return suscripcionUsuarioRepository.save(nuevaSuscripcion);
                });
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

    private MiSuscripcionDTO actualizarSuscripcionUsuario(
            Usuario usuario,
            PlanSuscripcion planNuevo
    ) {
        SuscripcionUsuario suscripcion = suscripcionUsuarioRepository
                .findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
                        usuario.getId(),
                        estadosActivos()
                )
                .orElseGet(() -> crearNuevaSuscripcion(usuario));

        suscripcion.setPlan(planNuevo);
        suscripcion.setEstadoSuscripcion(ESTADO_ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setFechaFin(calcularFechaFin(planNuevo));
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

        return suscripcion;
    }

    private LocalDate calcularFechaFin(PlanSuscripcion plan) {
        if (plan.getNombrePlan().equalsIgnoreCase(PLAN_PREMIUM)) {
            return LocalDate.now().plusDays(plan.getDuracionDias());
        }

        return null;
    }

    private boolean esPremium(PlanSuscripcion plan) {
        return plan.getNombrePlan().equalsIgnoreCase(PLAN_PREMIUM);
    }

    private List<String> estadosActivos() {
        return Arrays.asList(ESTADO_ACTIVA, ESTADO_PENDIENTE);
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
                plan.getOfertasDestacadas()
        );
    }

    private UsoPlanUsuarioDTO convertirUsoPlanUsuarioDTO(UsoPlanUsuario uso, PlanSuscripcion plan) {
        Integer postulacionesRestantes = calcularRestantes(
                plan.getMaxPostulacionesMes(),
                uso.getPostulacionesUsadas()
        );

        Integer ofertasRestantes = calcularRestantes(
                plan.getMaxOfertasActivas(),
                uso.getOfertasPublicadas()
        );

        Integer recomendacionesRestantes = calcularRestantes(
                plan.getMaxRecomendaciones(),
                uso.getRecomendacionesVistas()
        );

        return new UsoPlanUsuarioDTO(
                uso.getId(),
                uso.getPeriodo(),
                uso.getPostulacionesUsadas(),
                uso.getOfertasPublicadas(),
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
}