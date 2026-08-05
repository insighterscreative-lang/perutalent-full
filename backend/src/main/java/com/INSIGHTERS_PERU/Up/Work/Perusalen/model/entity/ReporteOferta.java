package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import java.time.LocalDateTime;

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

@Entity
@Table(
        name = "reporte_oferta",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reporte_oferta_usuario",
                        columnNames = {"id_oferta", "id_usuario_reportante"}
                )
        }
)
public class ReporteOferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta", nullable = false)
    private OfertaLaboral oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_reportante", nullable = false)
    private Usuario usuarioReportante;

    @Column(name = "motivo", nullable = false, length = 50)
    private String motivo;

    @Column(name = "descripcion", nullable = false, length = 1200)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    public ReporteOferta() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OfertaLaboral getOferta() { return oferta; }
    public void setOferta(OfertaLaboral oferta) { this.oferta = oferta; }
    public Usuario getUsuarioReportante() { return usuarioReportante; }
    public void setUsuarioReportante(Usuario usuarioReportante) { this.usuarioReportante = usuarioReportante; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
