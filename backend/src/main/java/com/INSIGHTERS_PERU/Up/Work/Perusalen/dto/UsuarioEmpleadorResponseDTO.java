package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEmpleadorResponseDTO {
    private Long idEmpleador;

    private String tipoEmpleador;

    private String nombreComercial;

    private String razonSocial;

    private String tipoDoc;

    private String numDoc;

    private String logoEmpleador;

    private String descripcionNegocio;

    private Integer aniosOperacion;

    private String correo;

    private List<String> categorias;

    private Integer trabajosActivos;

    private Integer trabajosFinalizados;

    private List<TrabajoPerfilDTO> trabajosActivosDetalle;

    private List<TrabajoPerfilDTO> trabajosFinalizadosDetalle;

    private List<String> modalidadesContratacion;

    private String sitioWeb;

    private String correoContacto;

    private String telefonoContacto;
}
