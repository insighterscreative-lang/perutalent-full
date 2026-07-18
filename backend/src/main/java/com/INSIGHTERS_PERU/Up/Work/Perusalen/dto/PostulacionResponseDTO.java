package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostulacionResponseDTO {

    private Long idPostulacion;

    private Long idEmpleado;
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private String emailEmpleado;
    private String telefonoEmpleado;

    private Long idDistrito;
    private String distrito;

    private List<Long> modalidadIds;
    private List<String> modalidades;

    private List<Long> habilidadIds;
    private List<String> habilidades;

    private List<Long> herramientaIds;
    private List<String> herramientas;

    private LocalDate fechaPostulacion;
    private String estadoPostulacion;

    private String cvUrl;

    private Boolean empleadoPremium;
    private String planEmpleado;
}