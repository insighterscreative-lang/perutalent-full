package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiSuscripcionDTO {

    private Long idSuscripcion;

    private Long idPlan;

    private String nombrePlan;

    private String estadoSuscripcion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Boolean esPremium;

    private Integer maxPostulacionesMes;

    private Integer maxRecomendaciones;

    private Integer maxOfertasActivas;

    private Boolean prioridadPostulante;

    private Boolean ofertasDestacadas;
}