package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCuentaResponseDTO {
    private Long id;
    private String email;
    private boolean esEmpleado;
    private boolean esEmpleador;
    private LocalDate fechaRegistro;
}
