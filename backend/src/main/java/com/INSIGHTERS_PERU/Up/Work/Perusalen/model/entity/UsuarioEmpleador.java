package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_empleador")
public class UsuarioEmpleador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleador")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "tipo_empleador", nullable = false, length = 50)
    private String tipoEmpleador;

    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "tipo_doc", nullable = false, length = 50)
    private String tipoDoc;

    @Column(name = "num_doc", nullable = false, unique = true, length = 50)
    private String numDoc;

    @Column(name = "logo_empleador", length = 300)
    private String logoEmpleador;

    @Column(name = "descripcion_negocio", length = 1000)
    private String descripcionNegocio;

    @Column(name = "anios_operacion", nullable = false)
    private Integer aniosOperacion;

    @Column(name = "sitio_web", length = 300)
    private String sitioWeb;

    @Column(name = "correo_contacto", length = 150)
    private String correoContacto;

    @Column(name = "telefono_contacto", length = 50)
    private String telefonoContacto;
}
