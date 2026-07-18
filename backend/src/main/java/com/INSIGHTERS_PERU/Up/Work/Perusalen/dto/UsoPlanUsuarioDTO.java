package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsoPlanUsuarioDTO {

    private Long id;

    private String periodo;

    private Integer postulacionesUsadas;

    private Integer ofertasPublicadas;

    private Integer recomendacionesVistas;

    private Integer maxPostulacionesMes;

    private Integer maxOfertasActivas;

    private Integer maxRecomendaciones;

    private Integer postulacionesRestantes;

    private Integer ofertasRestantes;

    private Integer recomendacionesRestantes;
}