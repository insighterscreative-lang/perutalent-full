package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoPerfilDTO {

    private Long idOferta;

    private String titulo;

    private String descripcion;

    private String categoria;

    private String modalidad;

    private String estadoOferta;

    private String empleador;

    private String distrito;

    private BigDecimal montoTotal;

    private String experiencia;

    private String tipoDuracion;

    private String fechaPublicacion;

    private String fechaTerminoPostulacion;

    private List<HabilidadTrabajoDTO> habilidades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HabilidadTrabajoDTO {

        private Long id;

        private String nombre;
    }
}