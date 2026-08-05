package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Información profesional que puede exponerse desde un perfil público.
 * Los documentos, datos de contacto, fecha de nacimiento, género, CV e
 * identificadores de ubicación quedan reservados para el propietario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEmpleadoPublicoResponseDTO {

    private Long idEmpleado;

    private String nombre;

    private String apellido;

    private String fotoPerfil;

    private String distrito;

    private String descripcion;

    private List<String> idiomas;

    private List<String> habilidades;

    private List<String> categorias;

    private String disponibilidadEquipo;

    private List<String> herramientas;

    private List<String> modalidades;

    private Integer trabajosActivos;

    private Integer trabajosFinalizados;

    private List<TrabajoPerfilDTO> trabajosActivosDetalle;

    private List<TrabajoPerfilDTO> trabajosFinalizadosDetalle;
}
