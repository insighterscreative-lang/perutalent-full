package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfertaLaboralResponseDTO {

    private Long id;

    private String titulo;

    private String codigoInterno;

    private String empleador;

    private String distrito;

    private Long idDistrito;

    private BigDecimal montoTotal;

    private String tipoDuracion;

    private Long idDuracion;

    private String categoria;

    private Long idCategoria;

    private String modalidad;

    private Long idMod;

    private String experiencia;

    private Long idExperienciaRequerida;

    private String descripcion;

    private String tareasEspecificas;

    private Integer cantidadDuracion;

    private LocalDate fechaTerminoPostulacion;

    private String estadoOferta;

    private List<HabilidadesResponseDTO> habilidades;

    private LocalDate fechaPublicacion;
}