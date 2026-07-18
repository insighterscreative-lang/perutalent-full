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
@Table(name = "oferta_tipo_trabajo",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_oferta", "id_tipo_trabajo"})
    }
)
public class OfertaTipoTrabajo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta_tipo_trabajo")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_oferta", nullable = false)
    private OfertaLaboral idOferta;

    @ManyToOne
    @JoinColumn(name = "id_tipo_trabajo", nullable = false)
    private TipoTrabajo idTipoTrabajo;
}
