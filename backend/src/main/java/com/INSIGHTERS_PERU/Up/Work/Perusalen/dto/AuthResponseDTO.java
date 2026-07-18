package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long id;
    private String email;
    private boolean esEmpleado;
    private boolean esEmpleador;
}
