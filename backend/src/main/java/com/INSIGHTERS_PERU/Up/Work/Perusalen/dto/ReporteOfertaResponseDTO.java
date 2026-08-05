package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDateTime;

public class ReporteOfertaResponseDTO {
    private Long idReporte;
    private String estado;
    private LocalDateTime fechaCreacion;

    public ReporteOfertaResponseDTO() {
    }

    public ReporteOfertaResponseDTO(Long idReporte, String estado, LocalDateTime fechaCreacion) {
        this.idReporte = idReporte;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
