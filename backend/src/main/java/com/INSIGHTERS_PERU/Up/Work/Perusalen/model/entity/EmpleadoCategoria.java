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
@Table(name = "empleado_categoria",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_empleado", "id_cateforia"})
    }
)
public class EmpleadoCategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado_categoria")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = false)
    private UsuarioEmpleado idEmpleado;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriasTrabajos idCategoria;
}
