package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato de email no es valido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
