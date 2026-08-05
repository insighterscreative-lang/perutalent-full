package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReporteOfertaRequestDTO {

    @NotBlank(message = "El motivo del reporte es obligatorio")
    @Pattern(
            regexp = "POSIBLE_ESTAFA|INFORMACION_FALSA|CONTENIDO_INAPROPIADO|OFERTA_DUPLICADA|DATOS_CONTACTO_SOSPECHOSOS|DISCRIMINACION|OTRO",
            message = "El motivo del reporte no es válido"
    )
    private String motivo;

    @NotBlank(message = "La descripción del reporte es obligatoria")
    @Size(min = 10, max = 1200, message = "La descripción debe tener entre 10 y 1200 caracteres")
    private String descripcion;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
