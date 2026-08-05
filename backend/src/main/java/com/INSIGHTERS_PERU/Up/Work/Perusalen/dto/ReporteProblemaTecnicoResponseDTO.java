package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDateTime;

public class ReporteProblemaTecnicoResponseDTO {
    private String codigoReporte;
    private String estado;
    private LocalDateTime fechaCreacion;

    public ReporteProblemaTecnicoResponseDTO() {
    }

    public ReporteProblemaTecnicoResponseDTO(
            String codigoReporte,
            String estado,
            LocalDateTime fechaCreacion
    ) {
        this.codigoReporte = codigoReporte;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public String getCodigoReporte() { return codigoReporte; }
    public void setCodigoReporte(String codigoReporte) { this.codigoReporte = codigoReporte; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
