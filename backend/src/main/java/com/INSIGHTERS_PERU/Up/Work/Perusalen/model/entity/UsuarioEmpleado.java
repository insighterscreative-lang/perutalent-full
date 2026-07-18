package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_empleado")
public class UsuarioEmpleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "tipo_doc", nullable = false, length = 50)
    private String tipoDoc;

    @Column(name = "num_doc", nullable = false, unique = true, length = 50)
    private String numDoc;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "genero", nullable = false, length = 50)
    private String genero;

    @Column(name = "telefono", nullable = false, length = 50)
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "id_distrito", nullable = false)
    private Distrito distrito;

    @Column(name = "nacionalidad", nullable = false, length = 50)
    private String nacionalidad;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;
    
    @Column(name = "foto_perfil", length = 1000)
    private String fotoPerfil;

    @Column(name = "curriculum", length = 1000)
    private String curriculum;

    @Column(name = "idiomas", nullable = false, length = 100)
    private String idiomas;

    @Column(name = "disponibilidad_equipo", length = 1000)
    private String disponibilidadEquipo;
}
