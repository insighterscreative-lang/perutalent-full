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
@Table(name = "culqi_evento")
public class CulqiEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_culqi_evento")
    private Long id;

    @Column(name = "culqi_event_id", nullable = false, unique = true, length = 150)
    private String culqiEventId;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago")
    private PagoSuscripcion pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion")
    private SuscripcionUsuario suscripcion;

    @Column(name = "procesado", nullable = false)
    private Boolean procesado;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
}