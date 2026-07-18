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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pago_suscripcion")
public class PagoSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanSuscripcion plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion")
    private SuscripcionUsuario suscripcion;

    @Column(name = "monto_centimos", nullable = false)
    private Integer montoCentimos;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "estado_pago", nullable = false, length = 50)
    private String estadoPago;

    @Column(name = "tipo_pago", nullable = false, length = 50)
    private String tipoPago;

    @Column(name = "culqi_charge_id", unique = true, length = 150)
    private String culqiChargeId;

    @Column(name = "culqi_subscription_id", length = 150)
    private String culqiSubscriptionId;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "respuesta_culqi", columnDefinition = "TEXT")
    private String respuestaCulqi;
}