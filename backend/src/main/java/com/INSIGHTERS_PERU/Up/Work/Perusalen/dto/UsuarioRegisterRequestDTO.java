package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.validation.PasswordPolicy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegisterRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Pattern(
        regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$",
        message = "Formato de correo inválido"
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String password;

    private Boolean esEmpleado;
    private Boolean esEmpleador;
    private Boolean aceptaTerminos;
}