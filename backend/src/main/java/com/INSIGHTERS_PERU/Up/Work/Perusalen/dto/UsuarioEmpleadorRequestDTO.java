package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEmpleadorRequestDTO {
    @NotBlank
    private String tipoEmpleador;

    @NotBlank
    private String nombreComercial;

    @NotBlank
    private String razonSocial;

    @NotBlank
    private String tipoDoc;

    @NotBlank
    private String numDoc;

    private String logoEmpleador;

    private String descripcionNegocio;

    @NotNull
    private Integer aniosOperacion;

    private List<Long> categoriasId;

    private String sitioWeb;

    private String correoContacto;

    private String telefonoContacto;
}
