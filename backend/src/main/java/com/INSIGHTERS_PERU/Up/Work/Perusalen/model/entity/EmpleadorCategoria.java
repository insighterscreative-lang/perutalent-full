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
@Table(name = "empleador_categoria",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_empleador", "id_categoria"})
    }
)
public class EmpleadorCategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleador_categoria")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_empleador", nullable = false)
    private UsuarioEmpleador empleador;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriasTrabajos categoria;
}