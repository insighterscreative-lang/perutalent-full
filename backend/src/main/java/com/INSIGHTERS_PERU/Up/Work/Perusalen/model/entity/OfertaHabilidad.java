package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

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
@Table(name = "oferta_habilidad",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_oferta", "id_habilidad"})
    }
)
public class OfertaHabilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta_habilidad")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_oferta", nullable = false)
    private OfertaLaboral idOferta;

    @ManyToOne
    @JoinColumn(name = "id_habilidad", nullable = false)
    private Habilidades idHabilidad;
}

