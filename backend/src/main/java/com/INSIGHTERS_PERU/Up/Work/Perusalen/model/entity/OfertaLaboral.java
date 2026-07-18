package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "oferta_laboral")
public class OfertaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_empleador", nullable = false)
    private UsuarioEmpleador idEmpleador;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriasTrabajos idCategoria;

    @ManyToOne
    @JoinColumn(name = "id_mod", nullable = false)
    private Modalidad idMod;

    @ManyToOne
    @JoinColumn(name = "id_distrito", nullable = false)
    private Distrito idDistrito;

    @ManyToOne
    @JoinColumn(name = "id_experiencia_requerida", nullable = false)
    private ExperienciaRequerida idExperienciaRequerida;

    @ManyToOne
    @JoinColumn(name = "id_empleado_seleccionado")
    private UsuarioEmpleado empleadoSeleccionado;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "codigo_interno", nullable = false, unique = true, length = 50)
    private String codigoInterno;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "tareas_especificas", nullable = false, length = 1000)
    private String tareasEspecificas;

    @Column(name = "cantidad_duracion", nullable = false)
    private Integer cantidadDuracion;

    @ManyToOne
    @JoinColumn(name = "id_tipo_duracion", nullable = false)
    private TipoDuracion idDuracion;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDate fechaPublicacion;

    @Column(name = "fecha_termino_postulacion", nullable = false)
    private LocalDate fechaTerminoPostulacion;

    @Column(name = "estado_oferta", nullable = false, length = 50)
    private String estadoOferta;

    @OneToMany(mappedBy = "idOferta", fetch = FetchType.LAZY)
    private List<OfertaHabilidad> habilidades;
}