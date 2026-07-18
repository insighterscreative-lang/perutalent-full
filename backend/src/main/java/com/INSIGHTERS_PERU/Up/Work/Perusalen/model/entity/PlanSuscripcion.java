package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_suscripcion")
public class PlanSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long id;

    @Column(name = "nombre_plan", nullable = false, unique = true, length = 50)
    private String nombrePlan;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio_centimos", nullable = false)
    private Integer precioCentimos;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(name = "max_postulaciones_mes")
    private Integer maxPostulacionesMes;

    @Column(name = "max_recomendaciones")
    private Integer maxRecomendaciones;

    @Column(name = "max_ofertas_activas")
    private Integer maxOfertasActivas;

    @Column(name = "prioridad_postulante", nullable = false)
    private Boolean prioridadPostulante;

    @Column(name = "ofertas_destacadas", nullable = false)
    private Boolean ofertasDestacadas;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "culqi_plan_id", length = 150)
    private String culqiPlanId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}