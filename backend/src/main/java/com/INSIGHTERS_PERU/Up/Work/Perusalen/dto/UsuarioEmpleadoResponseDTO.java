package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEmpleadoResponseDTO {

    private Long idEmpleado;

    private String nombre;

    private String apellido;

    private String tipoDoc;

    private String numDoc;

    private LocalDate fechaNacimiento;

    private String genero;

    private String fotoPerfil;

    private Long idDepartamento;

    private Long idProvincia;

    private Long idDistrito;

    private String distrito;

    private String nacionalidad;

    private String telefono;

    private String correo;

    private String descripcion;

    private String curriculum;

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