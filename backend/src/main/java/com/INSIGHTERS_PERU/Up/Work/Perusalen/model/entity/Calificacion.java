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
@Table(name = "calificacion",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_postulacion", "id_calificador"})
    }
)
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion idPostulacion;

    @ManyToOne
    @JoinColumn(name = "id_calificador", nullable = false)
    private Usuario idCalificador;

    @ManyToOne
    @JoinColumn(name = "id_calificado", nullable = false)
    private Usuario idCalificado;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion;

    @Column(name = "comentario", nullable = false, length = 1000)
    private String comentario;

    @Column(name = "fecha_calificacion", nullable = false)
    private LocalDate fechaCalificacion;
}
