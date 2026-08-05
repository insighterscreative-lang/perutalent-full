package com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reporte_problema_tecnico")
public class ReporteProblemaTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte_problema")
    private Long id;

    @Column(name = "codigo_reporte", nullable = false, unique = true, length = 30)
    private String codigoReporte;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "tipo_problema", nullable = false, length = 40)
    private String tipoProblema;

    @Column(name = "pantalla", nullable = false, length = 150)
    private String pantalla;

    @Column(name = "descripcion", nullable = false, length = 2500)
    private String descripcion;

    @Column(name = "pasos_reproducir", length = 2000)
    private String pasosReproducir;

    @Column(name = "informacion_adicional", length = 1500)
    private String informacionAdicional;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    public ReporteProblemaTecnico() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoReporte() { return codigoReporte; }
    public void setCodigoReporte(String codigoReporte) { this.codigoReporte = codigoReporte; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTipoProblema() { return tipoProblema; }
    public void setTipoProblema(String tipoProblema) { this.tipoProblema = tipoProblema; }
    public String getPantalla() { return pantalla; }
    public void setPantalla(String pantalla) { this.pantalla = pantalla; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPasosReproducir() { return pasosReproducir; }
    public void setPasosReproducir(String pasosReproducir) { this.pasosReproducir = pasosReproducir; }
    public String getInformacionAdicional() { return informacionAdicional; }
    public void setInformacionAdicional(String informacionAdicional) { this.informacionAdicional = informacionAdicional; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
