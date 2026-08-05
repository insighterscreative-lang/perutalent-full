package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.time.LocalDateTime;

public class ReclamoResponseDTO {
    private String codigoReclamo;
    private String estado;
    private LocalDateTime fechaCreacion;

    public ReclamoResponseDTO() {
    }

    public ReclamoResponseDTO(String codigoReclamo, String estado, LocalDateTime fechaCreacion) {
        this.codigoReclamo = codigoReclamo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public String getCodigoReclamo() { return codigoReclamo; }
    public void setCodigoReclamo(String codigoReclamo) { this.codigoReclamo = codigoReclamo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
