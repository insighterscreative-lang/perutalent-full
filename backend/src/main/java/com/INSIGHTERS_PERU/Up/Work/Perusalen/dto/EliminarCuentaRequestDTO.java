package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EliminarCuentaRequestDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La confirmación es obligatoria")
    private String confirmacion;
}
