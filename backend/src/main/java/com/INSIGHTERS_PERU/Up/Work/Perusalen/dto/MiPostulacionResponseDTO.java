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
public class MiPostulacionResponseDTO {

    private Long idPostulacion;
    private Long idOferta;

    private String tituloOferta;
    private Long idEmpleador;
    private String empleador;

    private String distrito;
    private String categoria;
    private String modalidad;
    private String experiencia;
    private String tipoDuracion;

    private Integer cantidadDuracion;
    private BigDecimal montoTotal;

    private String descripcionOferta;
    private String tareasEspecificas;
    private List<HabilidadesResponseDTO> habilidades;

    private LocalDate fechaPublicacion;
    private LocalDate fechaTerminoPostulacion;
    private String estadoOferta;
    private Boolean ofertaVencida;
    private Boolean ofertaFinalizada;

    private LocalDate fechaPostulacion;
    private String estadoPostulacion;
    private Boolean cvDisponible;
}
