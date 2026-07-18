package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "uso_plan_usuario",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_uso_usuario_periodo",
                        columnNames = {"id_usuario", "periodo"}
                )
        }
)
public class UsoPlanUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_uso")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "periodo", nullable = false, length = 7)
    private String periodo;

    @Column(name = "postulaciones_usadas", nullable = false)
    private Integer postulacionesUsadas;

    @Column(name = "ofertas_publicadas", nullable = false)
    private Integer ofertasPublicadas;

    @Column(name = "recomendaciones_vistas", nullable = false)
    private Integer recomendacionesVistas;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}