package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEmpleadoRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String tipoDoc;

    @NotBlank
    private String numDoc;

    @NotNull
    private LocalDate fechaNacimiento;

    @NotBlank
    private String genero;

    @NotBlank
    private String telefono;

    @NotNull
    private Long idDistrito;

    @NotBlank
    private String nacionalidad;

    private String descripcion;

    private String curriculum;

    private String fotoPerfil;

    private List<String> idiomas;

    private String disponibilidadEquipo;

    private List<Long> habilidadesId;

    private List<Long> categoriasId;

    private List<Long> herramientasId;

    private List<Long> modalidadesId;
}