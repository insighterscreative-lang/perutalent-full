package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoSuscripcionResponseDTO {

    private Long idPago;
    private String estadoPago;
    private String mensaje;

    private Long idPlan;
    private String nombrePlan;

    private Integer montoCentimos;
    private String moneda;

    private String culqiChargeId;
    private String culqiSubscriptionId;

    private Boolean renovacionAutomatica;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaProximoCobro;
}
