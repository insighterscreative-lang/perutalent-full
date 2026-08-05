package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfertaLaboralRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 50, message = "El título no puede superar los 50 caracteres")
    private String titulo;

    @NotBlank(message = "El código interno es obligatorio")
    @Size(max = 50, message = "El código interno no puede superar los 50 caracteres")
    private String codigoInterno;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @NotBlank(message = "Las tareas específicas son obligatorias")
    @Size(max = 1000, message = "Las tareas específicas no pueden superar los 1000 caracteres")
    private String tareasEspecificas;

    @NotNull(message = "La cantidad de duración es obligatoria")
    @Positive(message = "La cantidad de duración debe ser mayor a 0")
    private Integer cantidadDuracion;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto total debe ser mayor a 0")
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha de término de postulación es obligatoria")
    private LocalDate fechaTerminoPostulacion;

    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    @NotNull(message = "La modalidad es obligatoria")
    private Long idMod;

    @NotNull(message = "El distrito es obligatorio")
    private Long idDistrito;

    @NotNull(message = "La experiencia requerida es obligatoria")
    private Long idExperienciaRequerida;

    @NotNull(message = "El tipo de duración es obligatorio")
    private Long idDuracion;

    private List<Long> habilidadesId;
}