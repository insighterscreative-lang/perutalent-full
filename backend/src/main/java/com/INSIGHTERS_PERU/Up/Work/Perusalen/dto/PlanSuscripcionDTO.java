package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanSuscripcionDTO {

    private Long id;

    private String nombrePlan;

    private String descripcion;

    private Integer precioCentimos;

    private String moneda;

    private Integer duracionDias;

    private Integer maxPostulacionesMes;

    private Integer maxRecomendaciones;

    private Integer maxOfertasActivas;

    private Boolean prioridadPostulante;

    private Boolean ofertasDestacadas;

    private Boolean activo;
}