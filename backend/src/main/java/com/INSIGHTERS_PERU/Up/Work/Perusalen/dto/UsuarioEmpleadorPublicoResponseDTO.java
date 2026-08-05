package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Información comercial que puede exponerse desde un perfil público.
 * La razón social o nombre legal, documentos, correo de cuenta y datos de
 * contacto privados quedan reservados para el propietario del perfil.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEmpleadorPublicoResponseDTO {

    private Long idEmpleador;

    private String tipoEmpleador;

    private String nombreComercial;

    private String logoEmpleador;

    private String descripcionNegocio;

    private Integer aniosOperacion;

    private List<String> categorias;

    private Integer trabajosActivos;

    private Integer trabajosFinalizados;

    private List<TrabajoPerfilDTO> trabajosActivosDetalle;

    private List<TrabajoPerfilDTO> trabajosFinalizadosDetalle;

    private List<String> modalidadesContratacion;

    private String sitioWeb;
}
