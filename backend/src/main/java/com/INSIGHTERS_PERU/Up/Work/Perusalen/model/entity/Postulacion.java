package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "postulacion",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_oferta", "id_empleado"})
    }
)
public class Postulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulacion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_oferta", nullable = false)
    private OfertaLaboral idOferta;

    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = false)
    private UsuarioEmpleado idEmpleado;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDate fechaPostulacion;

    @Column(name = "estado_postulacion", nullable = false)
    private String estadoPostulacion;

    @Column(name = "cv_url", length = 1000)
    private String cvUrl;
}
